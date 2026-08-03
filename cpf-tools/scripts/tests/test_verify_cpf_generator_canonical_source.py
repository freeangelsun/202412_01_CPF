from __future__ import annotations

import importlib.util
from pathlib import Path
import unittest


class CanonicalGeneratorVerifierTest(unittest.TestCase):
    def setUp(self) -> None:
        self.root = Path(__file__).resolve().parents[3]
        self.idempotency = (
            self.root / "cpf-tools/scripts/verify-cpf-generator-idempotency-templates.py"
        ).read_text(encoding="utf-8")
        self.compile_gate = (
            self.root / "cpf-tools/scripts/verify-cpf-generator-java-template-compile.py"
        ).read_text(encoding="utf-8")

    def test_verifiers_read_the_canonical_generator(self) -> None:
        canonical = "cpf-tools/generator/create-domain.ps1"
        self.assertIn(canonical, self.idempotency)
        self.assertIn(canonical, self.compile_gate)
        self.assertNotIn("cpf-tools/scripts/create-domain.ps1').read_text", self.idempotency)
        self.assertNotIn('cpf-tools/scripts/create-domain.ps1"', self.compile_gate)

    def test_compile_gate_covers_both_persistence_profiles(self) -> None:
        self.assertIn('("mybatis", "myBatisRepository")', self.compile_gate)
        self.assertIn('("jdbc", "jdbcRepository")', self.compile_gate)
        self.assertIn("--release", self.compile_gate)
        self.assertIn('"21"', self.compile_gate)


if __name__ == "__main__":
    unittest.main()
