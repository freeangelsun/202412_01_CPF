from __future__ import annotations

import csv
import hashlib
import importlib.util
import tempfile
import unittest
from argparse import Namespace
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-full-qa-completion.py"
HEAD = "02dcb5d45646469f4950cf43c371706e00458616"


def load():
    spec = importlib.util.spec_from_file_location("full_qa_gate", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_csv(path: Path, fields: list[str], rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_index(root: Path, stem: str, id_field: str, rows: list[dict[str, str]]) -> None:
    part = root / f"cpf-docs/work/current/{stem}.parts/{stem}_PART_001.csv"
    write_csv(part, list(rows[0]), rows)
    index_fields = [
        "part_sequence", "part_path", "part_record_count", "first_record_id",
        "last_record_id", "size_bytes", "sha256", "logical_record_count",
    ]
    write_csv(
        root / f"cpf-docs/work/current/{stem}.csv",
        index_fields,
        [{
            "part_sequence": "1",
            "part_path": part.relative_to(root).as_posix(),
            "part_record_count": str(len(rows)),
            "first_record_id": rows[0][id_field],
            "last_record_id": rows[-1][id_field],
            "size_bytes": str(part.stat().st_size),
            "sha256": sha256(part),
            "logical_record_count": str(len(rows)),
        }],
    )


class FullQaCompletionGateTest(unittest.TestCase):
    def fixture(self):
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        requirements = [
            {"requirement_id": "CPF-FR-000001", "acceptance_criteria": "A1"},
            {"requirement_id": "CPF-FR-000002", "acceptance_criteria": "A2"},
        ]
        scenarios = [
            {"scenario_id": "CPF-SC-000001", "linked_requirement_id": "CPF-FR-000001", "scenario_type": "POSITIVE"},
            {"scenario_id": "CPF-SC-000002", "linked_requirement_id": "CPF-FR-000001", "scenario_type": "NEGATIVE"},
            {"scenario_id": "CPF-SC-000003", "linked_requirement_id": "CPF-FR-000002", "scenario_type": "BOUNDARY"},
        ]
        execution = [
            {"execution_order": "00-00000001", "requirement_id": "CPF-FR-000001", "work_package_id": "WP-1"},
            {"execution_order": "00-00000002", "requirement_id": "CPF-FR-000002", "work_package_id": "WP-2"},
        ]
        write_index(root, "CPF_REQUIREMENT_MASTER", "requirement_id", requirements)
        write_index(root, "CPF_SCENARIO_MASTER", "scenario_id", scenarios)
        write_index(root, "CPF_EXECUTION_SEQUENCE", "execution_order", execution)

        evidence = root / "cpf-docs/evidence/full-qa.log"
        evidence.parent.mkdir(parents=True, exist_ok=True)
        evidence.write_text("PASS exact-head evidence\n", encoding="utf-8")
        evidence_hash = sha256(evidence)

        requirement_fields = sorted(load().REQUIREMENT_LEDGER_REQUIRED)
        requirement_rows = []
        for requirement, order, work_package, scenario_ids in (
            ("CPF-FR-000001", "00-00000001", "WP-1", "CPF-SC-000001;CPF-SC-000002"),
            ("CPF-FR-000002", "00-00000002", "WP-2", "CPF-SC-000003"),
        ):
            row = {field: "" for field in requirement_fields}
            row.update({
                "requirement_id": requirement,
                "execution_order": order,
                "work_package_id": work_package,
                "acceptance_criteria": "all acceptance",
                "scenario_ids": scenario_ids,
                "owner_module": "cpf-core",
                "owner_package": "com.cpf.core.api",
                "source_paths": "cpf-core/src/main/java/A.java",
                "actual_consumer": "consumer",
                "call_path": "controller->service->port",
                "verification_method": "unit+runtime",
                "verification_level": "runtime",
                "verified_acceptance": "all",
                "unverified_acceptance": "",
                "QA_검수여부": "예",
                "QA_검수결과": "통과",
                "QA_검수evidence": "cpf-docs/evidence/full-qa.log",
                "QA_재개발요청여부": "아니오",
                "개발GPT_상태": "완료",
                "개발GPT_자체검수상태": "완료",
                "Codex_검수보완상태": "완료",
                "development_status": "완료",
                "verification_status": "완료",
                "baseline_sha": HEAD,
                "evidence_path": "cpf-docs/evidence/full-qa.log",
                "evidence_sha256": evidence_hash,
                "open_issue": "",
                "next_action": "",
                "state_revision": "1",
                "updated_at": "2026-08-04T04:00:00+09:00",
                "updated_by": "QA GPT",
            })
            requirement_rows.append(row)
        write_csv(root / "cpf-docs/work/current/REQUIREMENT_STATUS.csv", requirement_fields, requirement_rows)

        scenario_fields = sorted(load().SCENARIO_LEDGER_REQUIRED)
        scenario_rows = []
        for scenario, requirement, work_package, scenario_type in (
            ("CPF-SC-000001", "CPF-FR-000001", "WP-1", "POSITIVE"),
            ("CPF-SC-000002", "CPF-FR-000001", "WP-1", "NEGATIVE"),
            ("CPF-SC-000003", "CPF-FR-000002", "WP-2", "BOUNDARY"),
        ):
            row = {field: "" for field in scenario_fields}
            row.update({
                "scenario_id": scenario,
                "linked_requirement_id": requirement,
                "work_package_id": work_package,
                "scenario_type": scenario_type,
                "expected_result": "expected",
                "failure_criteria": "failure",
                "verification_level": "runtime",
                "QA_검수여부": "예",
                "QA_검수결과": "통과",
                "baseline_sha": HEAD,
                "evidence_path": "cpf-docs/evidence/full-qa.log",
                "evidence_sha256": evidence_hash,
                "unverified_scope": "",
                "state_revision": "1",
                "updated_at": "2026-08-04T04:00:00+09:00",
                "updated_by": "QA GPT",
            })
            scenario_rows.append(row)
        write_csv(root / "cpf-docs/work/current/SCENARIO_STATUS.csv", scenario_fields, scenario_rows)
        return temp, root

    def args(self, root: Path, mode: str = "product-pass") -> Namespace:
        return Namespace(
            root=str(root),
            expected_sha=HEAD,
            mode=mode,
            requirement_ledger="cpf-docs/work/current/REQUIREMENT_STATUS.csv",
            scenario_ledger="cpf-docs/work/current/SCENARIO_STATUS.csv",
            json_output=None,
        )

    def test_product_pass_fixture(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        result = load().verify(self.args(root))
        self.assertEqual("PASS", result["status"])
        self.assertEqual(2, result["requirements"]["total"])
        self.assertEqual(3, result["scenarios"]["total"])

    def test_missing_requirement_status_row_fails(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        path = root / "cpf-docs/work/current/REQUIREMENT_STATUS.csv"
        fields, rows = load().read_csv(path)
        write_csv(path, fields, rows[:1])
        with self.assertRaises(Exception) as failure:
            load().verify(self.args(root))
        self.assertIn("coverage mismatch", str(failure.exception))

    def test_stale_sha_fails(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        path = root / "cpf-docs/work/current/SCENARIO_STATUS.csv"
        fields, rows = load().read_csv(path)
        rows[0]["baseline_sha"] = "cb305fc5363263c9607e990ba640233c28668f01"
        write_csv(path, fields, rows)
        with self.assertRaises(Exception) as failure:
            load().verify(self.args(root))
        self.assertIn("stale baseline SHA", str(failure.exception))

    def test_evidence_hash_mismatch_fails(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / "cpf-docs/evidence/full-qa.log").write_text("tampered\n", encoding="utf-8")
        with self.assertRaises(Exception) as failure:
            load().verify(self.args(root))
        self.assertIn("evidence hash mismatch", str(failure.exception))

    def test_campaign_complete_allows_failed_requirement_but_product_pass_rejects(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        path = root / "cpf-docs/work/current/REQUIREMENT_STATUS.csv"
        fields, rows = load().read_csv(path)
        rows[0]["QA_검수결과"] = "미통과"
        rows[0]["development_status"] = "부분 구현"
        rows[0]["verification_status"] = "실패"
        rows[0]["개발GPT_상태"] = "재개발 요청"
        rows[0]["개발GPT_자체검수상태"] = "재검수 요청"
        rows[0]["Codex_검수보완상태"] = "미완료"
        rows[0]["QA_재개발요청여부"] = "예"
        rows[0]["open_issue"] = "defect"
        rows[0]["next_action"] = "rework"
        write_csv(path, fields, rows)
        campaign = load().verify(self.args(root, "campaign-complete"))
        self.assertEqual(1, campaign["requirements"]["failed"])
        with self.assertRaises(Exception):
            load().verify(self.args(root, "product-pass"))

    def test_scenario_set_mismatch_fails(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        path = root / "cpf-docs/work/current/REQUIREMENT_STATUS.csv"
        fields, rows = load().read_csv(path)
        rows[0]["scenario_ids"] = "CPF-SC-000001"
        write_csv(path, fields, rows)
        with self.assertRaises(Exception) as failure:
            load().verify(self.args(root))
        self.assertIn("scenario set mismatch", str(failure.exception))


if __name__ == "__main__":
    unittest.main()
