import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "run-cpf-r4-substitute-validation.py"
CURRENT_HEAD = "02dcb5d45646469f4950cf43c371706e00458616"
OLD_HEAD = "cb305fc5363263c9607e990ba640233c28668f01"


class SubstituteValidationWrapperTest(unittest.TestCase):
    def test_wrapper_covers_required_current_environment_gates(self):
        text = SCRIPT.read_text(encoding="utf-8")
        for token in (
            "PYTHON_GATE_TESTS",
            "FRONTEND_FULL_COMPILE",
            "FRONTEND_API_RUNTIME",
            "FRONTEND_WORKFLOW_RUNTIME",
            "JAVA21_CONTROLLER",
            "JAVA21_DB_LESS",
            "JAVA21_NETWORK",
            "JAVA21_PERSISTENCE",
            "JAVA21_TRANSACTION",
            "JAVA21_RUNTIME_COMMAND",
            "JAVA21_BATCH_ABANDON",
            "JAVA21_AUDIT_MULTI_PROCESS",
            "DB_VENDOR_SEMANTIC_PARITY",
            "WORK_PACKAGE_SOURCE_REVIEW",
            "REQUIREMENT_TRACEABILITY_BUILD",
            "REQUIREMENT_TRACEABILITY_CLOSURE",
        ):
            self.assertIn(token, text)

    def test_wrapper_does_not_claim_exact_environment_completion(self):
        text = SCRIPT.read_text(encoding="utf-8")
        self.assertIn("substitute validation only", text)
        self.assertNotIn("QA_PASS", text)

    def test_baseline_is_required_and_no_stale_default_exists(self):
        text = SCRIPT.read_text(encoding="utf-8")
        self.assertIn('parser.add_argument("--baseline-sha", required=True)', text)
        self.assertNotIn(OLD_HEAD, text)
        result = subprocess.run([sys.executable, str(SCRIPT), "--help"], text=True, capture_output=True)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("--baseline-sha", result.stdout)

    def test_mismatched_source_head_fails_before_any_gate(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            result = subprocess.run(
                [
                    sys.executable, str(SCRIPT),
                    "--source-root", str(root),
                    "--artifact-root", str(root / "artifact"),
                    "--datasets-root", str(root / "datasets"),
                    "--baseline-sha", CURRENT_HEAD,
                    "--source-head", OLD_HEAD,
                    "--phase", "runtime",
                ],
                text=True,
                capture_output=True,
            )
            self.assertNotEqual(0, result.returncode)
            self.assertIn("source HEAD mismatch", result.stderr)


if __name__ == "__main__":
    unittest.main()
