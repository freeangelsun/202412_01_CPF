from __future__ import annotations

import importlib.util
import sys
import unittest
from pathlib import Path

MODULE = Path(__file__).resolve().parents[1] / "tools/simulate-cpf-migration-lifecycle.py"
SPEC = importlib.util.spec_from_file_location("simulate_cpf_migration_lifecycle", MODULE)
assert SPEC and SPEC.loader
SIM = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = SIM
SPEC.loader.exec_module(SIM)


class MigrationLifecycleSimulatorTest(unittest.TestCase):
    def test_partial_failure_blocks_subsequent_migrations(self):
        state = SIM.simulate(["V1", "V2", "V3"], fail_at=1)
        self.assertEqual("APPLIED", state["states"]["V1"])
        self.assertEqual("UNKNOWN", state["states"]["V2"])
        self.assertEqual("PENDING", state["states"]["V3"])
        self.assertTrue(state["blocked"])

    def test_unknown_cannot_be_reapplied_or_rolled_back(self):
        state = SIM.simulate(["V1", "V2"], fail_at=1)
        with self.assertRaisesRegex(ValueError, "UNKNOWN"):
            SIM.reapply(state)
        with self.assertRaisesRegex(ValueError, "UNKNOWN"):
            SIM.rollback(state)

    def test_reconciled_not_applied_can_reapply(self):
        state = SIM.reconcile(SIM.simulate(["V1", "V2", "V3"], fail_at=1), "NOT_APPLIED")
        SIM.reapply(state)
        self.assertTrue(all(value == "APPLIED" for value in state["states"].values()))
        self.assertFalse(state["reconcileRequired"])

    def test_reconciled_applied_is_not_duplicated(self):
        state = SIM.reconcile(SIM.simulate(["V1", "V2", "V3"], fail_at=1), "APPLIED")
        SIM.reapply(state)
        self.assertEqual("RECONCILED_APPLIED", state["states"]["V2"])
        events = [event for event in state["events"] if event["operation"] == "V2" and event["action"] == "REAPPLY"]
        self.assertEqual([], events)

    def test_reverse_rollback_then_checksum_identity_reapply(self):
        state = SIM.simulate(["V1", "V2", "V3"])
        SIM.rollback(state)
        rollback_order = [event["operation"] for event in state["events"] if event["action"] == "ROLLBACK"]
        self.assertEqual(["V3", "V2", "V1"], rollback_order)
        SIM.reapply(state)
        self.assertTrue(all(value == "APPLIED" for value in state["states"].values()))
        for event in state["events"]:
            self.assertEqual(64, len(event["identitySha256"]))

    def test_invalid_failure_index_is_rejected(self):
        with self.assertRaises(ValueError):
            SIM.simulate(["V1"], fail_at=2)


if __name__ == "__main__":
    unittest.main()
