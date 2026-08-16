#!/usr/bin/env python3
"""Compute deterministic Git-independent CPF source and managed-tree identities.

`source` identifies product bytes (Source/SQL/API/Test/Config/Frontend/docs) while excluding
mutable review/evidence metadata and generated build caches. `managed` covers every repository
file that validation is expected not to mutate, including review/evidence metadata.
`cpf-tools/build/**` is always treated as product source and is never filtered as Gradle output.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

GENERATED_PARTS = {
    ".git", ".gradle", ".idea", ".pytest_cache", "__pycache__", "node_modules",
    "dist", ".vite", "playwright-report", "test-results", "target", "out", "coverage",
}
GENERATED_FILES = {".coverage"}


def _relative(path: Path, root: Path) -> str:
    return path.relative_to(root).as_posix()


def _is_generated(rel: str) -> bool:
    parts = Path(rel).parts
    if not parts:
        return True
    if any(part in GENERATED_PARTS for part in parts):
        return True
    if parts[-1] in GENERATED_FILES:
        return True
    # Gradle/module build outputs are generated, but cpf-tools/build/** is checked-in product source.
    if "build" in parts and not rel.startswith("cpf-tools/build/"):
        return True
    return False


def _include(rel: str, scope: str) -> bool:
    if _is_generated(rel):
        return False
    if scope == "source":
        # Review/evidence metadata may contain the computed source identity itself; exclude it from
        # the product-byte identity to avoid a circular digest. Managed scope still protects it.
        if rel.startswith("cpf-docs/work/"):
            return False
        if rel.startswith("cpf-docs/deliverables/evidence/"):
            return False
    return True


def snapshot(root: Path, scope: str) -> dict:
    root = root.resolve()
    rows: list[dict] = []
    sha1 = hashlib.sha1(usedforsecurity=False)
    sha256 = hashlib.sha256()
    total_bytes = 0
    for path in sorted((p for p in root.rglob("*") if p.is_file()), key=lambda p: p.as_posix()):
        rel = _relative(path, root)
        if not _include(rel, scope):
            continue
        data_hash = hashlib.sha256()
        with path.open("rb") as handle:
            for chunk in iter(lambda: handle.read(1024 * 1024), b""):
                data_hash.update(chunk)
        digest = data_hash.hexdigest()
        size = path.stat().st_size
        line = f"{rel}|{size}|{digest}\n".encode("utf-8")
        sha1.update(line)
        sha256.update(line)
        total_bytes += size
        rows.append({"path": rel, "sizeBytes": size, "sha256": digest})
    return {
        "schemaVersion": 1,
        "scope": scope,
        "identityPolicy": "GIT_INDEPENDENT_CANONICAL_PATH_SIZE_SHA256_LINES",
        "contentSha1": sha1.hexdigest(),
        "contentSha256": sha256.hexdigest(),
        "fileCount": len(rows),
        "totalBytes": total_bytes,
        "files": rows,
    }


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--scope", choices=("source", "managed"), default="source")
    ap.add_argument("--inventory-output")
    ap.add_argument("--summary-output")
    args = ap.parse_args()
    result = snapshot(Path(args.root), args.scope)
    if args.inventory_output:
        target = Path(args.inventory_output)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    summary = {k: v for k, v in result.items() if k != "files"}
    if args.summary_output:
        target = Path(args.summary_output)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
