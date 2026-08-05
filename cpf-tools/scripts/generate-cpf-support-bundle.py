#!/usr/bin/env python3
"""Create a consent-bound, sanitized CPF support bundle.

The tool intentionally accepts only an explicit allowlist manifest. It never walks the
repository or runtime directory implicitly. Every collected text file is masked,
re-scanned, hashed, and written with deterministic ZIP metadata.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
import zipfile
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path, PurePosixPath
from typing import Iterable

SENSITIVE_KEY = re.compile(
    r"(?i)(password|passwd|pwd|secret|token|api[_-]?key|authorization|proxy-authorization|cookie|set-cookie|session[_-]?id|private[_-]?key|client[_-]?secret)"
)
KEY_VALUE = re.compile(
    r"(?im)(?P<prefix>\b(?:password|passwd|pwd|secret|token|api[_-]?key|session[_-]?id|private[_-]?key|client[_-]?secret)\b\s*[:=]\s*)(?P<quote>['\"]?)(?P<value>[^\s,'\"}\]]+)(?P=quote)"
)
JSON_SECRET = re.compile(
    r'(?i)(?P<prefix>"(?:password|passwd|pwd|secret|token|api[_-]?key|authorization|proxy-authorization|cookie|set-cookie|session[_-]?id|private[_-]?key|client[_-]?secret)"\s*:\s*")(?P<value>(?:\\.|[^"\\])*)(?P<suffix>")'
)
HEADER_SECRET = re.compile(
    r"(?im)(?P<prefix>^[ \t]*(?:authorization|proxy-authorization|cookie|set-cookie)\s*:\s*)(?P<value>[^\r\n]+)"
)
JWT = re.compile(r"\beyJ[a-zA-Z0-9_-]{8,}\.[a-zA-Z0-9_-]{8,}\.[a-zA-Z0-9_-]{8,}\b")
URL_CREDENTIAL = re.compile(r"(?i)(https?://)([^/@:\s]+):([^/@\s]+)@")
PRIVATE_KEY = re.compile(
    r"-----BEGIN (?:RSA |EC |OPENSSH |ENCRYPTED )?PRIVATE KEY-----.*?-----END (?:RSA |EC |OPENSSH |ENCRYPTED )?PRIVATE KEY-----",
    re.DOTALL,
)
RESIDUAL_URL_CREDENTIAL = re.compile(r"(?i)https?://(?!\*\*\*MASKED\*\*\*)[^/@:\s]+:(?!\*\*\*MASKED\*\*\*)[^/@\s]+@")
RESIDUAL_AUTHORIZATION_TAIL = re.compile(
    r"(?im)^[ \t]*(?:authorization|proxy-authorization)\s*:\s*\*\*\*MASKED\*\*\*[ \t]+\S.*$"
)
UNMASKED_SENTINELS = (PRIVATE_KEY, JWT, RESIDUAL_URL_CREDENTIAL, RESIDUAL_AUTHORIZATION_TAIL)
ALLOWED_KINDS = {"version", "config", "topology", "health", "log", "metric", "thread_dump", "diagnostic"}
FIXED_ZIP_TIME = (2020, 1, 1, 0, 0, 0)


class BundleError(RuntimeError):
    pass


@dataclass(frozen=True)
class CollectedFile:
    source: str
    kind: str
    bundle_path: str
    original_size: int
    sanitized_size: int
    masked_values: int
    sha256: str


def _parse_utc(value: str, field: str) -> datetime:
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError as exc:
        raise BundleError(f"{field} must be ISO-8601: {value!r}") from exc
    if parsed.tzinfo is None:
        raise BundleError(f"{field} must include a timezone")
    return parsed.astimezone(timezone.utc)


def _require_consent(manifest: dict, now: datetime) -> dict:
    consent = manifest.get("consent")
    if not isinstance(consent, dict):
        raise BundleError("consent object is required")
    required = ("approved_by", "approved_at", "reason", "expires_at")
    missing = [name for name in required if not str(consent.get(name, "")).strip()]
    if missing:
        raise BundleError("consent fields missing: " + ", ".join(missing))
    approved_at = _parse_utc(str(consent["approved_at"]), "consent.approved_at")
    expires_at = _parse_utc(str(consent["expires_at"]), "consent.expires_at")
    if expires_at <= now:
        raise BundleError("consent has expired")
    if approved_at > now:
        raise BundleError("consent.approved_at is in the future")
    if expires_at <= approved_at:
        raise BundleError("consent.expires_at must be after approved_at")
    return {
        "approved_by": str(consent["approved_by"]).strip(),
        "approved_at": approved_at.isoformat().replace("+00:00", "Z"),
        "reason": str(consent["reason"]).strip(),
        "expires_at": expires_at.isoformat().replace("+00:00", "Z"),
    }


def _safe_relative(raw: str) -> PurePosixPath:
    normalized = raw.replace("\\", "/")
    path = PurePosixPath(normalized)
    if path.is_absolute() or not path.parts or any(part in {"", ".", ".."} for part in path.parts):
        raise BundleError(f"unsafe relative path: {raw!r}")
    if ":" in path.parts[0]:
        raise BundleError(f"drive-qualified path is not allowed: {raw!r}")
    return path


def _resolve_under(root: Path, relative: PurePosixPath) -> Path:
    root_resolved = root.resolve()
    candidate = root_resolved.joinpath(*relative.parts).resolve()
    try:
        candidate.relative_to(root_resolved)
    except ValueError as exc:
        raise BundleError(f"path escapes collection root: {relative}") from exc
    if not candidate.is_file():
        raise BundleError(f"required support input does not exist: {relative}")
    return candidate


def _mask_text(text: str) -> tuple[str, int]:
    masked = 0

    def replace_json(match: re.Match[str]) -> str:
        nonlocal masked
        masked += 1
        return f'{match.group("prefix")}***MASKED***{match.group("suffix")}'

    def replace_key_value(match: re.Match[str]) -> str:
        nonlocal masked
        masked += 1
        return f'{match.group("prefix")}***MASKED***'

    def replace_url(match: re.Match[str]) -> str:
        nonlocal masked
        masked += 1
        return f"{match.group(1)}***MASKED***:***MASKED***@"

    def replace_generic(pattern: re.Pattern[str], value: str) -> str:
        nonlocal masked
        matches = len(pattern.findall(value))
        masked += matches
        return pattern.sub("***MASKED***", value)

    text = JSON_SECRET.sub(replace_json, text)
    text = HEADER_SECRET.sub(replace_key_value, text)
    text = KEY_VALUE.sub(replace_key_value, text)
    text = URL_CREDENTIAL.sub(replace_url, text)
    text = replace_generic(PRIVATE_KEY, text)
    text = replace_generic(JWT, text)
    return text, masked


def _assert_sanitized(text: str, source: str) -> None:
    for pattern in UNMASKED_SENTINELS:
        if pattern.search(text):
            raise BundleError(f"sanitization failed for {source}: residual sensitive pattern")
    for line in text.splitlines():
        if SENSITIVE_KEY.search(line) and "***MASKED***" not in line:
            # Key names in prose are allowed; only fail when the line looks like an assignment.
            if ":" in line or "=" in line:
                raise BundleError(f"sanitization failed for {source}: possible unmasked assignment")


def _zip_write(zf: zipfile.ZipFile, name: str, data: bytes) -> None:
    info = zipfile.ZipInfo(name, FIXED_ZIP_TIME)
    info.compress_type = zipfile.ZIP_DEFLATED
    info.external_attr = 0o600 << 16
    zf.writestr(info, data)


def _canonical_json(value: object) -> bytes:
    return (json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n").encode("utf-8")


def generate_bundle(collection_root: Path, manifest_path: Path, output_path: Path, now: datetime | None = None) -> dict:
    now = (now or datetime.now(timezone.utc)).astimezone(timezone.utc)
    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise BundleError(f"cannot read input manifest: {exc}") from exc
    if not isinstance(manifest, dict):
        raise BundleError("input manifest must be a JSON object")

    request_id = str(manifest.get("request_id", "")).strip()
    if not request_id:
        raise BundleError("request_id is required")
    consent = _require_consent(manifest, now)
    max_total_bytes = int(manifest.get("max_total_bytes", 20 * 1024 * 1024))
    if max_total_bytes <= 0:
        raise BundleError("max_total_bytes must be positive")
    entries = manifest.get("files")
    if not isinstance(entries, list) or not entries:
        raise BundleError("files must be a non-empty array")

    collected: list[CollectedFile] = []
    payloads: list[tuple[str, bytes]] = []
    total_original = 0
    total_sanitized = 0
    seen_bundle_paths: set[str] = set()

    for index, entry in enumerate(entries):
        if not isinstance(entry, dict):
            raise BundleError(f"files[{index}] must be an object")
        kind = str(entry.get("kind", "")).strip()
        if kind not in ALLOWED_KINDS:
            raise BundleError(f"files[{index}].kind is unsupported: {kind!r}")
        relative = _safe_relative(str(entry.get("path", "")))
        source = _resolve_under(collection_root, relative)
        data = source.read_bytes()
        total_original += len(data)
        if total_original > max_total_bytes:
            raise BundleError("support inputs exceed max_total_bytes")
        if b"\x00" in data:
            raise BundleError(f"binary support input is not allowed: {relative}")
        try:
            text = data.decode("utf-8")
        except UnicodeDecodeError as exc:
            raise BundleError(f"support input must be UTF-8 text: {relative}") from exc
        sanitized, masked_values = _mask_text(text)
        _assert_sanitized(sanitized, str(relative))
        sanitized_bytes = sanitized.encode("utf-8")
        total_sanitized += len(sanitized_bytes)
        bundle_path = f"bundle/{kind}/{relative.as_posix()}"
        if bundle_path in seen_bundle_paths:
            raise BundleError(f"duplicate bundle path: {bundle_path}")
        seen_bundle_paths.add(bundle_path)
        digest = hashlib.sha256(sanitized_bytes).hexdigest()
        collected.append(
            CollectedFile(
                source=relative.as_posix(),
                kind=kind,
                bundle_path=bundle_path,
                original_size=len(data),
                sanitized_size=len(sanitized_bytes),
                masked_values=masked_values,
                sha256=digest,
            )
        )
        payloads.append((bundle_path, sanitized_bytes))

    result_manifest = {
        "schema_version": "1.0",
        "request_id": request_id,
        "generated_at": now.isoformat().replace("+00:00", "Z"),
        "bundle_expires_at": consent["expires_at"],
        "consent": consent,
        "collection_root": ".",
        "file_count": len(collected),
        "total_original_bytes": total_original,
        "total_sanitized_bytes": total_sanitized,
        "files": [item.__dict__ for item in sorted(collected, key=lambda item: item.bundle_path)],
    }
    manifest_bytes = _canonical_json(result_manifest)
    manifest_hash = hashlib.sha256(manifest_bytes).hexdigest()

    output_path.parent.mkdir(parents=True, exist_ok=True)
    temp = output_path.with_suffix(output_path.suffix + ".tmp")
    try:
        with zipfile.ZipFile(temp, "w") as zf:
            for name, data in sorted(payloads):
                _zip_write(zf, name, data)
            _zip_write(zf, "bundle-manifest.json", manifest_bytes)
            _zip_write(zf, "bundle-manifest.sha256", f"{manifest_hash}  bundle-manifest.json\n".encode("ascii"))
        temp.replace(output_path)
    finally:
        if temp.exists():
            temp.unlink()

    return {
        "status": "PASS",
        "output": str(output_path),
        "file_count": len(collected),
        "masked_values": sum(item.masked_values for item in collected),
        "manifest_sha256": manifest_hash,
        "bundle_sha256": hashlib.sha256(output_path.read_bytes()).hexdigest(),
        "expires_at": consent["expires_at"],
    }


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--collection-root", required=True, type=Path)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--result-json", type=Path)
    return parser


def main(argv: Iterable[str] | None = None) -> int:
    args = _parser().parse_args(argv)
    try:
        result = generate_bundle(args.collection_root, args.manifest, args.output)
    except BundleError as exc:
        result = {"status": "FAIL", "error": str(exc)}
        if args.result_json:
            args.result_json.parent.mkdir(parents=True, exist_ok=True)
            args.result_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False), file=sys.stderr)
        return 2
    if args.result_json:
        args.result_json.parent.mkdir(parents=True, exist_ok=True)
        args.result_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
