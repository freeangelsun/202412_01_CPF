from __future__ import annotations

import hashlib
import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
MODULE_PATH = ROOT / "cpf-tools/db/verification/verify-cpf-db-performance-evidence.py"
SPEC = importlib.util.spec_from_file_location("verify_cpf_db_performance_evidence", MODULE_PATH)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)
POLICY = json.loads((ROOT / "cpf-tools/db/cpf-db-performance-policy.json").read_text(encoding="utf-8"))


def valid_evidence() -> dict:
    return {
        "sourceSha": "a" * 40,
        "resultSha": "a" * 40,
        "vendor": "postgresql",
        "databaseVersion": "17.1",
        "dataScale": "large",
        "startedAt": "2026-08-05T00:00:00Z",
        "endedAt": "2026-08-05T00:01:00Z",
        "sanitized": True,
        "statistics": {
            "statisticsCommand": "ANALYZE",
            "statisticsCompletedAt": "2026-08-05T00:00:10Z",
            "statisticsExitCode": 0
        },
        "queryResults": [
            {
                "queryId": "DB.POINT.001",
                "queryClass": "pointLookup",
                "status": "PASS",
                "latencyMs": 10,
                "examinedRows": 1,
                "indexedAccess": True,
                "planSha256": "c" * 64,
                "bindValuesSanitized": True
            },
            {
                "queryId": "DB.PAGE.001",
                "queryClass": "pagedSearch",
                "status": "PASS",
                "latencyMs": 100,
                "examinedRows": 500,
                "stableSort": True,
                "planSha256": "d" * 64,
                "bindValuesSanitized": True
            },
            {
                "queryId": "DB.CLAIM.001",
                "queryClass": "batchClaim",
                "status": "PASS",
                "latencyMs": 80,
                "examinedRows": 20,
                "lockEvidence": "FOR UPDATE SKIP LOCKED",
                "planSha256": "e" * 64,
                "bindValuesSanitized": True
            },
            {
                "queryId": "DB.RETENTION.001",
                "queryClass": "retentionPreview",
                "status": "PASS",
                "latencyMs": 500,
                "examinedRows": 50000,
                "boundedBatch": True,
                "planSha256": "f" * 64,
                "bindValuesSanitized": True
            }
        ]
    }


class DbPerformanceContractTest(unittest.TestCase):
    def test_valid_representative_evidence_passes(self):
        result = MODULE.evaluate(POLICY, valid_evidence())
        self.assertEqual("PASS", result["status"])
        self.assertEqual(4, result["queryCount"])

    def test_slow_or_unbounded_query_fails_closed(self):
        evidence = valid_evidence()
        evidence["queryResults"][1]["latencyMs"] = 301
        evidence["queryResults"][3]["boundedBatch"] = False
        result = MODULE.evaluate(POLICY, evidence)
        self.assertEqual("FAIL", result["status"])
        self.assertTrue(any("latencyMs exceeds" in reason for reason in result["reasons"]))
        self.assertTrue(any("boundedBatch" in reason for reason in result["reasons"]))

    def test_unknown_or_missing_runtime_result_is_not_promoted(self):
        evidence = valid_evidence()
        evidence["queryResults"][0]["status"] = "UNKNOWN"
        result = MODULE.evaluate(POLICY, evidence)
        self.assertEqual("FAIL", result["status"])
        self.assertTrue(any("must be PASS" in reason for reason in result["reasons"]))

    def test_secret_bearing_evidence_key_is_rejected(self):
        evidence = valid_evidence()
        evidence["databasePassword"] = "not-allowed"
        result = MODULE.evaluate(POLICY, evidence)
        self.assertEqual("FAIL", result["status"])
        self.assertTrue(any("secret-bearing" in reason for reason in result["reasons"]))

    def test_evidence_hash_is_deterministic(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "evidence.json"
            path.write_text(json.dumps(valid_evidence(), sort_keys=True), encoding="utf-8")
            self.assertEqual(hashlib.sha256(path.read_bytes()).hexdigest(), MODULE.sha256(path))

    def test_powershell_consumer_requires_independent_approval_and_confirmations(self):
        text = (ROOT / "cpf-tools/db/tools/invoke-cpf-db-performance-gate.ps1").read_text(encoding="utf-8")
        for token in ["$Operator -eq $ApprovedBy", "ConfirmRepresentativeData", "ConfirmSanitizedEvidence", "ExpectedEvidenceSha256"]:
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
