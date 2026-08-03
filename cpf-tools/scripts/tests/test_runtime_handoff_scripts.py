import unittest
from pathlib import Path
ROOT=Path(__file__).parents[1]
class T(unittest.TestCase):
 def test_db_lifecycle_is_fail_closed_and_three_vendor(self):
  t=(ROOT/'run-db-vendor-lifecycle.ps1').read_text(encoding='utf-8')
  for x in ('mariadb','postgresql','oracle','FreshInstall','Upgrade','RollbackReapply','V99__bat_abandon_two_phase_state.sql','R99__bat_abandon_two_phase_state.sql'):
   self.assertIn(x,t)
  self.assertIn('$LASTEXITCODE -ne 0',t)
 def test_audit_script_runs_two_instances_kill_restart_and_checks_loss(self):
  t=(ROOT/'run-adm-audit-multi-instance.ps1').read_text(encoding='utf-8')
  for x in ('R4-A','R4-B','Stop-Process','R4-A-RESTART','Audit 중복 검출','Audit 누락 검출','MASK_ME'):
   self.assertIn(x,t)
if __name__=='__main__':unittest.main()
