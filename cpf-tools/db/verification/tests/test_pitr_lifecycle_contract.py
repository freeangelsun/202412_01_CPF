from __future__ import annotations
import json,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=(ROOT/'cpf-tools/db/tools/invoke-cpf-pitr-restore.ps1').read_text(encoding='utf-8-sig')
CONTRACT=json.loads((ROOT/'cpf-tools/db/cpf-pitr-lifecycle-contract.json').read_text(encoding='utf-8-sig'))
LIFECYCLE=json.loads((ROOT/'cpf-tools/db/cpf-db-lifecycle-contract.json').read_text(encoding='utf-8-sig'))
class PitrLifecycleContractTest(unittest.TestCase):
 def test_three_vendor_native_recovery_consumers(self):
  for token in ['mariadb-binlog','recovery.signal','recovery_target_time','pg_ctl','rman','RESTORE DATABASE','RECOVER DATABASE','OPEN RESETLOGS']:
   self.assertIn(token,SCRIPT)
  self.assertEqual(['mariadb','postgresql','oracle'],CONTRACT['officialVendors'])
 def test_plan_sha_and_high_risk_confirmations(self):
  for token in ['ExpectedPlanSha256','ConfirmApplicationsStopped','ConfirmIsolatedTarget','Operator','Reason','ApprovalReference']:
   self.assertIn(token,SCRIPT)
  self.assertTrue(CONTRACT['executionControls']['planFirst'])
 def test_artifact_hash_window_and_kind_validation(self):
  for token in ['PITR artifact SHA-256 mismatch','recovery window 밖','required artifact kind','sequence가 중복']:
   self.assertIn(token,SCRIPT)
 def test_partial_failure_is_unknown_and_reconcile_required(self):
  self.assertIn("$result.status='UNKNOWN'",SCRIPT)
  self.assertIn('$result.reconcileRequired=$true',SCRIPT)
  self.assertTrue(CONTRACT['executionControls']['unknownOnPartialFailure'])
 def test_postgresql_waits_for_promotion_and_oracle_catalogs_pieces(self):
  for token in ['pg_is_in_recovery','RecoveryTimeoutSeconds','promotion 완료 상태','CATALOG START WITH','OracleCatalogDirectory','catalog directory 밖']:
   self.assertIn(token,SCRIPT)
  self.assertNotIn('SET UNTIL TIME \\"TO_DATE',SCRIPT)
 def test_lifecycle_contract_includes_pitr_after_backup_restore(self):
  self.assertEqual('backup-restore',LIFECYCLE['orderedStages'][-2])
  self.assertEqual('point-in-time-recovery',LIFECYCLE['orderedStages'][-1])
if __name__=='__main__':unittest.main()
