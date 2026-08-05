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
SCRIPTS = ROOT / "cpf-tools" / "scripts"
DB = ROOT / "cpf-tools" / "db"


def load_module(filename: str, module_name: str):
    spec = importlib.util.spec_from_file_location(module_name, SCRIPTS / filename)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load {filename}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module


PERF = load_module("verify-cpf-db-performance-evidence.py", "cpf_db_perf_evidence")
DS = load_module("verify-cpf-datasource-runtime-evidence.py", "cpf_ds_runtime_evidence")
OBS = load_module("verify-cpf-data-observability-evidence.py", "cpf_data_observability_evidence")
OPS = load_module("verify-cpf-db-operability-evidence.py", "cpf_db_operability_evidence")


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8-sig"))


def performance_evidence() -> dict:
    plan = "c" * 64
    return {
        "sourceSha": "a" * 40,
        "resultSha": "a" * 40,
        "vendor": "mariadb",
        "databaseVersion": "11.4",
        "dataScale": "small",
        "rowCount": 1000,
        "startedAt": "2026-08-05T10:00:00Z",
        "endedAt": "2026-08-05T10:01:00Z",
        "sanitized": True,
        "statistics": {
            "statisticsCommand": "ANALYZE TABLE",
            "statisticsCompletedAt": "2026-08-05T10:00:10Z",
            "statisticsExitCode": 0,
        },
        "queryResults": [
            {"queryId": "Q-POINT", "queryClass": "pointLookup", "status": "PASS", "latencyMs": 5, "examinedRows": 1, "indexedAccess": True, "planSha256": plan, "bindValuesSanitized": True},
            {"queryId": "Q-PAGE", "queryClass": "pagedSearch", "status": "PASS", "latencyMs": 20, "examinedRows": 100, "stableSort": True, "planSha256": plan, "bindValuesSanitized": True},
            {"queryId": "Q-CLAIM", "queryClass": "batchClaim", "status": "PASS", "latencyMs": 25, "examinedRows": 10, "lockEvidence": "SKIP LOCKED", "planSha256": plan, "bindValuesSanitized": True},
            {"queryId": "Q-RET", "queryClass": "retentionPreview", "status": "PASS", "latencyMs": 30, "examinedRows": 100, "boundedBatch": True, "planSha256": plan, "bindValuesSanitized": True},
        ],
    }


def datasource_evidence() -> dict:
    return {
        "vendor": "mariadb",
        "sourceSha": "a" * 40,
        "operationId": "DS-OP-001",
        "reason": "verify production routing policy",
        "operator": "operator-a",
        "approvedBy": "approver-b",
        "sanitized": True,
        "status": "PASS",
        "startedAt": "2026-08-05T10:00:00Z",
        "endedAt": "2026-08-05T10:01:00Z",
        "routing": {
            "writeTarget": "PRIMARY",
            "readTarget": "PRIMARY",
            "readOnly": False,
            "consistency": "STRONG",
            "lagKnown": True,
            "replicaLagMs": 0,
            "freshnessEvidence": "primary route; replica lag 0ms",
            "decisionAuditId": "ROUTE-001",
        },
        "multiDataSource": {
            "owner": "cpf-common",
            "crossOwnerWrite": False,
            "resourceCount": 1,
            "writeOperation": True,
        },
        "connectionPool": {
            "maxPoolSize": 10,
            "instanceCount": 2,
            "databaseConnectionBudget": 100,
            "reservedConnections": 10,
            "connectionTimeoutMs": 3000,
            "validationTimeoutMs": 1000,
            "maxLifetimeMs": 60000,
        },
        "transaction": {
            "isolation": "READ_COMMITTED",
            "commitOutcome": "COMMITTED",
            "retried": False,
            "deadlockDetected": False,
        },
        "timeouts": {"queryTimeoutMs": 10000, "lockTimeoutMs": 1000},
        "slowQuery": {"durationMs": 10, "alertState": "NORMAL", "bindValuesSanitized": True},
        "capacityForecast": {
            "observationDays": 7,
            "forecastHorizonDays": 90,
            "headroomPercent": 30,
            "growthRatePerDay": 0,
            "state": "HEALTHY",
        },
    }


