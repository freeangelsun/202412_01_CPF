import unittest
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "build-cpf-requirement-development-traceability.py"


class RequirementTraceabilityBuilderTest(unittest.TestCase):
    def test_requirement_is_minimum_unit_and_qa_codex_are_untouched(self):
        text = SCRIPT.read_text(encoding="utf-8")
        for token in (
            "acceptance_criteria",
            "scenario_ids",
            "actual_source",
            "actual_call_path",
            "executed_test_runtime_evidence",
            "evidence_proves",
            "uncovered_acceptance",
            "source_resolution",
            "evidence_level",
            "개발GPT_상태",
        ):
            self.assertIn(token, text)
        self.assertNotIn('"QA_검수결과"', text)
        self.assertNotIn('"QA_검수여부"', text)
        self.assertNotIn('"Codex_검수결과"', text)

    def test_shared_evidence_never_auto_completes_requirement(self):
        text = SCRIPT.read_text(encoding="utf-8")
        self.assertIn('"개발GPT_상태": "미완료"', text)
        self.assertIn('"verification_status": "미검증"', text)
        self.assertIn('development_status = "부분 구현" if implementation_profiled else "미검증"', text)
        self.assertIn('"developerComplete": 0', text)

    def test_traceability_and_implementation_evidence_are_separated(self):
        text = SCRIPT.read_text(encoding="utf-8")
        self.assertIn("IMPLEMENTATION_SUBSTITUTE_RUNTIME", text)
        self.assertIn("TRACEABILITY_ONLY", text)
        self.assertIn("implementationEvidenceRequirements", text)
        self.assertIn("traceabilityOnlyRequirements", text)


if __name__ == "__main__":
    unittest.main()
