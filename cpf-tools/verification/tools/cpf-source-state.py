#!/usr/bin/env python3
"""Compute deterministic Git-independent CPF source and managed-tree identities.

`source` identifies product bytes (Source/SQL/API/Test/Config/Frontend/docs) while excluding
mutable review/evidence metadata and generated build caches. `managed` covers every repository
file that validation is expected not to mutate, including review/evidence metadata.
`cpf-tools/build/**` is always treated as product source and is never filtered as Gradle output.
"""
from __future__ import annotations

import argparse
import fnmatch
import hashlib
import json
import os
from pathlib import Path

GENERATED_PARTS = {
    "cpf-release", ".cpf-ide",
    ".git", ".gradle", ".idea", ".pytest_cache", "__pycache__", "node_modules",
    "dist", ".vite", "playwright-report", "test-results", "target", "out", "coverage",
}
GENERATED_FILES = {".coverage"}
GENERATED_ROOT_FILES = {"cpf-release.zip"}
GENERATED_FILE_PATTERNS = (
    "*.class",
    "hs_err_pid*.log",
    "replay_pid*.log",
    "java_pid*.hprof",
    "*.hprof",
    "*.stackdump",
)
GENERATED_PATH_MARKERS = (
    "/cpf-docs/governance/development-harness/evidence/platform/current/generated/",
    "/cpf-docs/work/evidence/generated/",  # retired scratch path: identity compatibility only
)
GENERATED_ROOT_PREFIXES = {".vscode/", ".github/modernize/", "logs/"}
CANONICAL_PRODUCT_BIN_PREFIXES = (
    "cpf-batch/control-plane/bin/", "cpf-batch/scheduler/bin/", "cpf-batch/worker/bin/",
    "cpf-batch/agent/bin/", "cpf-batch/center-cut/bin/",
)


def _relative(path: Path, root: Path) -> str:
    return path.relative_to(root).as_posix()


def _is_generated(rel: str) -> bool:
    parts = Path(rel).parts
    if not parts:
        return True
    if any(part in GENERATED_PARTS for part in parts):
        return True
    if rel in GENERATED_ROOT_FILES:
        return True
    if any(rel.startswith(prefix) for prefix in GENERATED_ROOT_PREFIXES):
        return True
    normalized = f"/{rel.strip('/')}/"
    if any(marker in normalized for marker in GENERATED_PATH_MARKERS):
        return True
    if parts[-1] in GENERATED_FILES:
        return True
    if any(fnmatch.fnmatchcase(parts[-1], pattern) for pattern in GENERATED_FILE_PATTERNS):
        return True
    # Gradle/module build outputs are generated, but cpf-tools/build/** is checked-in product source.
    if "build" in parts and not rel.startswith("cpf-tools/build/"):
        return True
    # Eclipse/IDE module-root compiled output is generated, but a literal "bin" segment under a
    # "templates" directory is checked-in product source (customer-facing CLI template scripts
    # meant to be installed into an end user's own bin/ folder, e.g. cpf-tools/release/*/templates/bin/).
    if "bin" in parts and "templates" not in parts and not any(rel.startswith(prefix) for prefix in CANONICAL_PRODUCT_BIN_PREFIXES):
        return True
    return False


def _include(rel: str, scope: str) -> bool:
    if _is_generated(rel):
        return False
    if scope == "source":
        # The repository carries the current bootstrap CLI binary so a fresh clone can execute
        # `cpf` before Gradle/Generator bootstrap. The JAR is reproducibly built from canonical
        # CLI sources and embeds sourceIdentitySha256, therefore source scope must exclude the
        # generated bootstrap binary to avoid a circular digest. Managed scope still protects it.
        if rel in {"cpf-tools/runtime/cli/lib/cpf-cli.jar", "cpf-tools/runtime/cli/lib/cpf-cli.jar.sha256"}:
            return False
        # Review/evidence metadata may contain the computed source identity itself; exclude it from
        # the product-byte identity to avoid a circular digest. Managed scope still protects it.
        if rel.startswith("cpf-docs/work/"):
            return False
        # Deliverables are review/evidence/package metadata. They can embed the computed source
        # identity and package hashes, so including them would make the source digest circular.
        # Product documentation lives outside cpf-docs/deliverables and remains part of source scope.
        if rel.startswith("cpf-docs/deliverables/"):
            return False
        # Current Development Harness authority/evidence is mutable governance metadata.
        # It can embed the current Product Source Identity itself, so source scope must exclude
        # these exact surfaces to keep identity calculation non-circular. Managed scope still
        # protects every byte and detects validation-time drift.
        if rel.startswith("cpf-docs/governance/development-harness/current/"):
            return False
        if rel.startswith("cpf-docs/governance/development-harness/evidence/"):
            return False
        if rel in {
            "cpf-docs/governance/development-harness/HANDOVER.md",
            "cpf-docs/governance/development-harness/SOURCE_IDENTITY.json",
            "cpf-docs/governance/development-harness/OPEN_ISSUES.md",
            "cpf-docs/governance/development-harness/PRODUCT_CONFORMANCE_REPORT.json",
        }:
            return False
    return True


def snapshot(root: Path, scope: str) -> dict:
    root = root.resolve()
    rows: list[dict] = []
    sha1 = hashlib.sha1(usedforsecurity=False)
    sha256 = hashlib.sha256()
    total_bytes = 0
    candidates: list[Path] = []
    for current, dirs, files in os.walk(root):
        current_path = Path(current)
        rel_dir = current_path.relative_to(root).as_posix() if current_path != root else ""
        kept = []
        for name in dirs:
            child_rel = f"{rel_dir}/{name}".lstrip("/")
            # Do not descend into generated/cache trees. cpf-tools/build is canonical source.
            if name in GENERATED_PARTS:
                continue
            if name == "build" and child_rel != "cpf-tools/build":
                continue
            if any(child_rel.startswith(prefix.rstrip("/")) for prefix in GENERATED_ROOT_PREFIXES):
                continue
            kept.append(name)
        dirs[:] = kept
        for name in files:
            path = current_path / name
            rel = _relative(path, root)
            if _include(rel, scope):
                candidates.append(path)

    for path in sorted(candidates, key=lambda p: p.as_posix()):
        rel = _relative(path, root)
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