def observability_evidence() -> dict:
    digest = "d" * 64
    return {
        "vendor": "mariadb",
        "sourceSha": "a" * 40,
        "operationId": "OBS-001",
        "reason": "verify lineage and quality contracts",
        "operator": "operator-a",
        "approvedBy": "approver-b",
        "sanitized": True,
        "state": "PASS",
        "startedAt": "2026-08-05T10:00:00Z",
        "endedAt": "2026-08-05T10:01:00Z",
        "lineage": {
            "contractVersion": "LINEAGE_V1",
            "nodes": [
                {"nodeId": "N1", "owner": "cpf-common", "logicalDatabase": "cmnDB", "objectType": "TABLE", "objectName": "source_table", "schemaHash": digest},
                {"nodeId": "N2", "owner": "cpf-common", "logicalDatabase": "cmnDB", "objectType": "TABLE", "objectName": "target_table", "schemaHash": digest},
            ],
            "edges": [
                {"edgeId": "E1", "sourceNodeId": "N1", "targetNodeId": "N2", "operation": "TRANSFORM", "mappingHash": digest}
            ],
        },
        "quality": {
            "contractVersion": "DATA_QUALITY_V1",
            "rules": [
                {"ruleId": "R1", "ruleVersion": "1", "owner": "cpf-common", "queryId": "Q1", "severity": "ERROR", "thresholdType": "MAX_INVALID_COUNT", "thresholdValue": 0}
            ],
            "results": [
                {"ruleId": "R1", "status": "PASS", "invalidCount": 0, "totalCount": 100}
            ],
        },
        "reconciliation": {
            "contractVersion": "RECONCILIATION_V1",
            "comparisons": [
                {"comparisonId": "C1", "leftCount": 100, "rightCount": 100, "mismatchCount": 0, "leftHash": digest, "rightHash": digest}
            ],
        },
    }


def operability_evidence() -> dict:
    return {
        "operationId": "DB-OP-001",
        "capabilityId": "DB-PERF",
        "vendor": "mariadb",
        "environment": "test",
        "topology": "single-instance",
        "operator": "operator-a",
        "reason": "verify database operation envelope",
        "approvalReference": "APR-001",
        "approvedBy": "approver-b",
        "startedAt": "2026-08-05T10:00:00Z",
        "finishedAt": "2026-08-05T10:00:01Z",
        "resultStatus": "SUCCEEDED",
        "reconcileRequired": False,
        "sourceSha": "a" * 40,
        "evidenceSha256": "b" * 64,
        "metrics": {"durationMs": 1000, "affectedRows": 0, "errorCount": 0, "retryCount": 0},
        "trace": {"traceId": "c" * 32, "spanId": "d" * 16},
        "health": {"before": "UP", "after": "UP"},
        "alerts": [],
        "runbookRef": "RUNBOOK-DB-PERF",
        "sanitized": True,
    }


class PerformanceEvidenceTest(unittest.TestCase):
    policy = load_json(DB / "cpf-db-performance-policy.json")

    def test_complete_evidence_passes(self):
        self.assertEqual("PASS", PERF.evaluate(self.policy, performance_evidence())["status"])

    def test_unknown_query_fails_closed(self):
        evidence = performance_evidence()
        evidence["queryResults"][0]["status"] = "UNKNOWN"
        self.assertEqual("FAIL", PERF.evaluate(self.policy, evidence)["status"])

    def test_missing_query_class_coverage_fails(self):
        evidence = performance_evidence()
        evidence["queryResults"] = evidence["queryResults"][:-1]
        result = PERF.evaluate(self.policy, evidence)
        self.assertTrue(any("coverage missing" in reason for reason in result["reasons"]))

    def test_plan_hash_and_sanitized_binds_are_required(self):
        evidence = performance_evidence()
        evidence["queryResults"][0]["planSha256"] = "bad"
        evidence["queryResults"][1]["bindValuesSanitized"] = False
        self.assertEqual("FAIL", PERF.evaluate(self.policy, evidence)["status"])

    def test_time_and_secret_key_fail(self):
        evidence = performance_evidence()
        evidence["startedAt"], evidence["endedAt"] = evidence["endedAt"], evidence["startedAt"]
        evidence["databasePassword"] = "masked"
        result = PERF.evaluate(self.policy, evidence)
        self.assertTrue(any("endedAt" in reason for reason in result["reasons"]))
        self.assertTrue(any("secret-bearing" in reason for reason in result["reasons"]))


class DataSourceEvidenceTest(unittest.TestCase):
    policy = load_json(DB / "cpf-datasource-runtime-policy.json")

    def test_complete_evidence_passes(self):
        self.assertEqual("PASS", DS.evaluate(self.policy, datasource_evidence())["status"])

    def test_unknown_commit_never_passes(self):
        evidence = datasource_evidence()
        evidence["transaction"]["commitOutcome"] = "UNKNOWN"
        self.assertEqual("FAIL", DS.evaluate(self.policy, evidence)["status"])

    def test_unknown_capacity_never_passes(self):
        evidence = datasource_evidence()
        evidence["capacityForecast"]["state"] = "UNKNOWN"
        self.assertEqual("FAIL", DS.evaluate(self.policy, evidence)["status"])

    def test_lagged_replica_must_fallback(self):
        evidence = datasource_evidence()
        evidence["routing"].update({"readTarget": "REPLICA", "readOnly": True, "consistency": "EVENTUAL", "replicaLagMs": 6000})
        result = DS.evaluate(self.policy, evidence)
        self.assertTrue(any("fall back" in reason for reason in result["reasons"]))

    def test_multi_resource_write_requires_compensation(self):
        evidence = datasource_evidence()
        evidence["multiDataSource"]["resourceCount"] = 2
        self.assertEqual("FAIL", DS.evaluate(self.policy, evidence)["status"])

    def test_pool_budget_and_lifetime_fail_closed(self):
        evidence = datasource_evidence()
        evidence["connectionPool"].update({"maxPoolSize": 50, "instanceCount": 3, "databaseConnectionBudget": 100, "reservedConnections": 10, "maxLifetimeMs": 1})
        result = DS.evaluate(self.policy, evidence)
        self.assertGreaterEqual(len(result["reasons"]), 2)


