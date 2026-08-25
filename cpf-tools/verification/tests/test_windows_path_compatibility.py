from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/verification/verify_windows_path_compatibility.py"


def run_gate(tmp_path: Path, *extra: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [sys.executable, str(SCRIPT), "--root", str(tmp_path), *extra],
        text=True,
        capture_output=True,
        check=False,
    )


def make_long_file(tmp_path: Path) -> Path:
    path = tmp_path / ("a" * 100) / ("b" * 95) / "sample.txt"
    path.parent.mkdir(parents=True)
    path.write_text("ok\n", encoding="utf-8")
    return path


def test_project_relative_path_over_200_is_always_fail_closed(tmp_path: Path):
    make_long_file(tmp_path)
    result = run_gate(tmp_path, "--target-root-text", r"C:\\cpf")
    assert result.returncode == 1
    assert "RELATIVE_PATH_TOO_LONG" in result.stdout
    assert "WINDOWS_PATH_COMPATIBILITY=FAIL" in result.stdout


def test_strict_relative_budget_compatibility_switch_keeps_same_fail_closed_policy(tmp_path: Path):
    make_long_file(tmp_path)
    result = run_gate(tmp_path, "--target-root-text", r"C:\\cpf", "--strict-relative-budget")
    assert result.returncode == 1
    assert "RELATIVE_PATH_TOO_LONG" in result.stdout


def test_real_full_path_budget_is_fail_closed(tmp_path: Path):
    make_long_file(tmp_path)
    long_root = r"C:\\" + ("root" * 30)
    result = run_gate(tmp_path, "--target-root-text", long_root)
    assert result.returncode == 1
    assert "FULL_PATH_TOO_LONG" in result.stdout


def test_ephemeral_build_output_is_ignored_but_cpf_tools_build_is_product_source(tmp_path: Path):
    generated = tmp_path / "module" / "build" / ("x" * 180) / "generated.txt"
    generated.parent.mkdir(parents=True)
    generated.write_text("ignored\n", encoding="utf-8")
    product = tmp_path / "cpf-tools" / "build" / "product.txt"
    product.parent.mkdir(parents=True)
    product.write_text("kept\n", encoding="utf-8")
    result = run_gate(tmp_path, "--target-root-text", r"C:\\cpf")
    assert result.returncode == 0, result.stdout + result.stderr
    assert "WINDOWS_PATH_FILES=1" in result.stdout


def test_dated_protected_deliverable_is_allowed_but_still_managed(tmp_path: Path):
    report = tmp_path / "cpf-docs" / "deliverables" / "documentation" / "20260816" / "report.md"
    report.parent.mkdir(parents=True)
    report.write_text("ok\n", encoding="utf-8")
    result = run_gate(tmp_path, "--target-root-text", r"C:\cpf")
    assert result.returncode == 0, result.stdout + result.stderr
    assert "WINDOWS_PATH_FILES=1" in result.stdout
    assert "FORBIDDEN_VERSIONED_DIR" not in result.stdout


def test_dated_directory_outside_protected_deliverables_still_fails(tmp_path: Path):
    source = tmp_path / "cpf-starters" / "data" / "20260816" / "Sample.java"
    source.parent.mkdir(parents=True)
    source.write_text("class Sample {}\n", encoding="utf-8")
    result = run_gate(tmp_path, "--target-root-text", r"C:\cpf")
    assert result.returncode == 1
    assert "FORBIDDEN_VERSIONED_DIR 20260816" in result.stdout


def test_protected_deliverable_remains_subject_to_full_path_budget(tmp_path: Path):
    report = tmp_path / "cpf-docs" / "deliverables" / "documentation" / "20260816" / "report.md"
    report.parent.mkdir(parents=True)
    report.write_text("ok\n", encoding="utf-8")
    long_root = "C:\\" + ("root" * 60)
    result = run_gate(tmp_path, "--target-root-text", long_root)
    assert result.returncode == 1
    assert "FULL_PATH_TOO_LONG" in result.stdout
