#!/usr/bin/env python3
"""Build self-consistent CPF desired-state development package metadata.

Run this only against a desired-state tree (delete-manifest candidates already absent from
that tree). The production working copy itself is not deleted by this tool.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
from datetime import datetime, timezone
from pathlib import Path
from zipfile import ZipFile

BASELINE_EXPECTED_SHA256 = "b5573c0ab545597563846d0fd31e8669e5b7fec6df73393fed70f17b5f0b6850"
REVIEW = Path("cpf-docs/work")
PACKAGE_REL = "cpf-docs/work/PACKAGE_MANIFEST.json"
CHANGE_REL = "cpf-docs/work/CHANGE_MANIFEST.csv"
SUMS_REL = "cpf-docs/work/SHA256SUMS.txt"
SOURCE_IDENTITY_EXCLUSIONS = {
    PACKAGE_REL,
    CHANGE_REL,
    SUMS_REL,
    "cpf-docs/work/QA_FINDING_REVALIDATION.csv",
    "cpf-docs/work/TEST_AND_EVIDENCE.md",
    "cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md",
}
PACKAGE_METADATA_EXCLUSIONS = {PACKAGE_REL, CHANGE_REL, SUMS_REL}
PROTECTED_PREFIXES = (
    "cpf-docs/deliverables/",
    "cpf-docs/guides/",
    "cpf-docs/environment/docker/",
    "cpf-tools/environment/docker-development-test/",
)


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def sha256_zip_entry(zf: ZipFile, name: str) -> str:
    h = hashlib.sha256()
    with zf.open(name) as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def all_files(root: Path) -> list[str]:
    return sorted(p.relative_to(root).as_posix() for p in root.rglob("*") if p.is_file())


def identity(entries: list[tuple[str, str]]) -> tuple[str, str]:
    material = "".join(f"{digest}  {rel}\n" for rel, digest in sorted(entries)).encode("utf-8")
    return hashlib.sha1(material).hexdigest(), hashlib.sha256(material).hexdigest()


def tree_identity(root: Path, excluded: set[str]) -> dict:
    entries = []
    total = 0
    for rel in all_files(root):
        if rel in excluded:
            continue
        target = root / rel
        entries.append((rel, sha256_file(target)))
        total += target.stat().st_size
    sha1, sha256 = identity(entries)
    return {"sha1": sha1, "sha256": sha256, "fileCount": len(entries), "totalBytes": total}


def baseline_inventory(path: Path) -> dict[str, str]:
    if sha256_file(path) != BASELINE_EXPECTED_SHA256:
        raise RuntimeError("baseline ZIP SHA-256 does not match the approved development baseline")
    out = {}
    with ZipFile(path) as zf:
        for info in zf.infolist():
            if info.is_dir():
                continue
            out[info.filename.replace("\\", "/")] = sha256_zip_entry(zf, info.filename)
    if len(out) != 8440:
        raise RuntimeError(f"baseline file count mismatch: {len(out)}")
    return out


def read_delete_manifest(root: Path) -> tuple[list[dict], int]:
    path = root / "cpf-docs/deliverables/DELETE_MANIFEST.csv"
    with path.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))
    seen = set()
    protected = 0
    for row in rows:
        rel = (row.get("path") or "").replace("\\", "/").strip()
        if not rel or rel in seen:
            raise RuntimeError(f"delete manifest missing/duplicate path: {rel!r}")
        seen.add(rel)
        if rel.startswith(PROTECTED_PREFIXES):
            protected += 1
    return rows, protected


def update_finding_identity(root: Path, source: dict) -> None:
    path = root / REVIEW / "QA_FINDING_REVALIDATION.csv"
    with path.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))
        fields = list(rows[0].keys()) if rows else []
    if len(rows) != 25:
        raise RuntimeError(f"QA finding count mismatch: {len(rows)}")
    required = {"source_head", "result_identity"}
    if not required.issubset(fields):
        raise RuntimeError("QA finding ledger identity columns missing")
    result_identity = f"CONTENT_SHA1_{source['sha1']};CONTENT_SHA256_{source['sha256']}"
    for row in rows:
        row["source_head"] = source["sha1"]
        row["result_identity"] = result_identity
    with path.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def update_dynamic_docs(root: Path, source: dict, full_count: int, delete_count: int) -> None:
    test = root / REVIEW / "TEST_AND_EVIDENCE.md"
    text = test.read_text(encoding="utf-8")
    text = re.sub(r"- Desired-state file count: `[^`]+`", f"- Desired-state file count: `{full_count:,}`", text)
    text = re.sub(
        r"- Desired-state content identity: `[^`]+`",
        f"- Source identity (Git-independent, evidence-metadata excluded): `{source['sha256']}`",
        text,
    )
    text = re.sub(r"currently contains `\d+`", f"currently contains `{delete_count}`", text)
    if "Evidence integrity" not in text:
        marker = "| Java source syntax |"
        insertion = f"| Evidence integrity | PASS | 36 developer requirements / 25 QA findings / source identity `{source['sha256'][:16]}...` / file-level package hashes fail-closed |\n"
        text = text.replace(marker, insertion + marker)
    test.write_text(text, encoding="utf-8")

    current = root / REVIEW / "CPF_CURRENT_WORK_REQUEST.md"
    text = current.read_text(encoding="utf-8")
    text = re.sub(
        r"> Desired-state content identity: `[^`]+`",
        f"> Source identity (Git-independent, evidence-metadata excluded): `{source['sha256']}`",
        text,
    )
    text = re.sub(r"> Desired-state files: `[^`]+`", f"> Desired-state files: `{full_count:,}`", text)
    current.write_text(text, encoding="utf-8")


def compute_current_hashes(root: Path, excluded: set[str]) -> dict[str, str]:
    return {rel: sha256_file(root / rel) for rel in all_files(root) if rel not in excluded}


def change_rows(root: Path, baseline: dict[str, str], current: dict[str, str]) -> list[dict]:
    rows = []
    for rel in sorted(set(baseline) | set(current)):
        if rel in {CHANGE_REL, SUMS_REL}:
            continue
        old = baseline.get(rel)
        new = current.get(rel)
        if old == new:
            continue
        if old is None:
            target = root / rel
            rows.append({"path": rel, "change_type": "ADDED", "size_bytes": target.stat().st_size,
                         "sha256": new, "baseline_sha256": ""})
        elif new is None:
            rows.append({"path": rel, "change_type": "DELETED", "size_bytes": "0",
                         "sha256": "", "baseline_sha256": old})
        else:
            target = root / rel
            rows.append({"path": rel, "change_type": "MODIFIED", "size_bytes": target.stat().st_size,
                         "sha256": new, "baseline_sha256": old})
    return rows


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", required=True)
    parser.add_argument("--baseline-zip", required=True)
    parser.add_argument("--generated-at")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    baseline_zip = Path(args.baseline_zip).resolve()
    if not root.is_dir() or not baseline_zip.is_file():
        raise SystemExit("root or baseline ZIP missing")

    delete_rows, protected_delete_count = read_delete_manifest(root)
    if protected_delete_count:
        raise SystemExit(f"protected delete candidate count must be zero, actual={protected_delete_count}")

    baseline = baseline_inventory(baseline_zip)
    full_count = len(all_files(root))
    source = tree_identity(root, SOURCE_IDENTITY_EXCLUSIONS)
    update_finding_identity(root, source)
    update_dynamic_docs(root, source, full_count, len(delete_rows))

    # Source identity must stay stable because identity-bearing evidence files are explicitly excluded.
    source_after = tree_identity(root, SOURCE_IDENTITY_EXCLUSIONS)
    if source_after != source:
        raise RuntimeError("source identity changed while updating identity-bearing evidence metadata")

    # Precompute change counts; PACKAGE_MANIFEST is modified before/after but remains the same change class.
    current_for_counts = compute_current_hashes(root, {CHANGE_REL, SUMS_REL})
    preliminary = change_rows(root, baseline, current_for_counts)
    counts = {kind: sum(row["change_type"] == kind for row in preliminary)
              for kind in ("ADDED", "MODIFIED", "DELETED")}

    payload_paths = [rel for rel in all_files(root) if rel not in PACKAGE_METADATA_EXCLUSIONS]
    files = []
    payload_entries = []
    payload_bytes = 0
    for rel in payload_paths:
        target = root / rel
        digest = sha256_file(target)
        size = target.stat().st_size
        files.append({"path": rel, "sizeBytes": size, "sha256": digest})
        payload_entries.append((rel, digest))
        payload_bytes += size
    payload_sha1, payload_sha256 = identity(payload_entries)

    generated_at = args.generated_at or datetime.now(timezone.utc).isoformat()
    manifest = {
        "schemaVersion": 6,
        "packageType": "DESIRED_STATE_FULL_SOURCE",
        "generatedAt": generated_at,
        "baselineInput": baseline_zip.name,
        "baselineSourceZipSha256": BASELINE_EXPECTED_SHA256,
        "baselineSourceFileCount": len(baseline),
        "gitExactSha": "UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT",
        "sourceIdentity": {
            **source,
            "algorithm": "sorted SHA-256/path inventory hashed with SHA-1 and SHA-256",
            "excludedPaths": sorted(SOURCE_IDENTITY_EXCLUSIONS),
            "purpose": "Stable source identity for developer finding/evidence binding; not a Git SHA.",
        },
        "packageMetadataExcludedPaths": sorted(PACKAGE_METADATA_EXCLUSIONS),
        "packagePayloadIdentity": {
            "sha1": payload_sha1,
            "sha256": payload_sha256,
            "fileCount": len(files),
            "totalBytes": payload_bytes,
            "algorithm": "sorted SHA-256/path inventory hashed with SHA-1 and SHA-256",
        },
        "desiredState": {
            "fullFileCount": len(files) + len(PACKAGE_METADATA_EXCLUSIONS),
            "deleteCandidatesExcluded": len(delete_rows),
            "runtimeGarbageIncluded": 0,
        },
        "changeSummary": counts,
        "developmentStatus": "SOURCE_STATIC_CURRENT_PASS",
        "verificationStatus": "RUNTIME_REVERIFY_REQUIRED",
        "deleteManifest": "cpf-docs/deliverables/DELETE_MANIFEST.csv",
        "deleteManifestCount": len(delete_rows),
        "approvedDeleteCount": sum((row.get("approved") or "").strip().lower() == "true" for row in delete_rows),
        "protectedDeleteCount": protected_delete_count,
        "actualSourceWorkspaceDeletionPerformed": False,
        "desiredStateExcludesDeleteCandidates": True,
        "publicDistribution": {
            "policy": "cpf-tools/release/public/cpf-public-surface-policy.json",
            "publisher": "cpf-tools/release/public/publish-cpf-public-repository.ps1",
            "staticStaging": "PASS files=116",
            "realPush": "NOT_EXECUTED",
        },
        "finalEvidence": "cpf-docs/work/TEST_AND_EVIDENCE.md",
        "handover": "cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md",
        "codexRequest": "cpf-docs/work/current/CODEX_REVALIDATION_REQUEST.md",
        "workResultReviewGenerated": False,
        "workResultReviewPolicy": "Generate only when user explicitly requests it after final development/validation/packaging.",
        "unverifiedRuntime": [
            "Java25 root Gradle configuration/compile/test/build/publication/SBOM",
            "cpf-backoffice-web standalone Gradle build/test",
            "official Node >=22.18 clean frontend build/test",
            "live Oracle/PostgreSQL/MariaDB lifecycle",
            "Redis/Valkey live reconnect/failover",
            "Multi-WAS/process-kill/restart/redeploy",
            "external BZA live HTTP/browser E2E",
            "PowerShell runtime execution",
            "real public Git remote clone/commit/push",
        ],
        "files": files,
    }
    package_path = root / PACKAGE_REL
    package_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    # Change manifest excludes itself and SHA256SUMS to avoid self-reference; PACKAGE_MANIFEST is included.
    current = compute_current_hashes(root, {CHANGE_REL, SUMS_REL})
    rows = change_rows(root, baseline, current)
    change_path = root / CHANGE_REL
    with change_path.open("w", encoding="utf-8", newline="") as f:
        fields = ["path", "change_type", "size_bytes", "sha256", "baseline_sha256"]
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)

    # SHA256SUMS includes all package payload files plus the two generated metadata files, never itself.
    sums_paths = sorted(set(payload_paths) | {PACKAGE_REL, CHANGE_REL})
    sums = "".join(f"{sha256_file(root / rel)}  {rel}\n" for rel in sums_paths)
    (root / SUMS_REL).write_text(sums, encoding="utf-8")

    final_counts = {kind: sum(row["change_type"] == kind for row in rows)
                    for kind in ("ADDED", "MODIFIED", "DELETED")}
    if final_counts != counts:
        # Count classes are expected to remain stable; fail instead of silently publishing a stale summary.
        raise RuntimeError(f"change summary drift preliminary={counts} final={final_counts}")

    print(json.dumps({
        "status": "PASS",
        "fullFileCount": len(all_files(root)),
        "sourceIdentitySha1": source["sha1"],
        "sourceIdentitySha256": source["sha256"],
        "packagePayloadSha256": payload_sha256,
        "changeSummary": final_counts,
        "deleteCandidates": len(delete_rows),
    }, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
