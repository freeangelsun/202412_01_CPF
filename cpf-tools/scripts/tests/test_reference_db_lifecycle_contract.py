from __future__ import annotations

import json
import re
import subprocess
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
CONTRACT = ROOT / "cpf-tools/generator/contracts/reference-edu-schema-ownership-contract.json"
VENDORS = ("mariadb", "postgresql", "oracle")


def ddl_signature(sql: str) -> tuple[frozenset[str], frozenset[str], frozenset[str]]:
    def names(pattern: str) -> frozenset[str]:
        return frozenset(match.upper() for match in re.findall(pattern, sql, re.IGNORECASE))

    return (
        names(r"\bCREATE\s+TABLE\s+([A-Za-z][A-Za-z0-9_$#]*)"),
        names(r"\bCREATE\s+(?:UNIQUE\s+)?INDEX\s+([A-Za-z][A-Za-z0-9_$#]*)"),
        names(r"\bCONSTRAINT\s+([A-Za-z][A-Za-z0-9_$#]*)"),
    )


class ReferenceDbLifecycleContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.contract = json.loads(CONTRACT.read_text(encoding="utf-8"))

    def run_gate(self, relative_script: str) -> subprocess.CompletedProcess[str]:
        return subprocess.run(
            [sys.executable, str(ROOT / relative_script), "--root", str(ROOT)],
            cwd=ROOT,
            text=True,
            capture_output=True,
            check=False,
        )

    def test_generated_verify_has_no_drift(self) -> None:
        result = self.run_gate("cpf-tools/generator/generate-reference-db-verify.py")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_contract_static_gate_passes(self) -> None:
        result = self.run_gate("cpf-tools/scripts/verify-reference-db-lifecycle-contract.py")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_three_vendor_exact_object_name_parity(self) -> None:
        signatures: dict[str, dict[str, tuple[frozenset[str], frozenset[str], frozenset[str]]]] = {}
        for vendor in VENDORS:
            signatures[vendor] = {}
            for pack_name, metadata in self.contract["featurePacks"].items():
                source = ROOT / f"cpf-tools/db/vendor/{vendor}" / metadata["artifacts"]["source"]
                signatures[vendor][pack_name] = ddl_signature(source.read_text(encoding="utf-8"))
        self.assertEqual(signatures["mariadb"], signatures["postgresql"])
        self.assertEqual(signatures["mariadb"], signatures["oracle"])

    def test_three_vendor_verify_is_fail_closed_and_covers_all_objects(self) -> None:
        fail_tokens = {
            "mariadb": "SIGNAL SQLSTATE ''45000''",
            "postgresql": "RAISE EXCEPTION",
            "oracle": "RAISE_APPLICATION_ERROR",
        }
        for vendor in VENDORS:
            for metadata in self.contract["featurePacks"].values():
                base = ROOT / f"cpf-tools/db/vendor/{vendor}"
                source = (base / metadata["artifacts"]["source"]).read_text(encoding="utf-8")
                verify = (base / metadata["artifacts"]["verify"]).read_text(encoding="utf-8").upper()
                expected = set().union(*ddl_signature(source))
                missing = {name for name in expected if name not in verify}
                self.assertFalse(missing, f"{vendor} missing verify objects: {sorted(missing)}")
                self.assertIn(fail_tokens[vendor], verify)
                self.assertIn("CHECK_NAME", verify)
                self.assertIn("PASSED", verify)


if __name__ == "__main__":
    unittest.main()
