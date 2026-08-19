#!/usr/bin/env python3
"""Fail-closed validation for CPF development evidence and desired-state package metadata.

The verifier intentionally separates three identities:
* baselineSourceZipSha256: provenance of the input ZIP when an exact Git SHA is unavailable.
* sourceIdentity: deterministic identity of the source tree excluding identity-bearing evidence metadata.
* packagePayloadIdentity: deterministic identity of all desired-state files except the three self-referential package metadata files.

This separation prevents evidence files from hashing their own identity while still validating every packaged byte.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
from pathlib import Path

SHA1_RE = re.compile(r"^[0-9a-f]{40}$")
SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
PLACEHOLDERS = ("TODO", "TBD", "<HEAD>", "<SHA>", "YOUR_", "REPLACE_ME", "추후", "나중에 실행", "미정")
DEFAULT_METADATA_NAMES = ("PACKAGE_MANIFEST.json", "CHANGE_MANIFEST.csv", "SHA256SUMS.txt")


class GateError(RuntimeError):
    pass


def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def _rows(path: Path):
    if not path.is_file():
        raise GateError(f"missing csv: {path}")
    with path.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        return list(reader.fieldnames or []), [
            {key: (value or "").strip() for key, value in row.items()} for row in reader
        ]


def _safe(root: Path, rel: str) -> Path:
    rel = rel.replace("\\", "/").strip()
    if not rel or rel.startswith(("/", "\\")) or ".." in Path(rel).parts:
        raise GateError(f"unsafe evidence path: {rel!r}")
    target = (root / rel).resolve()
    if target != root and root not in target.parents:
        raise GateError(f"evidence escapes root: {rel}")
    return target


def _all_file_paths(root: Path) -> list[str]:
    return sorted(
        path.relative_to(root).as_posix()
        for path in root.rglob("*")
        if path.is_file()
    )


def _identity_from_entries(entries: list[tuple[str, str]]) -> tuple[str, str]:
    """Hash a sorted hash/path inventory; avoids platform metadata and filesystem ordering drift."""
    material = "".join(f"{digest}  {rel}\n" for rel, digest in sorted(entries)).encode("utf-8")
    return hashlib.sha1(material).hexdigest(), hashlib.sha256(material).hexdigest()


def _compute_tree_identity(root: Path, excluded: set[str]) -> tuple[str, str, int, int]:
    entries: list[tuple[str, str]] = []
    total_bytes = 0
    for rel in _all_file_paths(root):
        if rel in excluded:
            continue
        target = root / rel
        entries.append((rel, _sha256(target)))
        total_bytes += target.stat().st_size
    sha1, sha256 = _identity_from_entries(entries)
    return sha1, sha256, len(entries), total_bytes


def _sha_list(path: Path, root: Path) -> dict[str, str]:
    if not path.is_file():
        raise GateError(f"missing sha list: {path}")
    out: dict[str, str] = {}
    for lineno, line in enumerate(path.read_text(encoding="utf-8-sig", errors="replace").splitlines(), 1):
        if not line.strip():
            continue
        parts = line.strip().split(None, 1)
        if len(parts) != 2 or not SHA256_RE.fullmatch(parts[0].lower()):
            raise GateError(f"invalid SHA256SUMS line {lineno}")
        rel = parts[1].strip().lstrip("*").replace("\\", "/")
        if rel in out:
            raise GateError(f"duplicate SHA256SUMS path: {rel}")
        target = _safe(root, rel)
        if not target.is_file():
            raise GateError(f"SHA256SUMS file missing: {rel}")
        actual = _sha256(target)
        if actual != parts[0].lower():
            raise GateError(f"SHA256SUMS hash mismatch: {rel}")
        out[rel] = actual
    return out


def _verify_change_manifest(path: Path, root: Path, payload_paths: set[str], package_rel: str) -> dict[str, dict]:
    fields, rows = _rows(path)
    required = {"path", "change_type", "size_bytes", "sha256", "baseline_sha256"}
    missing = required - set(fields)
    if missing:
        raise GateError(f"change manifest missing columns: {sorted(missing)}")
    out: dict[str, dict] = {}
    for row in rows:
        rel = row["path"].replace("\\", "/").strip()
        if not rel or rel in out:
            raise GateError(f"missing/duplicate change path: {rel!r}")
        change = row["change_type"].upper()
        if change not in {"ADDED", "MODIFIED", "DELETED"}:
            raise GateError(f"{rel}: invalid change_type={change}")
        target = _safe(root, rel)
        baseline_digest = row["baseline_sha256"].lower()
        if change == "DELETED":
            if target.exists():
                raise GateError(f"{rel}: DELETED path still exists")
            if not SHA256_RE.fullmatch(baseline_digest):
                raise GateError(f"{rel}: DELETED baseline_sha256 missing/invalid")
            if row["sha256"] or row["size_bytes"] not in {"", "0"}:
                raise GateError(f"{rel}: DELETED target metadata must be blank/0")
        else:
            if not target.is_file():
                raise GateError(f"change manifest file missing: {rel}")
            try:
                size = int(row["size_bytes"])
            except Exception as exc:
                raise GateError(f"{rel}: invalid size_bytes") from exc
            digest = row["sha256"].lower()
            if size != target.stat().st_size:
                raise GateError(f"change manifest size mismatch: {rel}")
            if not SHA256_RE.fullmatch(digest) or digest != _sha256(target):
                raise GateError(f"change manifest hash mismatch: {rel}")
            if change == "ADDED" and baseline_digest:
                raise GateError(f"{rel}: ADDED baseline_sha256 must be blank")
            if change == "MODIFIED" and not SHA256_RE.fullmatch(baseline_digest):
                raise GateError(f"{rel}: MODIFIED baseline_sha256 missing/invalid")
            if rel not in payload_paths and rel != package_rel:
                raise GateError(f"change manifest path is not packaged: {rel}")
        out[rel] = row
    return out


def _validate_manifest(root: Path, review: Path, manifest: dict) -> dict:
    if int(manifest.get("schemaVersion", 0)) < 6:
        raise GateError("package manifest schemaVersion must be >= 6")

    baseline = str(manifest.get("baselineSourceZipSha256", "")).strip().lower()
    if not SHA256_RE.fullmatch(baseline):
        raise GateError("package manifest baselineSourceZipSha256 missing/invalid")
    git_sha = str(manifest.get("gitExactSha", "")).strip()
    if git_sha != "UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT" and not SHA1_RE.fullmatch(git_sha.lower()):
        raise GateError("package manifest gitExactSha missing/invalid")

    source_identity = manifest.get("sourceIdentity")
    if not isinstance(source_identity, dict):
        raise GateError("package manifest sourceIdentity missing")
    source_sha1 = str(source_identity.get("sha1", "")).lower()
    source_sha256 = str(source_identity.get("sha256", "")).lower()
    if not SHA1_RE.fullmatch(source_sha1) or not SHA256_RE.fullmatch(source_sha256):
        raise GateError("package manifest sourceIdentity hash missing/invalid")
    source_exclusions = source_identity.get("excludedPaths")
    if not isinstance(source_exclusions, list) or not source_exclusions:
        raise GateError("package manifest sourceIdentity.excludedPaths missing")
    excluded = {str(value).replace("\\", "/") for value in source_exclusions}
    computed_source = _compute_tree_identity(root, excluded)
    if computed_source[0] != source_sha1 or computed_source[1] != source_sha256:
        raise GateError(
            "source identity mismatch "
            f"expected={source_sha256} actual={computed_source[1]}"
        )
    expected_source_count = source_identity.get("fileCount")
    if expected_source_count is not None and int(expected_source_count) != computed_source[2]:
        raise GateError("source identity fileCount mismatch")

    review_rel = review.relative_to(root).as_posix()
    canonical_metadata = {f"{review_rel}/{name}" for name in DEFAULT_METADATA_NAMES}
    metadata_exclusions = manifest.get("packageMetadataExcludedPaths", sorted(canonical_metadata))
    if not isinstance(metadata_exclusions, list):
        raise GateError("packageMetadataExcludedPaths must be a list")
    metadata_excluded = {str(value).replace("\\", "/") for value in metadata_exclusions}
    if metadata_excluded != canonical_metadata:
        raise GateError("package metadata exclusions must be the three review-directory self-referential files")

    files = manifest.get("files")
    if not isinstance(files, list) or not files:
        raise GateError("package manifest files must be a non-empty list")
    seen: set[str] = set()
    entries: list[tuple[str, str]] = []
    payload_bytes = 0
    for item in files:
        if not isinstance(item, dict):
            raise GateError("package manifest file item must be object")
        rel = str(item.get("path", "")).replace("\\", "/")
        if not rel or rel in seen:
            raise GateError(f"missing/duplicate manifest path: {rel!r}")
        if rel in metadata_excluded:
            raise GateError(f"self-referential metadata must not be package payload: {rel}")
        seen.add(rel)
        target = _safe(root, rel)
        if not target.is_file():
            raise GateError(f"manifest file missing: {rel}")
        try:
            size = int(item.get("sizeBytes", item.get("size_bytes")))
        except Exception as exc:
            raise GateError(f"manifest size invalid: {rel}") from exc
        digest = str(item.get("sha256", "")).lower()
        if size != target.stat().st_size:
            raise GateError(f"manifest size mismatch: {rel}")
        if not SHA256_RE.fullmatch(digest) or digest != _sha256(target):
            raise GateError(f"manifest hash mismatch: {rel}")
        entries.append((rel, digest))
        payload_bytes += size

    actual_payload = set(_all_file_paths(root)) - metadata_excluded
    if seen != actual_payload:
        missing = sorted(actual_payload - seen)[:5]
        extra = sorted(seen - actual_payload)[:5]
        raise GateError(f"package payload inventory mismatch missing={missing} extra={extra}")

    payload_sha1, payload_sha256 = _identity_from_entries(entries)
    payload = manifest.get("packagePayloadIdentity")
    if not isinstance(payload, dict):
        raise GateError("packagePayloadIdentity missing")
    if str(payload.get("sha1", "")).lower() != payload_sha1:
        raise GateError("package payload SHA-1 mismatch")
    if str(payload.get("sha256", "")).lower() != payload_sha256:
        raise GateError("package payload SHA-256 mismatch")
    if int(payload.get("fileCount", -1)) != len(seen):
        raise GateError("package payload fileCount mismatch")
    if int(payload.get("totalBytes", -1)) != payload_bytes:
        raise GateError("package payload totalBytes mismatch")

    desired = manifest.get("desiredState")
    if not isinstance(desired, dict):
        raise GateError("desiredState missing")
    if int(desired.get("fullFileCount", -1)) != len(_all_file_paths(root)):
        raise GateError("desiredState fullFileCount mismatch")

    return {
        "sourceSha1": source_sha1,
        "sourceSha256": source_sha256,
        "payloadSha1": payload_sha1,
        "payloadSha256": payload_sha256,
        "payloadPaths": seen,
        "manifestFiles": len(seen),
    }


def verify(root: Path, review_dir: Path, expected_sha: str | None, source_head: str | None,
           expected_requirements: int, expected_findings: int):
    root = root.resolve()
    review = (root / review_dir).resolve() if not review_dir.is_absolute() else review_dir.resolve()
    if review != root and root not in review.parents:
        raise GateError("review directory escapes root")

    required = [
        "PACKAGE_MANIFEST.json",
        "SHA256SUMS.txt",
        "QA_FINDING_REVALIDATION.csv",
        "REQUIREMENT_STATUS.csv",
        "TEST_AND_EVIDENCE.md",
        "CHANGE_MANIFEST.csv",
    ]
    for name in required:
        if not (review / name).is_file():
            raise GateError(f"missing review artifact: {name}")

    manifest = json.loads((review / "PACKAGE_MANIFEST.json").read_text(encoding="utf-8"))
    package = _validate_manifest(root, review, manifest)

    # Optional CLI identity checks remain available for direct invocations, but do not substitute
    # a whole-tree runtime hash for the manifest's explicitly scoped source identity.
    if expected_sha:
        expected_sha = expected_sha.lower()
        if not SHA1_RE.fullmatch(expected_sha):
            raise GateError("expected SHA format invalid")
        if not source_head:
            raise GateError("--expected-sha requires --source-head")
    if source_head:
        source_head = source_head.lower()
        if not SHA1_RE.fullmatch(source_head):
            raise GateError("runtime source identity format invalid")
        if expected_sha and source_head != expected_sha:
            raise GateError(f"runtime source identity mismatch expected={expected_sha} actual={source_head}")

    sha_entries = _sha_list(review / "SHA256SUMS.txt", root)
    package_rel = (review / "PACKAGE_MANIFEST.json").relative_to(root).as_posix()
    change_rel = (review / "CHANGE_MANIFEST.csv").relative_to(root).as_posix()
    sha_rel = (review / "SHA256SUMS.txt").relative_to(root).as_posix()
    expected_sha_paths = set(package["payloadPaths"]) | {package_rel, change_rel}
    if sha_rel in sha_entries:
        raise GateError("SHA256SUMS.txt must not hash itself")
    if set(sha_entries) != expected_sha_paths:
        missing = sorted(expected_sha_paths - set(sha_entries))[:5]
        extra = sorted(set(sha_entries) - expected_sha_paths)[:5]
        raise GateError(f"SHA256SUMS inventory mismatch missing={missing} extra={extra}")

    _verify_change_manifest(review / "CHANGE_MANIFEST.csv", root, package["payloadPaths"], package_rel)

    fields, findings = _rows(review / "QA_FINDING_REVALIDATION.csv")
    mandatory = {
        "finding_id", "개발GPT_상태", "source_head", "result_identity",
        "positive_exit_code", "negative_exit_code", "regression_exit_code",
        "evidence_paths", "execution_command",
    }
    missing = mandatory - set(fields)
    if missing:
        raise GateError(f"finding ledger missing columns: {sorted(missing)}")
    if len(findings) != expected_findings:
        raise GateError(f"finding count mismatch expected={expected_findings} actual={len(findings)}")

    ids: set[str] = set()
    complete = incomplete = evidence_refs = 0
    completed_commands: dict[str, str] = {}
    expected_result_identity = (
        f"CONTENT_SHA1_{package['sourceSha1']};CONTENT_SHA256_{package['sourceSha256']}"
    )
    for row in findings:
        fid = row["finding_id"]
        if not fid or fid in ids:
            raise GateError(f"missing/duplicate finding id: {fid}")
        ids.add(fid)
        state = row["개발GPT_상태"]
        if state not in {"완료", "미완료"}:
            raise GateError(f"{fid}: invalid developer state {state!r}")
        if row["source_head"].lower() != package["sourceSha1"]:
            raise GateError(f"{fid}: stale source identity")
        if row["result_identity"] != expected_result_identity:
            raise GateError(f"{fid}: stale result identity")
        command = row["execution_command"]
        if not command or any(token.lower() in command.lower() for token in PLACEHOLDERS):
            raise GateError(f"{fid}: non-reproducible command")
        refs = [value.strip() for value in re.split(r"[;\n]", row["evidence_paths"]) if value.strip()]
        if not refs:
            raise GateError(f"{fid}: evidence missing")
        for rel in refs:
            if not _safe(root, rel).is_file():
                raise GateError(f"{fid}: referenced evidence missing: {rel}")
            evidence_refs += 1
        if state == "완료":
            normalized_command = " ".join(command.split())
            previous = completed_commands.get(normalized_command)
            if previous:
                raise GateError(f"{fid}: execution command duplicates completed finding {previous}")
            completed_commands[normalized_command] = fid
            if not any(fid.lower() in rel.lower() for rel in refs):
                raise GateError(f"{fid}: completed finding lacks dedicated evidence path containing finding ID")
            for key in ("positive_exit_code", "negative_exit_code", "regression_exit_code"):
                if row[key] != "0":
                    raise GateError(f"{fid}: completed finding lacks successful {key}")
            complete += 1
        else:
            if not row.get("미완료사유", "").strip():
                raise GateError(f"{fid}: incomplete reason missing")
            incomplete += 1

    req_fields, reqs = _rows(review / "REQUIREMENT_STATUS.csv")
    if len(reqs) != expected_requirements:
        raise GateError(f"requirement count mismatch expected={expected_requirements} actual={len(reqs)}")
    req_id_field = "requirement_id" if "requirement_id" in req_fields else ("exact_id" if "exact_id" in req_fields else None)
    if not req_id_field:
        raise GateError("requirement projection ID column missing: expected requirement_id or exact_id")
    req_ids = [row[req_id_field] for row in reqs]
    if any(not value for value in req_ids) or len(set(req_ids)) != len(reqs):
        raise GateError("requirement projection IDs missing/duplicate")

    for path in review.glob("*"):
        if path.is_file() and path.suffix.lower() in {".md", ".csv", ".json", ".txt"}:
            text = path.read_text(encoding="utf-8-sig", errors="replace")
            lower = text.lower()
            for token in PLACEHOLDERS:
                value = token.lower()
                if value == "todo":
                    if re.search(r"\btodo\b(?!\s*=\s*0)", lower):
                        raise GateError(f"placeholder token {token!r} in {path.name}")
                elif value in lower:
                    raise GateError(f"placeholder token {token!r} in {path.name}")

    return {
        "status": "PASS",
        "sourceIdentitySha1": package["sourceSha1"],
        "sourceIdentitySha256": package["sourceSha256"],
        "packagePayloadSha1": package["payloadSha1"],
        "packagePayloadSha256": package["payloadSha256"],
        "runtimeSourceSha": source_head or "NOT_SUPPLIED",
        "manifestFiles": package["manifestFiles"],
        "sha256Entries": len(sha_entries),
        "findings": {
            "total": len(findings),
            "complete": complete,
            "incomplete": incomplete,
            "evidenceReferences": evidence_refs,
        },
        "requirements": len(reqs),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--review-dir", required=True)
    parser.add_argument("--expected-sha")
    parser.add_argument("--source-head")
    parser.add_argument("--expected-requirements", type=int, default=36)
    parser.add_argument("--expected-findings", type=int, default=25)
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        result = verify(
            root,
            Path(args.review_dir),
            args.expected_sha,
            args.source_head,
            args.expected_requirements,
            args.expected_findings,
        )
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    if args.json_output:
        output = Path(args.json_output)
        if not output.is_absolute():
            output = root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
