#!/usr/bin/env python3
"""Validate CPF's official DB vendor manifest and repository path contracts."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path, PurePosixPath


class GateError(RuntimeError):
    pass


OFFICIAL = ("mariadb", "postgresql", "oracle")
LIFECYCLE_KEYS = (
    "provision", "emptyInstall", "productSeed", "optionalSampleSeed",
    "testSeed", "verify", "migration", "rollback",
)


def load(path: Path) -> dict:
    if not path.is_file():
        raise GateError(f"DB vendor manifest missing: {path}")
    try:
        return json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        raise GateError(f"invalid DB vendor manifest JSON: {exc}") from exc


def safe_relative(value: str, label: str) -> PurePosixPath:
    if not value or "\\" in value:
        raise GateError(f"{label} must be a nonblank repository POSIX path: {value!r}")
    path = PurePosixPath(value)
    if path.is_absolute() or any(part in {"", ".", ".."} for part in path.parts):
        raise GateError(f"{label} escapes or is not canonical: {value!r}")
    return path


def concrete_prefix(path: PurePosixPath) -> PurePosixPath:
    parts = []
    for part in path.parts:
        if "{" in part or "}" in part:
            break
        parts.append(part)
    if not parts:
        raise GateError(f"path has no concrete repository prefix: {path}")
    return PurePosixPath(*parts)


def validate(root: Path, metadata_only: bool) -> dict:
    root = root.resolve()
    path = root / "cpf-tools/db/vendor-pack-manifest.json"
    manifest = load(path)
    official = tuple(str(item).lower() for item in manifest.get("officialVendors") or [])
    supported = tuple(str(item).lower() for item in manifest.get("supportedVendors") or [])
    if official != OFFICIAL:
        raise GateError(f"officialVendors must be exactly {OFFICIAL}: actual={official}")
    if supported != OFFICIAL:
        raise GateError(f"supportedVendors must be exactly {OFFICIAL}: actual={supported}")
    if manifest.get("candidateVendors") not in ([], None):
        raise GateError("candidateVendors must be empty for the official exactly-three policy")
    policy = manifest.get("selectionPolicy") or {}
    if policy.get("sourceTreeMutation") is not False or policy.get("selectedVendorOnly") is not True:
        raise GateError("selectionPolicy must prohibit source mutation and select one vendor")
    generated = manifest.get("generatedDomainRegistration") or {}
    if generated.get("metadataDriven") is not True or generated.get("fixedDomainList") is not False:
        raise GateError("generated domains must be metadata-driven without a fixed domain list")

    vendors = manifest.get("vendors") or {}
    if tuple(vendors.keys()) != OFFICIAL:
        raise GateError(f"vendors object must preserve canonical order {OFFICIAL}: actual={tuple(vendors.keys())}")
    checked_paths: list[str] = []
    for vendor in OFFICIAL:
        entry = vendors[vendor]
        if str(entry.get("status", "")) not in {"미구현", "미검증", "완료", "실패", "재확인 필요", "부분 구현"}:
            raise GateError(f"invalid vendor status: vendor={vendor} status={entry.get('status')!r}")
        required_paths = {
            "pack": entry.get("pack"),
            "vendorRoot": entry.get("vendorRoot"),
            "runtimeRoot": entry.get("runtimeRoot"),
            "domainTemplateRoot": entry.get("domainTemplateRoot"),
        }
        lifecycle = entry.get("lifecycle") or {}
        missing = [key for key in LIFECYCLE_KEYS if not lifecycle.get(key)]
        if missing:
            raise GateError(f"lifecycle paths missing: vendor={vendor} keys={missing}")
        for key in LIFECYCLE_KEYS:
            required_paths[f"lifecycle.{key}"] = lifecycle[key]
        for label, raw in required_paths.items():
            rel = safe_relative(str(raw), f"{vendor}.{label}")
            prefix = concrete_prefix(rel)
            checked_paths.append(str(rel))
            expected_prefix = PurePosixPath("cpf-tools", "db", "vendor", vendor)
            if tuple(prefix.parts[:4]) != tuple(expected_prefix.parts):
                raise GateError(f"vendor path ownership mismatch: vendor={vendor} {label}={rel}")
            resolved = (root / Path(*prefix.parts)).resolve()
            try:
                resolved.relative_to(root)
            except ValueError as exc:
                raise GateError(f"vendor path escapes repository: {vendor}.{label}={rel}") from exc
            if not metadata_only:
                if label in {"vendorRoot", "runtimeRoot", "domainTemplateRoot"}:
                    if not resolved.is_dir():
                        raise GateError(f"required DB directory missing: {vendor}.{label}={prefix}")
                elif "{" not in str(rel) and "}" not in str(rel) and not resolved.exists():
                    raise GateError(f"required DB lifecycle/pack path missing: {vendor}.{label}={rel}")

    return {
        "status": "PASS",
        "official_vendors": list(OFFICIAL),
        "vendor_count": 3,
        "checked_path_count": len(checked_paths),
        "metadata_only": metadata_only,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--metadata-only", action="store_true")
    parser.add_argument("--json-output", type=Path)
    args = parser.parse_args()
    try:
        result = validate(args.root, args.metadata_only)
        if args.json_output:
            out = args.json_output if args.json_output.is_absolute() else args.root.resolve() / args.json_output
            out.parent.mkdir(parents=True, exist_ok=True)
            out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False))
        return 0
    except (GateError, OSError) as exc:
        print(f"CPF DB vendor manifest gate FAILED: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
