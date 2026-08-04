from __future__ import annotations

import csv
import hashlib
import importlib.util
import json
import tempfile
import unittest
from argparse import Namespace
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "build-cpf-full-qa-ledgers.py"


def load():
    spec = importlib.util.spec_from_file_location("full_ledger_builder", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader
    spec.loader.exec_module(module)
    return module


def write_csv(path: Path, fields: list[str], rows: list[dict[str, object]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def index(root: Path, stem: str, part: Path, id_field: str, count: int) -> None:
    with part.open(encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))
    raw = part.read_bytes()
    write_csv(
        root / f"cpf-docs/work/current/{stem}.csv",
        ["part_sequence", "part_path", "part_record_count", "first_record_id", "last_record_id", "size_bytes", "sha256", "logical_record_count"],
        [{
            "part_sequence": 1,
            "part_path": part.relative_to(root).as_posix(),
            "part_record_count": count,
            "first_record_id": rows[0][id_field],
            "last_record_id": rows[-1][id_field],
            "size_bytes": len(raw),
            "sha256": hashlib.sha256(raw).hexdigest(),
            "logical_record_count": count,
        }],
    )


class LedgerBuilderTest(unittest.TestCase):
    SHA = "a" * 40

    def fixture(self):
        temporary = tempfile.TemporaryDirectory()
        root = Path(temporary.name)
        req_fields = [
            "requirement_id", "requirement", "priority", "owner_module", "owner_package",
            "source_basis", "change_target", "actual_consumer", "acceptance_criteria",
            "verification_method", "regression_protection",
        ]
        requirements = [
            {"requirement_id": "CPF-FR-000001", "requirement": "one", "priority": "P0", "owner_module": "cpf-core", "owner_package": "com.cpf.core.api", "source_basis": "s1", "change_target": "c1", "actual_consumer": "x1", "acceptance_criteria": "a1", "verification_method": "v1", "regression_protection": "r1"},
            {"requirement_id": "CPF-FR-000002", "requirement": "two", "priority": "P1", "owner_module": "cpf-admin", "owner_package": "com.cpf.admin.opr", "source_basis": "s2", "change_target": "c2", "actual_consumer": "x2", "acceptance_criteria": "a2", "verification_method": "v2", "regression_protection": "r2"},
        ]
        req_part = root / "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.parts/part.csv"
        write_csv(req_part, req_fields, requirements)
        index(root, "CPF_REQUIREMENT_MASTER", req_part, "requirement_id", 2)

        sc_fields = ["scenario_id", "linked_requirement_id", "work_package_id", "scenario_type", "title", "preconditions", "steps", "expected_result", "failure_criteria", "environment", "topology", "required_evidence"]
        scenarios = [
            {"scenario_id": "CPF-SC-000001", "linked_requirement_id": "CPF-FR-000001", "work_package_id": "WP-1", "scenario_type": "POSITIVE", "title": "s1", "preconditions": "p", "steps": "s", "expected_result": "e", "failure_criteria": "f", "environment": "java21", "topology": "single", "required_evidence": "log"},
            {"scenario_id": "CPF-SC-000002", "linked_requirement_id": "CPF-FR-000001", "work_package_id": "WP-1", "scenario_type": "NEGATIVE", "title": "s2", "preconditions": "p", "steps": "s", "expected_result": "e", "failure_criteria": "f", "environment": "java21", "topology": "single", "required_evidence": "log"},
            {"scenario_id": "CPF-SC-000003", "linked_requirement_id": "CPF-FR-000002", "work_package_id": "WP-2", "scenario_type": "BOUNDARY", "title": "s3", "preconditions": "p", "steps": "s", "expected_result": "e", "failure_criteria": "f", "environment": "java21", "topology": "single", "required_evidence": "log"},
        ]
        sc_part = root / "cpf-docs/work/current/CPF_SCENARIO_MASTER.parts/part.csv"
        write_csv(sc_part, sc_fields, scenarios)
        index(root, "CPF_SCENARIO_MASTER", sc_part, "scenario_id", 3)

        ex_fields = ["execution_order", "requirement_id", "work_package_id"]
        executions = [
            {"execution_order": "00-00000001", "requirement_id": "CPF-FR-000001", "work_package_id": "WP-1"},
            {"execution_order": "00-00000002", "requirement_id": "CPF-FR-000002", "work_package_id": "WP-2"},
        ]
        ex_part = root / "cpf-docs/work/current/CPF_EXECUTION_SEQUENCE.parts/part.csv"
        write_csv(ex_part, ex_fields, executions)
        index(root, "CPF_EXECUTION_SEQUENCE", ex_part, "execution_order", 2)
        args = Namespace(
            root=str(root), expected_sha=self.SHA, generated_at="2026-08-04T04:00:00+09:00",
            updated_by="QA GPT", requirement_output="cpf-docs/work/current/REQUIREMENT_STATUS.csv",
            scenario_output="cpf-docs/work/current/SCENARIO_STATUS.csv", json_output=None,
        )
        return temporary, root, args

    def test_initial_generation_has_full_coverage_and_unreviewed_defaults(self):
        temporary, root, args = self.fixture(); self.addCleanup(temporary.cleanup)
        result = load().build(args)
        self.assertEqual(2, result["requirements"])
        self.assertEqual(3, result["scenarios"])
        _, requirements = load().read_csv(root / args.requirement_output)
        self.assertEqual(["CPF-FR-000001", "CPF-FR-000002"], [row["requirement_id"] for row in requirements])
        self.assertEqual("CPF-SC-000001;CPF-SC-000002", requirements[0]["scenario_ids"])
        self.assertEqual("아니오", requirements[0]["QA_검수여부"])
        self.assertEqual("미검증", requirements[0]["verification_status"])

    def test_existing_qa_fields_are_preserved(self):
        temporary, root, args = self.fixture(); self.addCleanup(temporary.cleanup)
        module = load(); module.build(args)
        path = root / args.requirement_output
        fields, rows = module.read_csv(path)
        rows[0]["QA_검수여부"] = "예"
        rows[0]["QA_검수결과"] = "통과"
        rows[0]["QA_검수evidence"] = "evidence.md"
        rows[0]["state_revision"] = "7"
        write_csv(path, fields, rows)
        module.build(args)
        _, refreshed = module.read_csv(path)
        self.assertEqual("예", refreshed[0]["QA_검수여부"])
        self.assertEqual("통과", refreshed[0]["QA_검수결과"])
        self.assertEqual("evidence.md", refreshed[0]["QA_검수evidence"])
        self.assertEqual("7", refreshed[0]["state_revision"])

    def test_regeneration_is_byte_stable(self):
        temporary, root, args = self.fixture(); self.addCleanup(temporary.cleanup)
        module = load(); module.build(args)
        req = root / args.requirement_output; sc = root / args.scenario_output
        first = (req.read_bytes(), sc.read_bytes())
        module.build(args)
        self.assertEqual(first, (req.read_bytes(), sc.read_bytes()))

    def test_duplicate_master_id_fails_closed(self):
        temporary, root, args = self.fixture(); self.addCleanup(temporary.cleanup)
        part = root / "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.parts/part.csv"
        fields, rows = load().read_csv(part)
        rows[1]["requirement_id"] = rows[0]["requirement_id"]
        write_csv(part, fields, rows)
        index(root, "CPF_REQUIREMENT_MASTER", part, "requirement_id", 2)
        with self.assertRaises(Exception):
            load().build(args)

    def test_unknown_existing_ledger_id_fails_closed(self):
        temporary, root, args = self.fixture(); self.addCleanup(temporary.cleanup)
        module = load(); module.build(args)
        path = root / args.requirement_output
        fields, rows = module.read_csv(path)
        alien = dict(rows[0]); alien["requirement_id"] = "CPF-FR-999999"
        rows.append(alien); write_csv(path, fields, rows)
        with self.assertRaises(Exception):
            module.build(args)

    def test_scenario_work_package_drift_fails_closed(self):
        temporary, root, args = self.fixture(); self.addCleanup(temporary.cleanup)
        part = root / "cpf-docs/work/current/CPF_SCENARIO_MASTER.parts/part.csv"
        fields, rows = load().read_csv(part)
        rows[0]["work_package_id"] = "WRONG"
        write_csv(part, fields, rows)
        index(root, "CPF_SCENARIO_MASTER", part, "scenario_id", 3)
        with self.assertRaises(Exception):
            load().build(args)


if __name__ == "__main__":
    unittest.main()
