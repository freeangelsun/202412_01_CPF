from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
STATE = ROOT / "cpf-tools/verification/tools/cpf-source-state.py"
VERIFY = ROOT / "cpf-tools/verification/tools/verify-cpf-runtime-evidence-provenance.py"


def _state(scope: str) -> dict:
    result = subprocess.run(
        [sys.executable, str(STATE), "--root", str(ROOT), "--scope", scope],
        check=True,
        capture_output=True,
        text=True,
    )
    return json.loads(result.stdout)


def test_external_runtime_evidence_matches_current_source_and_managed_state(tmp_path: Path):
    source = _state("source")
    managed = _state("managed")
    (tmp_path / "source-state-before.json").write_text(json.dumps(source), encoding="utf-8")
    (tmp_path / "managed-state-before.json").write_text(json.dumps(managed), encoding="utf-8")
    (tmp_path / "environment.json").write_text(
        json.dumps({"resultContentSha256": source["contentSha256"]}), encoding="utf-8"
    )

    result = subprocess.run(
        [
            sys.executable,
            str(VERIFY),
            "--root",
            str(ROOT),
            "--evidence-dir",
            str(tmp_path),
            "--expected-source-sha256",
            source["contentSha256"],
            "--expected-managed-sha256",
            managed["contentSha256"],
        ],
        check=False,
        capture_output=True,
        text=True,
    )
    assert result.returncode == 0, result.stdout + result.stderr
    assert json.loads(result.stdout)["repositoryResidentRawEvidence"] is False


def test_repository_resident_raw_evidence_is_rejected():
    result = subprocess.run(
        [
            sys.executable,
            str(VERIFY),
            "--root",
            str(ROOT),
            "--evidence-dir",
            str(ROOT / "cpf-docs"),
            "--expected-source-sha256",
            "0" * 64,
            "--expected-managed-sha256",
            "0" * 64,
        ],
        check=False,
        capture_output=True,
        text=True,
    )
    assert result.returncode == 1
    assert "outside the repository" in result.stdout
