from __future__ import annotations
import json,re,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPTS=ROOT/'cpf-tools/db/tools'
class BackupReplicaContractTest(unittest.TestCase):
 def test_contract_requires_verified_cross_region_replica_before_primary_purge(self):
  replica=json.loads((ROOT/'cpf-tools/db/cpf-backup-lifecycle-contract.json').read_text(encoding="utf-8"))['crossRegionReplication']
  self.assertTrue(replica['supported']);self.assertEqual(1,replica['minimumVerifiedReplicaCountBeforePrimaryPurge'])
  self.assertTrue(replica['replicaHashMustMatchPrimary']);self.assertTrue(replica['regionMustDifferFromPrimary']);self.assertEqual('fail-closed',replica['unknownState'])
  self.assertEqual('sha256-sidecar',replica['evidenceMode']);self.assertEqual('VERIFIED',replica['sidecarStatus']);self.assertTrue(replica['separationOfDutiesRequiredForPurge'])
 def test_backup_manifest_declares_primary_region_without_duplicate_replica_ledger(self):
  text=(SCRIPTS/'backup-cpf-database.ps1').read_text(encoding="utf-8");self.assertIn('primaryRegion=$Region',text);self.assertNotIn('replicas=@()',text)
 def test_replica_registration_and_copy_share_hash_protected_sidecar_contract(self):
  register=(SCRIPTS/'register-cpf-backup-replica.ps1').read_text(encoding="utf-8");copy=(SCRIPTS/'replicate-cpf-backup-artifact.ps1').read_text(encoding="utf-8")
  for text in (register,copy):
   self.assertIn("status='VERIFIED'",text);self.assertIn('Write-CpfManifestHash',text);self.assertIn('ApprovalReference',text)
  self.assertIn('replicaHash -ne $primaryHash',register);self.assertIn('Replicated artifact SHA-256 mismatch',copy)
  self.assertNotIn('Add-Member -NotePropertyName replicas',register)
  self.assertNotRegex(register,re.compile(r'(?i)\[string\]\$(password|secret|token)'))
 def test_retention_requires_replica_reviewed_plan_sod_and_reconcile(self):
  text=(SCRIPTS/'invoke-cpf-backup-retention.ps1').read_text(encoding="utf-8")
  for token in ('minimumVerifiedReplicaCountBeforePrimaryPurge','REPLICA_REQUIRED','ExpectedPlanSha256','Reviewed retention plan SHA mismatch','$deletionCommitted','reconcileRequired','INVALID_FAIL_CLOSED','-PlanSha256 $planSha','verifiedReplicaCount','Approver','purge-intent-','PURGE_UNKNOWN'):
   self.assertIn(token,text)
  self.assertNotIn('Remove-Item -LiteralPath $artifact -Force',text)
if __name__=='__main__':unittest.main()
