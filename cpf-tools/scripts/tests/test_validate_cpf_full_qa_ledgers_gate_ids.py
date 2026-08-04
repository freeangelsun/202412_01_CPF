from __future__ import annotations

import csv
import subprocess
import sys
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "validate-cpf-full-qa-ledgers.py"
SHA = "f" * 40


def write(path: Path, fields: list[str], rows: list[dict[str, str]]) -> None:
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def test_full_ledger_validator_accepts_phase_gate_requirement_ids(tmp_path: Path) -> None:
    requirements = tmp_path / "requirements.csv"
    scenarios = tmp_path / "scenarios.csv"
    write(
        requirements,
        ["requirement_id", "baseline_sha", "scenario_ids", "QA_검수여부"],
        [
            {
                "requirement_id": "CPF-FR-000001",
                "baseline_sha": SHA,
                "scenario_ids": "CPF-SC-000001",
                "QA_검수여부": "",
            },
            {
                "requirement_id": "CPF-GATE-00",
                "baseline_sha": SHA,
                "scenario_ids": "CPF-SC-000002",
                "QA_검수여부": "",
            },
        ],
    )
    write(
        scenarios,
        ["scenario_id", "linked_requirement_id", "baseline_sha", "QA_검수여부"],
        [
            {
                "scenario_id": "CPF-SC-000001",
                "linked_requirement_id": "CPF-FR-000001",
                "baseline_sha": SHA,
                "QA_검수여부": "",
            },
            {
                "scenario_id": "CPF-SC-000002",
                "linked_requirement_id": "CPF-GATE-00",
                "baseline_sha": SHA,
                "QA_검수여부": "",
            },
        ],
    )

    result = subprocess.run(
        [
            sys.executable,
            str(SCRIPT),
            "--requirements",
            str(requirements),
            "--scenarios",
            str(scenarios),
            "--expected-requirements",
            "2",
            "--expected-scenarios",
            "2",
            "--expected-sha",
            SHA,
        ],
        text=True,
        capture_output=True,
        check=False,
    )

    assert result.returncode == 0, result.stdout + result.stderr
    assert '"status": "PASS"' in result.stdout


def test_full_ledger_validator_rejects_unknown_requirement_id_family(tmp_path: Path) -> None:
    requirements = tmp_path / "requirements.csv"
    scenarios = tmp_path / "scenarios.csv"
    write(
        requirements,
        ["requirement_id", "baseline_sha", "scenario_ids"],
        [{"requirement_id": "CPF-UNKNOWN-01", "baseline_sha": SHA, "scenario_ids": "CPF-SC-000001"}],
    )
    write(
        scenarios,
        ["scenario_id", "linked_requirement_id", "baseline_sha"],
        [{"scenario_id": "CPF-SC-000001", "linked_requirement_id": "CPF-UNKNOWN-01", "baseline_sha": SHA}],
    )

    result = subprocess.run(
        [
            sys.executable,
            str(SCRIPT),
            "--requirements",
            str(requirements),
            "--scenarios",
            str(scenarios),
            "--expected-requirements",
            "1",
            "--expected-scenarios",
            "1",
            "--expected-sha",
            SHA,
        ],
        text=True,
        capture_output=True,
        check=False,
    )

    assert result.returncode == 1
    assert "invalid/duplicate requirement IDs" in result.stdout


def test_full_ledger_validator_scales_linearly_for_large_ledgers(tmp_path: Path) -> None:
    count = 6000
    requirements = tmp_path / "requirements-large.csv"
    scenarios = tmp_path / "scenarios-large.csv"
    requirement_rows = []
    scenario_rows = []
    for index in range(1, count + 1):
        requirement_id = f"CPF-FR-{index:06d}"
        scenario_id = f"CPF-SC-{index:06d}"
        requirement_rows.append(
            {"requirement_id": requirement_id, "baseline_sha": SHA, "scenario_ids": scenario_id}
        )
        scenario_rows.append(
            {"scenario_id": scenario_id, "linked_requirement_id": requirement_id, "baseline_sha": SHA}
        )
    write(requirements, ["requirement_id", "baseline_sha", "scenario_ids"], requirement_rows)
    write(scenarios, ["scenario_id", "linked_requirement_id", "baseline_sha"], scenario_rows)

    result = subprocess.run(
        [
            sys.executable,
            str(SCRIPT),
            "--requirements",
            str(requirements),
            "--scenarios",
            str(scenarios),
            "--expected-requirements",
            str(count),
            "--expected-scenarios",
            str(count),
            "--expected-sha",
            SHA,
        ],
        text=True,
        capture_output=True,
        check=False,
        timeout=15,
    )

    assert result.returncode == 0, result.stdout + result.stderr
    assert '"status": "PASS"' in result.stdout
