from __future__ import annotations

import csv
import subprocess
import sys
from pathlib import Path

SCRIPTS = Path(__file__).parents[1]
SHA = "f" * 40


def write(path: Path, fields: list[str], rows: list[dict[str, object]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def run(script: str, *args: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [sys.executable, str(SCRIPTS / script), *args],
        text=True,
        capture_output=True,
    )


def fixtures(tmp_path: Path):
    requirements = tmp_path / "requirements.csv"
    scenarios = tmp_path / "scenarios.csv"
    # Deliberately non-contiguous/non-numeric-suffix IDs prove IDs are not ordinals.
    requirement_ids = ["CPF-FR-000001", "CPF-FR-000002-A", "CPF-FR-000002-B", "CPF-FR-000004"]
    write(
        requirements,
        ["requirement_id", "baseline_sha"],
        [{"requirement_id": rid, "baseline_sha": SHA} for rid in requirement_ids],
    )
    write(
        scenarios,
        ["scenario_id", "linked_requirement_id", "baseline_sha"],
        [
            {"scenario_id": f"CPF-SC-{index:06d}", "linked_requirement_id": rid, "baseline_sha": SHA}
            for index, rid in enumerate(requirement_ids, 1)
        ],
    )
    return requirements, scenarios, requirement_ids


def test_exact_logical_partition_coverage(tmp_path: Path):
    requirements, scenarios, ids = fixtures(tmp_path)
    plan = tmp_path / "plan.csv"
    write(
        plan,
        [
            "partition_id", "logical_start", "logical_end", "requirement_count",
            "baseline_sha", "first_requirement_id", "last_requirement_id",
        ],
        [
            {"partition_id": "QA-A", "logical_start": 1, "logical_end": 2, "requirement_count": 2,
             "baseline_sha": SHA, "first_requirement_id": ids[0], "last_requirement_id": ids[1]},
            {"partition_id": "QA-B", "logical_start": 3, "logical_end": 4, "requirement_count": 2,
             "baseline_sha": SHA, "first_requirement_id": ids[2], "last_requirement_id": ids[3]},
        ],
    )
    result = run(
        "validate-qa-partition-coverage.py", "--plan", str(plan), "--expected-total", "4",
        "--expected-sha", SHA, "--requirement-ledger", str(requirements),
        "--scenario-ledger", str(scenarios),
    )
    assert result.returncode == 0, result.stdout + result.stderr
    assert '"uniqueRequirementIds": 4' in result.stdout
    assert "IDs were not treated as ordinals" in result.stdout


def test_partition_gap_fails(tmp_path: Path):
    requirements, scenarios, _ = fixtures(tmp_path)
    plan = tmp_path / "plan.csv"
    write(
        plan,
        ["partition_id", "logical_start", "logical_end", "requirement_count", "baseline_sha"],
        [
            {"partition_id": "QA-A", "logical_start": 1, "logical_end": 1, "requirement_count": 1, "baseline_sha": SHA},
            {"partition_id": "QA-B", "logical_start": 3, "logical_end": 4, "requirement_count": 2, "baseline_sha": SHA},
        ],
    )
    result = run(
        "validate-qa-partition-coverage.py", "--plan", str(plan), "--expected-total", "4",
        "--expected-sha", SHA, "--requirement-ledger", str(requirements),
        "--scenario-ledger", str(scenarios),
    )
    assert result.returncode == 1
    assert "gap/overlap" in result.stdout


def test_boundary_id_mismatch_fails(tmp_path: Path):
    requirements, scenarios, _ = fixtures(tmp_path)
    plan = tmp_path / "plan.csv"
    write(
        plan,
        ["partition_id", "logical_start", "logical_end", "requirement_count", "baseline_sha", "first_requirement_id"],
        [{"partition_id": "QA-A", "logical_start": 1, "logical_end": 4, "requirement_count": 4,
          "baseline_sha": SHA, "first_requirement_id": "CPF-FR-WRONG"}],
    )
    result = run(
        "validate-qa-partition-coverage.py", "--plan", str(plan), "--expected-total", "4",
        "--expected-sha", SHA, "--requirement-ledger", str(requirements),
        "--scenario-ledger", str(scenarios),
    )
    assert result.returncode == 1
    assert "first Requirement mismatch" in result.stdout


def test_scenario_partition_mismatch_fails(tmp_path: Path):
    requirements, scenarios, ids = fixtures(tmp_path)
    # Add explicit, wrong ownership to one scenario.
    rows = []
    with scenarios.open(encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))
    rows[0]["partition_id"] = "QA-B"
    write(scenarios, ["scenario_id", "linked_requirement_id", "baseline_sha", "partition_id"], rows)
    plan = tmp_path / "plan.csv"
    write(
        plan,
        ["partition_id", "logical_start", "logical_end", "requirement_count", "baseline_sha"],
        [
            {"partition_id": "QA-A", "logical_start": 1, "logical_end": 2, "requirement_count": 2, "baseline_sha": SHA},
            {"partition_id": "QA-B", "logical_start": 3, "logical_end": 4, "requirement_count": 2, "baseline_sha": SHA},
        ],
    )
    result = run(
        "validate-qa-partition-coverage.py", "--plan", str(plan), "--expected-total", "4",
        "--expected-sha", SHA, "--requirement-ledger", str(requirements),
        "--scenario-ledger", str(scenarios),
    )
    assert result.returncode == 1
    assert "scenario partition mismatch" in result.stdout


def test_query_count_and_duplicate_detection(tmp_path: Path):
    ledger = tmp_path / "query.csv"
    write(ledger, ["query_id"], [{"query_id": "Q-1"}, {"query_id": "Q-2"}])
    ok = run("validate-qa-query-count.py", "--query-file", str(ledger), "--expected-count", "2")
    assert ok.returncode == 0
    write(ledger, ["query_id"], [{"query_id": "Q-1"}, {"query_id": "Q-1"}])
    bad = run("validate-qa-query-count.py", "--query-file", str(ledger), "--expected-count", "2")
    assert bad.returncode == 1 and "duplicate" in bad.stdout
