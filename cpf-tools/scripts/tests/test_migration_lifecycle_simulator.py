from __future__ import annotations
import importlib.util,sys,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
P=ROOT/'cpf-tools/scripts/simulate-cpf-migration-lifecycle.py'
S=importlib.util.spec_from_file_location('migration_sim',P);assert S and S.loader
M=importlib.util.module_from_spec(S);sys.modules[S.name]=M;S.loader.exec_module(M)
class MigrationLifecycleSimulatorTest(unittest.TestCase):
 def test_partial_failure_is_unknown_and_reconcile_required(self):
  s=M.simulate(['V1','V2','V3'],1);self.assertEqual('COMPLETED',s['states']['V1']);self.assertEqual('UNKNOWN',s['states']['V2']);self.assertEqual('PENDING',s['states']['V3']);self.assertTrue(s['reconcileRequired'])
 def test_rollback_is_reverse_and_never_assumes_unknown_rollback(self):
  s=M.rollback(M.simulate(['V1','V2','V3'],2));actions=[x for x in s['events'] if x['action']=='ROLLBACK'];self.assertEqual(['V3','V2','V1'],[x['operation'] for x in actions]);self.assertEqual('SKIPPED_UNKNOWN',actions[0]['status']);self.assertTrue(s['reconcileRequired'])
 def test_reapply_completes_only_non_completed_states(self):
  s=M.reapply(M.simulate(['V1','V2','V3'],1));self.assertTrue(all(v=='COMPLETED' for v in s['states'].values()));self.assertFalse(s['reconcileRequired']);self.assertFalse(any(e['action']=='REAPPLY' and e['operation']=='V1' for e in s['events']))
 def test_duplicate_operations_fail_closed(self):
  with self.assertRaises(ValueError):M.simulate(['V1','V1'])
 def test_plan_hash_is_order_sensitive(self):
  self.assertNotEqual(M.plan_hash(['V1','V2']),M.plan_hash(['V2','V1']))
if __name__=='__main__':unittest.main()
