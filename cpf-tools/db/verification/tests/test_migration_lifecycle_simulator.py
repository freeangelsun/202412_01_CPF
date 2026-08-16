from __future__ import annotations
import importlib.util
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
P = ROOT / "cpf-tools/db/tools/simulate-cpf-migration-lifecycle.py"
S = importlib.util.spec_from_file_location("migration_sim", P)
assert S and S.loader
M = importlib.util.module_from_spec(S)
sys.modules[S.name] = M
S.loader.exec_module(M)


class MigrationLifecycleSimulatorTest(unittest.TestCase):
    def test_partial_failure_is_unknown_and_reconcile_required(self):
        state = M.simulate(["V1", "V2", "V3"], 1)
        self.assertEqual("APPLIED", state["states"]["V1"])
        self.assertEqual("UNKNOWN", state["states"]["V2"])
        self.assertEqual("PENDING", state["states"]["V3"])
        self.assertTrue(state["reconcileRequired"])
        self.assertTrue(state["blocked"])

    def test_rollback_refuses_unreconciled_unknown(self):
        state = M.simulate(["V1", "V2", "V3"], 2)
        with self.assertRaises(ValueError):
            M.rollback(state)

    def test_reapply_refuses_unreconciled_unknown(self):
        state = M.simulate(["V1", "V2", "V3"], 1)
        with self.assertRaises(ValueError):
            M.reapply(state)

    def test_reconcile_not_applied_then_reapply_only_non_applied_states(self):
        state = M.simulate(["V1", "V2", "V3"], 1)
        M.reconcile(state, "NOT_APPLIED", "V2")
        M.reapply(state)
        self.assertTrue(all(value == "APPLIED" for value in state["states"].values()))
        self.assertFalse(state["reconcileRequired"])
        self.assertFalse(state["blocked"])
        self.assertFalse(any(event["action"] == "REAPPLY" and event["operation"] == "V1" for event in state["events"]))
        self.assertTrue(any(event["action"] == "REAPPLY" and event["operation"] == "V2" for event in state["events"]))
        self.assertTrue(any(event["action"] == "REAPPLY" and event["operation"] == "V3" for event in state["events"]))

    def test_reconcile_applied_then_rollback_is_reverse(self):
        state = M.simulate(["V1", "V2", "V3"], 2)
        M.reconcile(state, "APPLIED", "V3")
        M.rollback(state)
        actions = [event for event in state["events"] if event["action"] == "ROLLBACK"]
        self.assertEqual(["V3", "V2", "V1"], [event["operation"] for event in actions])
        self.assertTrue(all(event["status"] == "ROLLED_BACK" for event in actions))
        self.assertFalse(state["blocked"])

    def test_duplicate_operations_fail_closed(self):
        with self.assertRaises(ValueError):
            M.simulate(["V1", "V1"])

    def test_plan_hash_is_order_sensitive(self):
        self.assertNotEqual(M.plan_hash(["V1", "V2"]), M.plan_hash(["V2", "V1"]))


if __name__ == "__main__":
    unittest.main()
