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

    def test_generated_domain_supports_online_and_optional_batch(self) -> None:
        self.assertIn('modules["online"]', self.engine)
        self.assertIn('modules.get("batch", False)', self.engine)
        self.assertIn('cpf-starter-batch', self.engine)
        self.assertIn('batch/src/main/java', self.engine)
        catalog = (self.root / "cpf-tools/generator/contracts/cpf-starter-catalog.json").read_text(encoding="utf-8")
        self.assertIn('"artifactId": "cpf-starter-batch"', catalog)
        self.assertIn('"profileId": "batch"', catalog)

    def test_product_composite_substitutes_every_catalog_public_starter(self) -> None:
        self.assertIn("cpf-tools/generator/contracts/cpf-starter-catalog.json", self.engine)
        self.assertIn("starterCatalog.modules instanceof List", self.engine)
        self.assertIn("row.visibility?.toString() == 'public'", self.engine)
        self.assertIn("dependencySubstitution", self.engine)
        self.assertIn(
            'substitute module(\\"${starter.groupId}:${starter.artifactId}\\") using project(starter.projectPath.toString())',
            self.engine,
        )

    def test_generated_gradle_properties_bound_daemon_resources(self) -> None:
        gate_path = self.root / "cpf-tools/generator/verification/verify-cpf-generator-java-template-compile.py"
        spec = importlib.util.spec_from_file_location("cpf_java_compile_gate_resource_test", gate_path)
        self.assertIsNotNone(spec)
        self.assertIsNotNone(spec.loader)
        gate = importlib.util.module_from_spec(spec)
        sys.modules[spec.name] = gate
        spec.loader.exec_module(gate)
        engine = gate.load_engine(self.root / gate.CANONICAL_ENGINE)
        definition = engine.load_domain_contract(self.root / "cpf-member/gradle.properties")
        rendered = engine.render_gradle_properties(definition, {"cpfVersion": "1.0.0-SNAPSHOT"})
        for token in (
            "org.gradle.jvmargs=-Xms250m -Xmx1000m -XX:MaxMetaspaceSize=256m -Dfile.encoding=UTF-8",
            "org.gradle.workers.max=2",
            "org.gradle.parallel=false",
        ):
            self.assertIn(token, rendered)


if __name__ == "__main__":
    unittest.main()
