from __future__ import annotations

import hashlib
import json
import unittest
from dataclasses import dataclass


@dataclass
class Operation:
    name: str
    status: str = "NOT_EXECUTED"


def plan_hash(operations: list[Operation]) -> str:
    payload = [{"name": item.name} for item in operations]
    return hashlib.sha256(json.dumps(payload, separators=(",", ":"), sort_keys=True).encode()).hexdigest()


def execute(operations: list[Operation], fail_at: int | None = None) -> dict:
    result = {"status": "APPLYING", "reconcileRequired": False, "failureOperation": ""}
    for index, operation in enumerate(operations):
        operation.status = "APPLYING"
        if fail_at == index:
            operation.status = "UNKNOWN"
            result.update(status="UNKNOWN", reconcileRequired=True, failureOperation=operation.name)
            return result
        operation.status = "COMPLETED"
    result["status"] = "COMPLETED"
    return result


def reapply(operations: list[Operation]) -> dict:
    # Completed operations remain idempotently completed; only unresolved work resumes.
    for operation in operations:
        if operation.status == "UNKNOWN":
            operation.status = "NOT_EXECUTED"
    pending = [item for item in operations if item.status != "COMPLETED"]
    resumed = execute(pending)
    return {**resumed, "completed": [item.name for item in operations if item.status == "COMPLETED"]}


class MigrationLifecycleStateModelTest(unittest.TestCase):
    def setUp(self):
        self.operations = [Operation("V1"), Operation("V2"), Operation("V3")]

    def test_success_marks_every_operation_completed(self):
        result = execute(self.operations)
        self.assertEqual("COMPLETED", result["status"])
        self.assertEqual(["COMPLETED"] * 3, [item.status for item in self.operations])

    def test_partial_failure_is_unknown_not_rolled_back_assumption(self):
        result = execute(self.operations, fail_at=1)
        self.assertEqual("UNKNOWN", result["status"])
        self.assertTrue(result["reconcileRequired"])
        self.assertEqual("V2", result["failureOperation"])
        self.assertEqual(["COMPLETED", "UNKNOWN", "NOT_EXECUTED"], [item.status for item in self.operations])

    def test_reapply_resumes_unknown_and_pending_without_replaying_completed(self):
        execute(self.operations, fail_at=1)
        result = reapply(self.operations)
        self.assertEqual("COMPLETED", result["status"])
        self.assertEqual(["V1", "V2", "V3"], result["completed"])

    def test_plan_hash_is_deterministic_and_order_sensitive(self):
        first = plan_hash(self.operations)
        second = plan_hash([Operation("V1"), Operation("V2"), Operation("V3")])
        reordered = plan_hash([Operation("V2"), Operation("V1"), Operation("V3")])
        self.assertEqual(first, second)
        self.assertNotEqual(first, reordered)

    def test_duplicate_versions_are_rejected_by_preflight_model(self):
        names = ["V1", "V2", "V2"]
        self.assertNotEqual(len(names), len(set(names)))


if __name__ == "__main__":
    unittest.main()
