import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[4]
LIFECYCLE = REPO / 'cpf-tools/db/tools/run-db-vendor-lifecycle.ps1'


class T(unittest.TestCase):
    def test_db_lifecycle_is_fail_closed_and_three_vendor(self):
        text = LIFECYCLE.read_text(encoding='utf-8-sig')
        for token in (
            'mariadb', 'postgresql', 'oracle',
            'FreshInstall', 'Upgrade', 'RollbackReapply',
            'vendor-pack-manifest.json', 'checksums.sha256',
            'initialize-cpf-database.ps1', 'invoke-platform-database-migration.ps1',
            'ConfirmExecute', 'ExpectedLifecyclePlanSha256',
            "if($executionStarted){'UNKNOWN'}else{'FAILED'}",
        ):
            self.assertIn(token, text)
        for version in ('V98', 'V99', 'V100'):
            self.assertNotIn(version, text)

    def test_db_lifecycle_publishes_fresh_and_current_edge_order(self):
        text = LIFECYCLE.read_text(encoding='utf-8-sig')
        fresh = [text.index("role = 'Provision'"), text.index("role = 'EmptyInstall'"),
                 text.index("role = 'ProductSeed'"), text.index("role = 'Verify'")]
        self.assertEqual(sorted(fresh), fresh)
        self.assertIn("stage = 'Rollback'", text)
        self.assertIn("stage = 'Reapply'", text)
        self.assertLess(text.index("stage = 'Rollback'"), text.index("stage = 'Reapply'"))
        self.assertIn('PRE_CURRENT_EDGE_FIXTURE', text)

    def test_audit_script_runs_two_instances_kill_restart_and_checks_loss(self):
        text = (REPO / 'cpf-tools/verification/tools/run-adm-audit-multi-instance.ps1').read_text(encoding='utf-8')
        for token in ('R4-A', 'R4-B', 'Stop-Process', 'R4-A-RESTART', 'Audit 중복 검출', 'Audit 누락 검출', 'MASK_ME'):
            self.assertIn(token, text)


if __name__ == '__main__':
    unittest.main()
