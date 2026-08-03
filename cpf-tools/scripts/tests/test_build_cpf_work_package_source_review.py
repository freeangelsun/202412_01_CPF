import csv
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "build-cpf-work-package-source-review.py"


class WorkPackageSourceReviewTest(unittest.TestCase):
    def test_positive_exact_file_and_gate_mapping(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            source = root / "repo"
            (source / "cpf-admin/src/main/java/com/cpf/admin/online").mkdir(parents=True)
            (source / "cpf-admin/src/test/java/com/cpf/admin/online").mkdir(parents=True)
            (source / "cpf-tools/scripts").mkdir(parents=True)
            (source / "cpf-admin/src/main/java/com/cpf/admin/online/OnlineService.java").write_text(
                "class OnlineService { void retry(){ /* audit reconcile */ } }", encoding="utf-8"
            )
            (source / "cpf-admin/src/test/java/com/cpf/admin/online/OnlineServiceTest.java").write_text(
                "class OnlineServiceTest {}", encoding="utf-8"
            )
            (source / "cpf-tools/scripts/verify-cpf-frontend-consumer-closure.py").write_text("print('ok')", encoding="utf-8")
            (source / "cpf-tools/scripts/verify-cpf-operator-trust-boundary.py").write_text("print('ok')", encoding="utf-8")
            execution = root / "execution.csv"
            requirement = root / "requirement.csv"
            scenario = root / "scenario.csv"
            self.write_csv(execution, [{"execution_order":"1","requirement_id":"R-1","work_package_id":"P09-ADM-UI-ONLINE"},{"execution_order":"2","requirement_id":"R-2","work_package_id":"P09-GATE"}])
            self.write_csv(requirement, [
                {"requirement_id":"R-1","requirement":"온라인 retry audit 복구","acceptance_criteria":"retry audit reconcile test","verification_method":"test","function_type":"RETRY","feature":"온라인","capability":"ONLINE","owner_module":"cpf-admin","owner_package":"com.cpf.admin.online","actual_consumer":"operator","change_target":"cpf-admin"},
                {"requirement_id":"R-2","requirement":"gate","acceptance_criteria":"gate test","verification_method":"test","function_type":"PHASE_GATE","feature":"gate","capability":"GATE","owner_module":"repository-wide test ownership","owner_package":"N/A","actual_consumer":"developer","change_target":"cpf-tools"},
            ])
            self.write_csv(scenario, [{"scenario_id":"S-1","linked_requirement_id":"R-1"},{"scenario_id":"S-2","linked_requirement_id":"R-2"}])
            output = root / "review.csv"
            summary = root / "summary.json"
            proc = subprocess.run([sys.executable, str(SCRIPT), "--execution-glob", str(execution), "--requirement-glob", str(requirement), "--scenario-glob", str(scenario), "--source-root", str(source), "--output", str(output), "--summary-output", str(summary), "--start-row", "1", "--expected-requirements", "2", "--expected-work-packages", "2", "--baseline-sha", "a"*40], capture_output=True, text=True)
            self.assertEqual(0, proc.returncode, proc.stderr)
            result = json.loads(summary.read_text(encoding="utf-8"))
            self.assertEqual(2, result["resolvedWorkPackages"])
            rows = list(csv.DictReader(output.open(encoding="utf-8-sig")))
            self.assertIn("OnlineService.java", rows[0]["actual_source_files"])
            self.assertIn("verify-cpf-frontend-consumer-closure.py", rows[1]["actual_source_files"])

    def test_required_aspect_file_is_rescued_beyond_top_sixteen(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            source = root / "repo"
            main = source / "cpf-admin/src/main/java/com/cpf/admin/online"
            test_dir = source / "cpf-admin/src/test/java/com/cpf/admin/online"
            main.mkdir(parents=True)
            test_dir.mkdir(parents=True)
            for index in range(20):
                (main / f"OnlineFeature{index:02d}.java").write_text(
                    f"class OnlineFeature{index:02d} {{ void onlineFeature() {{}} }}", encoding="utf-8"
                )
            (test_dir / "OnlineFeatureContractTest.java").write_text(
                "class OnlineFeatureContractTest { void testContract() {} }", encoding="utf-8"
            )
            execution = root / "execution.csv"
            requirement = root / "requirement.csv"
            scenario = root / "scenario.csv"
            self.write_csv(execution, [{"execution_order":"1","requirement_id":"R-1","work_package_id":"P09-ADM-UI-ONLINE"}])
            self.write_csv(requirement, [{
                "requirement_id":"R-1","requirement":"온라인 기능","acceptance_criteria":"positive negative test",
                "verification_method":"test","function_type":"TEST","feature":"Online Feature",
                "capability":"ONLINE","owner_module":"cpf-admin","owner_package":"com.cpf.admin.online",
                "actual_consumer":"operator","change_target":"cpf-admin"
            }])
            self.write_csv(scenario, [{"scenario_id":"S-1","linked_requirement_id":"R-1"}])
            output = root / "review.csv"
            proc = subprocess.run([sys.executable, str(SCRIPT), "--execution-glob", str(execution),
                "--requirement-glob", str(requirement), "--scenario-glob", str(scenario),
                "--source-root", str(source), "--output", str(output),
                "--summary-output", str(root / "summary.json"), "--start-row", "1",
                "--expected-requirements", "1", "--expected-work-packages", "1",
                "--baseline-sha", "c"*40], capture_output=True, text=True)
            self.assertEqual(0, proc.returncode, proc.stderr)
            row = next(csv.DictReader(output.open(encoding="utf-8-sig")))
            self.assertIn("OnlineFeatureContractTest.java", row["actual_source_files"])
            self.assertNotIn("test", row["uncovered_aspects"].split(";"))

    def test_unresolved_work_package_fails_closed(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            source = root / "repo"
            source.mkdir()
            execution = root / "execution.csv"
            requirement = root / "requirement.csv"
            scenario = root / "scenario.csv"
            self.write_csv(execution, [{"execution_order":"1","requirement_id":"R-1","work_package_id":"P10-NO-SOURCE"}])
            self.write_csv(requirement, [{"requirement_id":"R-1","requirement":"missing","acceptance_criteria":"missing source","verification_method":"test","function_type":"TEST","feature":"missing","capability":"MISSING","owner_module":"unknown","owner_package":"N/A","actual_consumer":"consumer","change_target":"unknown"}])
            self.write_csv(scenario, [{"scenario_id":"S-1","linked_requirement_id":"R-1"}])
            proc = subprocess.run([sys.executable, str(SCRIPT), "--execution-glob", str(execution), "--requirement-glob", str(requirement), "--scenario-glob", str(scenario), "--source-root", str(source), "--output", str(root/"review.csv"), "--summary-output", str(root/"summary.json"), "--start-row", "1", "--expected-requirements", "1", "--expected-work-packages", "1", "--baseline-sha", "b"*40], capture_output=True, text=True)
            self.assertEqual(1, proc.returncode)
            self.assertEqual(["P10-NO-SOURCE"], json.loads((root/"summary.json").read_text())["unresolvedWorkPackages"])

    @staticmethod
    def write_csv(path: Path, rows: list[dict[str, str]]) -> None:
        with path.open("w", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
            writer.writeheader()
            writer.writerows(rows)


if __name__ == "__main__":
    unittest.main()
