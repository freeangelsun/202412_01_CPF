import unittest
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "run-cpf-r4-substitute-validation.py"


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


if __name__ == "__main__":
    unittest.main()
