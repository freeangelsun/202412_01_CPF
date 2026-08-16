import csv
import json
import tempfile
import unittest
from pathlib import Path
import importlib.util

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-development-traceability-closure.py"
SPEC = importlib.util.spec_from_file_location("traceability_closure", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


class DevelopmentTraceabilityClosureTest(unittest.TestCase):
    def test_positive_individual_traceability(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            evidence = root / "evidence.log"
            evidence.write_text("PASS", encoding="utf-8")
            requirement = root / "requirements.csv"
            work_package = root / "work-packages.csv"
            review = root / "source-review.csv"
            sha = "a" * 40
            row = self.row(sha, evidence.name)
            self.write(requirement, [row])
            self.write(work_package, [{"work_package_id":"WP-1","requirement_count":"1"}])
            self.write(review, [{"work_package_id":"WP-1","source_resolution":"EXACT_SNAPSHOT_FILES"}])
            result = MODULE.verify(root, requirement, work_package, review, 1, 1, sha)
            self.assertEqual("PASS", result["status"])
            self.assertEqual(1, result["traceabilityOnlyRows"])

    def test_missing_evidence_fails(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            requirement = root / "requirements.csv"
            work_package = root / "work-packages.csv"
            review = root / "source-review.csv"
            sha = "b" * 40
            self.write(requirement, [self.row(sha, "missing.log")])
            self.write(work_package, [{"work_package_id":"WP-1","requirement_count":"1"}])
            self.write(review, [{"work_package_id":"WP-1","source_resolution":"EXACT_SNAPSHOT_FILES"}])
            with self.assertRaises(MODULE.GateError):
                MODULE.verify(root, requirement, work_package, review, 1, 1, sha)

    def test_qa_owned_column_is_rejected(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            evidence = root / "evidence.log"
            evidence.write_text("PASS", encoding="utf-8")
            requirement = root / "requirements.csv"
            work_package = root / "work-packages.csv"
            review = root / "source-review.csv"
            sha = "c" * 40
            row = self.row(sha, evidence.name)
            row["QA_검수결과"] = "통과"
            self.write(requirement, [row])
            self.write(work_package, [{"work_package_id":"WP-1","requirement_count":"1"}])
            self.write(review, [{"work_package_id":"WP-1","source_resolution":"EXACT_SNAPSHOT_FILES"}])
            with self.assertRaises(MODULE.GateError):
                MODULE.verify(root, requirement, work_package, review, 1, 1, sha)

    @staticmethod
    def row(sha: str, evidence: str) -> dict[str, str]:
        return {
            "execution_order":"1","requirement_id":"R-1","work_package_id":"WP-1","requirement":"requirement",
            "acceptance_criteria":"criteria","verification_method":"method","scenario_count":"1","scenario_ids":"S-1",
            "scenario_expected_results":"expected","scenario_failure_criteria":"failure","actual_consumer":"consumer",
            "actual_source":"module/File.java","actual_call_path":"consumer -> source","source_resolution":"EXACT_SNAPSHOT_FILES",
            "evidence_level":"TRACEABILITY_ONLY","executed_test_runtime_evidence":evidence,"evidence_proves":"traceability only",
            "uncovered_acceptance":"runtime missing","development_status":"미검증","verification_status":"미검증",
            "개발GPT_상태":"미완료","개발GPT_미완료사유":"runtime missing","개발GPT_실행및검증":"run command",
            "개발GPT_evidence":evidence,"verifiedAgainstSha":sha,
        }

    @staticmethod
    def write(path: Path, rows: list[dict[str, str]]) -> None:
        with path.open("w", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
            writer.writeheader()
            writer.writerows(rows)


if __name__ == "__main__":
    unittest.main()
