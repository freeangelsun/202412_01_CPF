#!/usr/bin/env python3
"""Fail-closed provenance check for one externally-owned CPF validation run."""
from __future__ import annotations

import argparse
import importlib.util
import json
import sys
from pathlib import Path


class ProvenanceError(RuntimeError):
    pass


def _load_source_state(root: Path):
    script = root / "cpf-tools/verification/tools/cpf-source-state.py"
    spec = importlib.util.spec_from_file_location("cpf_runtime_source_state", script)
    if spec is None or spec.loader is None:
        raise ProvenanceError(f"canonical source-state tool unavailable: {script}")
    module = importlib.util.module_from_spec(spec)
    previous = sys.dont_write_bytecode
    sys.dont_write_bytecode = True
    try:
        spec.loader.exec_module(module)
    finally:
        sys.dont_write_bytecode = previous
    return module


def _json(path: Path) -> dict:
    if not path.is_file():
        raise ProvenanceError(f"runtime evidence file missing: {path}")
    value = json.loads(path.read_text(encoding="utf-8-sig"))
    if not isinstance(value, dict):
        raise ProvenanceError(f"runtime evidence must be an object: {path}")
    return value


def verify(root: Path, evidence_dir: Path, source_sha256: str, managed_sha256: str) -> dict:
    root = root.resolve()
    evidence_dir = evidence_dir.resolve()
    if evidence_dir == root or root in evidence_dir.parents:
        raise ProvenanceError("raw runtime evidence directory must be outside the repository")

    recorded_source = _json(evidence_dir / "source-state-before.json")
    recorded_managed = _json(evidence_dir / "managed-state-before.json")
    environment = _json(evidence_dir / "environment.json")
    state = _load_source_state(root)
    current_source = state.snapshot(root, "source")
    current_managed = state.snapshot(root, "managed")

    source_values = {
        str(recorded_source.get("contentSha256", "")).lower(),
        str(environment.get("resultContentSha256", "")).lower(),
        current_source["contentSha256"].lower(),
    }
    if source_values != {source_sha256.lower()}:
        raise ProvenanceError(f"runtime source provenance mismatch: {sorted(source_values)}")

    managed_values = {
        str(recorded_managed.get("contentSha256", "")).lower(),
        current_managed["contentSha256"].lower(),
    }
    if managed_values != {managed_sha256.lower()}:
        raise ProvenanceError(f"runtime managed-state provenance mismatch: {sorted(managed_values)}")

    return {
        "status": "PASS",
        "sourceIdentitySha256": source_sha256.lower(),
        "managedIdentitySha256": managed_sha256.lower(),
        "sourceFiles": current_source["fileCount"],
        "managedFiles": current_managed["fileCount"],
        "evidenceDirectory": str(evidence_dir),
        "repositoryResidentRawEvidence": False,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--evidence-dir", required=True)
    parser.add_argument("--expected-source-sha256", required=True)
    parser.add_argument("--expected-managed-sha256", required=True)
    args = parser.parse_args()
    try:
        result = verify(
            Path(args.root),
            Path(args.evidence_dir),
            args.expected_source_sha256,
            args.expected_managed_sha256,
        )
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