class ObservabilityEvidenceTest(unittest.TestCase):
    policy = load_json(DB / "cpf-data-observability-policy.json")

    def test_complete_evidence_passes(self):
        self.assertEqual("PASS", OBS.evaluate(self.policy, observability_evidence())["status"])

    def test_unknown_state_with_reconcile_plan_still_fails_final_gate(self):
        evidence = observability_evidence()
        evidence["state"] = "UNKNOWN"
        evidence["reconcilePlan"] = "inspect operation ledger"
        self.assertEqual("FAIL", OBS.evaluate(self.policy, evidence)["status"])

    def test_quality_threshold_is_evaluated(self):
        evidence = observability_evidence()
        evidence["quality"]["results"][0]["invalidCount"] = 1
        result = OBS.evaluate(self.policy, evidence)
        self.assertTrue(any("did not pass" in reason for reason in result["reasons"]))

    def test_missing_quality_result_fails(self):
        evidence = observability_evidence()
        evidence["quality"]["results"] = []
        self.assertEqual("FAIL", OBS.evaluate(self.policy, evidence)["status"])

    def test_lineage_unknown_node_and_reconciliation_mismatch_fail(self):
        evidence = observability_evidence()
        evidence["lineage"]["edges"][0]["targetNodeId"] = "MISSING"
        evidence["reconciliation"]["comparisons"][0]["mismatchCount"] = 1
        result = OBS.evaluate(self.policy, evidence)
        self.assertTrue(any("unknown node" in reason for reason in result["reasons"]))
        self.assertTrue(any("must match" in reason for reason in result["reasons"]))


class OperabilityEvidenceTest(unittest.TestCase):
    contract = load_json(DB / "cpf-db-operability-contract.json")

    def test_complete_envelope_passes(self):
        self.assertEqual("PASS", OPS.evaluate(self.contract, operability_evidence())["status"])

    def test_unknown_result_and_unknown_health_fail(self):
        evidence = operability_evidence()
        evidence["resultStatus"] = "UNKNOWN"
        evidence["reconcileRequired"] = True
        evidence["health"]["after"] = "UNKNOWN"
        self.assertEqual("FAIL", OPS.evaluate(self.contract, evidence)["status"])

    def test_separation_of_duties_is_required(self):
        evidence = operability_evidence()
        evidence["approvedBy"] = evidence["operator"]
        self.assertEqual("FAIL", OPS.evaluate(self.contract, evidence)["status"])

    def test_duration_must_match_timestamps(self):
        evidence = operability_evidence()
        evidence["metrics"]["durationMs"] = 100000
        result = OPS.evaluate(self.contract, evidence)
        self.assertTrue(any("durationMs" in reason for reason in result["reasons"]))

    def test_secret_key_and_invalid_capability_fail(self):
        evidence = operability_evidence()
        evidence["databasePassword"] = "masked"
        evidence["capabilityId"] = "DB-UNKNOWN"
        result = OPS.evaluate(self.contract, evidence)
        self.assertTrue(any("secret-bearing" in reason for reason in result["reasons"]))
        self.assertTrue(any("capabilityId" in reason for reason in result["reasons"]))


class CliHashTest(unittest.TestCase):
    def test_hash_mismatch_returns_failure(self):
        with tempfile.TemporaryDirectory() as temp:
            evidence = Path(temp) / "evidence.json"
            evidence.write_text(json.dumps(performance_evidence()), encoding="utf-8")
            completed = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPTS / "verify-cpf-db-performance-evidence.py"),
                    "--policy",
                    str(DB / "cpf-db-performance-policy.json"),
                    "--evidence",
                    str(evidence),
                    "--expected-evidence-sha256",
                    "0" * 64,
                ],
                text=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                check=False,
            )
            self.assertEqual(1, completed.returncode)
            self.assertIn('"status": "FAIL"', completed.stdout)


if __name__ == "__main__":
    unittest.main()
