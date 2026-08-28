from __future__ import annotations

import importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
VERIFIER = ROOT / "cpf-tools/verification/tools/verify-cpf-runtime-utf8-boundaries.py"


def load_module():
    spec = importlib.util.spec_from_file_location("cpf_utf8_boundary", VERIFIER)
    module = importlib.util.module_from_spec(spec)
    assert spec and spec.loader
    spec.loader.exec_module(module)
    return module


def test_actual_repository_utf8_boundary_passes():
    module = load_module()
    result = module.verify(ROOT)
    assert result["status"] == "PASS", result["failures"]
    assert result["redirectedProcessFiles"] > 0


def test_missing_process_output_encoding_fails(tmp_path: Path):
    module = load_module()
    target = tmp_path / "cpf-tools/verification/tools/bad.ps1"
    target.parent.mkdir(parents=True)
    target.write_text("$p=[Diagnostics.ProcessStartInfo]::new();$p.RedirectStandardOutput=$true\n", encoding="utf-8")
    result = module.verify(tmp_path)
    assert result["status"] == "FAIL"
    assert any("PROCESS_STDOUT_ENCODING_MISSING" in item for item in result["failures"])


def test_mojibake_source_fails(tmp_path: Path):
    module = load_module()
    target = tmp_path / "cpf-tools/runtime/tools/bad.ps1"
    target.parent.mkdir(parents=True)
    target.write_text("Write-Host '?ㅼ젣 DB'\n", encoding="utf-8")
    result = module.verify(tmp_path)
    assert result["status"] == "FAIL"
    assert any("MOJIBAKE_SOURCE" in item for item in result["failures"])


def test_start_process_without_child_utf8_contract_fails(tmp_path: Path):
    module = load_module()
    target = tmp_path / "cpf-tools/runtime/tools/bad-start.ps1"
    target.parent.mkdir(parents=True)
    target.write_text(
        "Start-Process -FilePath 'java' -RedirectStandardOutput out.log -RedirectStandardError err.log\n",
        encoding="utf-8",
    )
    result = module.verify(tmp_path)
    assert result["status"] == "FAIL"
    assert any("START_PROCESS_CHILD_UTF8_MISSING" in item for item in result["failures"])


def test_mariadb_restore_without_explicit_charset_fails(tmp_path: Path):
    module = load_module()
    target = tmp_path / "cpf-tools/db/tools/restore.ps1"
    target.parent.mkdir(parents=True)
    target.write_text(
        "$CpfUtf8ChildJavaOptions='-Dfile.encoding=UTF-8'\n"
        "$env:PYTHONUTF8='1';$env:PYTHONIOENCODING='utf-8'\n"
        "Start-Process -FilePath mariadb -RedirectStandardInput dump.sql\n",
        encoding="utf-8",
    )
    result = module.verify(tmp_path)
    assert result["status"] == "FAIL"
    assert any("MARIADB_CLIENT_CHARSET_MISSING" in item for item in result["failures"])
