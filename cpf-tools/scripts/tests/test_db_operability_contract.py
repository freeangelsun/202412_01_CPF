from __future__ import annotations

import copy
import importlib.util
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONTRACT_PATH = ROOT / "cpf-tools/db/cpf-db-operability-contract.json"
NORMALIZER_PATH = ROOT / "cpf-tools/scripts/normalize-cpf-db-operation-evidence.py"
GENERATOR_PATH = ROOT / "cpf-tools/scripts/generate-cpf-db-operations-openapi.py"
OPENAPI_PATH = ROOT / "cpf-tools/db/generated/cpf-db-operations.openapi.json"
POSITIVE_PATH = ROOT / "cpf-tools/db/sample/operability/db-operation-positive.json"
WRAPPER_PATH = ROOT / "cpf-tools/scripts/invoke-cpf-db-operability-gate.ps1"


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    assert spec and spec.loader
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


normalizer = load_module("cpf_db_operation_normalizer", NORMALIZER_PATH)


class DbOperabilityContractTest(unittest.TestCase):
    def setUp(self):
        self.contract = json.loads(CONTRACT_PATH.read_text(encoding="utf-8"))
        self.evidence = json.loads(POSITIVE_PATH.read_text(encoding="utf-8"))

    def assert_invalid(self, evidence: dict, token: str):
        with self.assertRaises(normalizer.ContractError) as caught:
            normalizer.validate(self.contract, evidence)
        self.assertIn(token, str(caught.exception))

    def test_canonical_capabilities_and_vendors_are_exact(self):
        self.assertEqual(["mariadb", "postgresql", "oracle"], self.contract["officialVendors"])
        self.assertEqual(
            {
                "DB-OWNERSHIP", "DB-INSTALL", "DB-FRESH", "DB-MIGRATION", "DB-ROLLBACK",
                "DB-BACKUP", "DB-MULTI-VENDOR", "DB-SQL", "DB-PERF", "DB-MULTI",
                "DATA-LINEAGE", "DATA-RETENTION",
            },
            {item["id"] for item in self.contract["capabilities"]},
        )

    def test_positive_evidence_normalizes(self):
        result = normalizer.validate(self.contract, self.evidence)
        self.assertEqual("SUCCEEDED", result["resultStatus"])
        self.assertEqual("invoke-platform-database-migration.ps1", result["consumer"])
        self.assertRegex(result["contractSha256"], r"^[0-9a-f]{64}$")

    def test_missing_metrics_fails(self):
        evidence = copy.deepcopy(self.evidence)
        del evidence["metrics"]["retryCount"]
        self.assert_invalid(evidence, "metrics.retryCount")

    def test_operator_cannot_approve_own_operation(self):
        evidence = copy.deepcopy(self.evidence)
        evidence["approvedBy"] = evidence["operator"].upper()
        self.assert_invalid(evidence, "must be different")

    def test_secret_bearing_key_is_rejected_recursively(self):
        evidence = copy.deepcopy(self.evidence)
        evidence["metadata"] = {"nested": {"accessToken": "not-allowed"}}
        self.assert_invalid(evidence, "secret-bearing evidence keys")

    def test_unknown_requires_reconcile_and_alert(self):
        evidence = copy.deepcopy(self.evidence)
        evidence["resultStatus"] = "UNKNOWN"
        evidence["health"]["after"] = "UNKNOWN"
        evidence["reconcileRequired"] = False
        self.assert_invalid(evidence, "UNKNOWN requires")
        evidence["reconcileRequired"] = True
        evidence["alerts"] = []
        self.assert_invalid(evidence, "requires at least one alert")

    def test_success_cannot_finish_down(self):
        evidence = copy.deepcopy(self.evidence)
        evidence["health"]["after"] = "DOWN"
        self.assert_invalid(evidence, "successful result")

    def test_control_character_injection_is_rejected(self):
        evidence = copy.deepcopy(self.evidence)
        evidence["reason"] = "approved operation\nCONNECT attacker"
        self.assert_invalid(evidence, "control character")

    def test_openapi_generation_is_deterministic(self):
        with tempfile.TemporaryDirectory() as temp:
            generated = Path(temp) / "openapi.json"
            completed = subprocess.run(
                [sys.executable, str(GENERATOR_PATH), "--contract", str(CONTRACT_PATH), "--output", str(generated)],
                check=False,
                capture_output=True,
                text=True,
            )
            self.assertEqual(0, completed.returncode, completed.stderr)
            self.assertEqual(OPENAPI_PATH.read_bytes(), generated.read_bytes())
        spec = json.loads(OPENAPI_PATH.read_text(encoding="utf-8"))
        self.assertEqual("CROSS_SESSION_REQUIRED", spec["x-cpf-route-consumer-status"])
        self.assertIn("/cpf/db/operations/verify", spec["paths"])

    def test_cli_positive_and_negative_exit_codes(self):
        with tempfile.TemporaryDirectory() as temp:
            output = Path(temp) / "normalized.json"
            positive = subprocess.run(
                [sys.executable, str(NORMALIZER_PATH), "--contract", str(CONTRACT_PATH), "--evidence", str(POSITIVE_PATH), "--output", str(output)],
                check=False,
                capture_output=True,
                text=True,
            )
            self.assertEqual(0, positive.returncode, positive.stderr)
            negative_path = ROOT / "cpf-tools/db/sample/operability/db-operation-negative-unknown-without-reconcile.json"
            negative = subprocess.run(
                [sys.executable, str(NORMALIZER_PATH), "--contract", str(CONTRACT_PATH), "--evidence", str(negative_path), "--output", str(output)],
                check=False,
                capture_output=True,
                text=True,
            )
            self.assertEqual(2, negative.returncode)
            self.assertIn("UNKNOWN requires", negative.stderr)

    def test_wrapper_contains_hash_sod_and_sanitization_guards(self):
        text = WRAPPER_PATH.read_text(encoding="utf-8")
        for token in (
            "ExpectedEvidenceSha256", "Get-FileHash", "ConfirmSanitizedEvidence",
            "independent operator and approver", "Evidence vendor mismatch",
            "Evidence approvalReference mismatch", "$LASTEXITCODE -ne 0",
        ):
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
