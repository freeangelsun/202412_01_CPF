from __future__ import annotations

import hashlib
import importlib.util
import json
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-split-master-dataset.py"
SPEC = importlib.util.spec_from_file_location("split_master_snapshot", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def blob_sha(content: bytes) -> str:
    return hashlib.sha1(f"blob {len(content)}\0".encode("ascii") + content).hexdigest()


def make_fixture(tmp_path: Path) -> tuple[Path, str]:
    expected_sha = "a" * 40
    files = []
    for relative, _, _ in MODULE.INDEXES.values():
        target = tmp_path / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        content = f"fixture:{relative}\n".encode()
        target.write_bytes(content)
        files.append({
            "path": relative,
            "gitBlobSha": blob_sha(content),
            "sha256": hashlib.sha256(content).hexdigest(),
            "sizeBytes": len(content),
        })
    provenance = tmp_path / "provenance.json"
    provenance.write_text(json.dumps({
        "sourceMode": MODULE.PROVENANCE_SOURCE_MODE,
        "repository": "freeangelsun/202412_01_CPF",
        "baselineSha": expected_sha,
        "files": files,
    }), encoding="utf-8")
    return provenance, expected_sha


def test_snapshot_provenance_binds_all_canonical_indexes(tmp_path: Path) -> None:
    provenance, expected_sha = make_fixture(tmp_path)
    head, result = MODULE.load_snapshot_provenance(tmp_path, provenance, expected_sha)
    assert head == expected_sha
    assert result["sourceMode"] == MODULE.PROVENANCE_SOURCE_MODE
    assert len(result["verifiedIndexFiles"]) == 3


def test_snapshot_provenance_rejects_tampered_index(tmp_path: Path) -> None:
    provenance, expected_sha = make_fixture(tmp_path)
    relative = next(iter(MODULE.INDEXES.values()))[0]
    (tmp_path / relative).write_text("tampered", encoding="utf-8")
    with pytest.raises(MODULE.GateError, match="Git blob mismatch"):
        MODULE.load_snapshot_provenance(tmp_path, provenance, expected_sha)


def test_snapshot_provenance_rejects_missing_or_extra_index(tmp_path: Path) -> None:
    provenance, expected_sha = make_fixture(tmp_path)
    data = json.loads(provenance.read_text(encoding="utf-8"))
    data["files"].pop()
    provenance.write_text(json.dumps(data), encoding="utf-8")
    with pytest.raises(MODULE.GateError, match="missing canonical index paths"):
        MODULE.load_snapshot_provenance(tmp_path, provenance, expected_sha)
