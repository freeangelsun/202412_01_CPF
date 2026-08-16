from __future__ import annotations

import importlib.util
from pathlib import Path
import sys
import unittest


class CanonicalGeneratorVerifierTest(unittest.TestCase):
    def setUp(self) -> None:
        self.root = Path(__file__).resolve().parents[4]
        self.idempotency = (
            self.root / "cpf-tools/generator/verification/verify-cpf-generator-idempotency-templates.py"
        ).read_text(encoding="utf-8")
        self.compile_gate = (
            self.root / "cpf-tools/generator/verification/verify-cpf-generator-java-template-compile.py"
        ).read_text(encoding="utf-8")
        self.generated_javac = (
            self.root / "cpf-tools/verification/nxt3/verify_generated_javac.py"
        ).read_text(encoding="utf-8")
        self.engine = (
            self.root / "cpf-tools/generator/engine/cpf_domain_generator.py"
        ).read_text(encoding="utf-8")

    def test_verifiers_read_the_canonical_generator(self) -> None:
        canonical = "cpf-tools/generator/engine/cpf_domain_generator.py"
        self.assertIn(canonical, self.idempotency)
        self.assertIn(canonical, self.compile_gate)
        self.assertNotIn("cpf-tools/generator/tools/create-domain.ps1').read_text", self.idempotency)
        self.assertNotIn('cpf-tools/generator/tools/create-domain.ps1"', self.compile_gate)

    def test_compile_gate_delegates_to_current_nxt3_regression_outputs(self) -> None:
        self.assertIn("cpf-tools/verification/nxt3/verify_generated_javac.py", self.compile_gate)
        self.assertIn("run_generated_javac", self.compile_gate)
        self.assertIn('result.get("status") != "PASS"', self.compile_gate)
        self.assertNotIn("myBatisRepository", self.compile_gate)
        self.assertNotIn("jdbcRepository", self.compile_gate)
        self.assertNotIn("COMMON_BLOCKS", self.compile_gate)

    def test_non_mybatis_sample_transaction_fails_canonical_preflight(self) -> None:
        gate_path = self.root / "cpf-tools/generator/verification/verify-cpf-generator-java-template-compile.py"
        spec = importlib.util.spec_from_file_location("cpf_java_compile_gate_test", gate_path)
        self.assertIsNotNone(spec)
        self.assertIsNotNone(spec.loader)
        gate = importlib.util.module_from_spec(spec)
        sys.modules[spec.name] = gate
        spec.loader.exec_module(gate)
        engine = gate.load_engine(self.root / gate.CANONICAL_ENGINE)
        detail = gate.verify_unsupported_sample_preflight(self.root, engine)
        self.assertIn("persistence=mybatis", detail)

    def test_nxt3_stubs_cover_current_canonical_engine_api(self) -> None:
        for token in (
            "transactionSequence()",
            "operatorId()",
            "DUPLICATE",
            "DeleteMapping.java",
        ):
            self.assertIn(token, self.generated_javac)

    def test_generated_domain_is_online_only_and_batch_is_separate_capability(self) -> None:
        self.assertNotIn("render_batch_job", self.engine)
        self.assertNotIn("@CpfBatchJob", self.engine)
        self.assertNotIn("localBatchPort", self.engine)
        self.assertIn("Generated Domain은 online 업무 Runtime을 생성", self.engine)
        catalog = (self.root / "cpf-tools/generator/contracts/cpf-starter-catalog.json").read_text(encoding="utf-8")
        self.assertIn('"artifactId": "cpf-starter-batch"', catalog)
        self.assertIn('"profileId": "batch"', catalog)


if __name__ == "__main__":
    unittest.main()
