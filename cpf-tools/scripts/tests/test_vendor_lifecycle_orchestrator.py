from __future__ import annotations
import re,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
TEXT=(ROOT/'cpf-tools/scripts/run-db-vendor-lifecycle.ps1').read_text(encoding='utf-8-sig')
class VendorLifecycleOrchestratorTest(unittest.TestCase):
 def test_delegates_to_canonical_install_and_migration_consumers(self):
  self.assertIn('initialize-cpf-database.ps1',TEXT);self.assertIn('invoke-platform-database-migration.ps1',TEXT)
 def test_no_direct_secret_or_sqlplus_transport(self):
  for token in ('CPF_ORACLE_PASSWORD','connect $user/','set echo on','Tee-Object -FilePath $log'):
   self.assertNotIn(token,TEXT)
  self.assertNotRegex(TEXT,re.compile(r'(?i)password\s*=\s*Need'))
 def test_plan_apply_approval_and_unknown_boundaries(self):
  for token in ('ConfirmExecute','ApprovalReference','ExpectedPlanSha256','ExpectedRollbackPlanSha256','ExpectedLifecyclePlanSha256','lifecyclePlanSha256','profileSha256','backupManifests',"if($executionStarted){'UNKNOWN'}else{'FAILED'}",'reconcileRequired=$executionStarted'):
   self.assertIn(token,TEXT)
 def test_execution_requires_reviewed_lifecycle_plan_hash(self):
  self.assertIn("ExpectedLifecyclePlanSha256 -notmatch '^[0-9a-fA-F]{64}$'",TEXT)
  self.assertIn('Write-CpfJsonAtomic $lifecyclePlan $planAbsolute',TEXT)
  self.assertIn('Get-FileHash -LiteralPath $planAbsolute -Algorithm SHA256',TEXT)
 def test_versions_are_not_hardcoded(self):
  for version in ('V98','V99','V100'):
   self.assertNotIn(version,TEXT)
  self.assertIn('MigrationVersion',TEXT);self.assertIn('FromVersion',TEXT);self.assertIn('ToVersion',TEXT)
if __name__=='__main__':unittest.main()
