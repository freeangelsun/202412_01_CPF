import re
import unittest
from pathlib import Path

ROOT = Path(__file__).parents[1]


class T(unittest.TestCase):
    def test_db_lifecycle_is_fail_closed_and_three_vendor(self):
        text = (ROOT / 'run-db-vendor-lifecycle.ps1').read_text(encoding='utf-8')
        for token in (
            'mariadb', 'postgresql', 'oracle',
            'FreshInstall', 'Upgrade', 'RollbackReapply',
            'V98__bat_operation_expected_version.sql',
            'V99__bat_abandon_two_phase_state.sql',
            'V100__bat_operation_request_ledger.sql',
            'R98__bat_operation_expected_version.sql',
            'R99__bat_abandon_two_phase_state.sql',
            'R100__bat_operation_request_ledger.sql',
            'verify/V98__bat_operation_expected_version.sql',
            'verify/V99__bat_abandon_two_phase_state.sql',
            'verify/V100__bat_operation_request_ledger.sql',
        ):
            self.assertIn(token, text)
        self.assertIn('$LASTEXITCODE -ne 0', text)

    def test_db_lifecycle_orders_v100_and_r100_correctly(self):
        text = (ROOT / 'run-db-vendor-lifecycle.ps1').read_text(encoding='utf-8')
        bodies = {
            mode: re.search(rf"'{mode}'\s*\{{([^}}]+)\}}", text).group(1)
            for mode in ('FreshInstall', 'Upgrade', 'RollbackReapply')
        }
        self.assertIn('Run-Sql $verify100', bodies['FreshInstall'])
        self.assertLess(bodies['Upgrade'].index('Run-Sql $v99'), bodies['Upgrade'].index('Run-Sql $v100'))
        self.assertLess(bodies['Upgrade'].index('Run-Sql $v100'), bodies['Upgrade'].index('Run-Sql $verify100'))
        rollback = bodies['RollbackReapply']
        self.assertLess(rollback.index('Run-Sql $r100'), rollback.index('Run-Sql $r99'))
        self.assertLess(rollback.index('Run-Sql $r99'), rollback.index('Run-Sql $r98'))
        self.assertLess(rollback.index('Run-Sql $r98'), rollback.index('Run-Sql $v98'))
        self.assertLess(rollback.index('Run-Sql $v99'), rollback.index('Run-Sql $v100'))
        self.assertLess(rollback.index('Run-Sql $v100'), rollback.index('Run-Sql $verify100'))

    def test_audit_script_runs_two_instances_kill_restart_and_checks_loss(self):
        text = (ROOT / 'run-adm-audit-multi-instance.ps1').read_text(encoding='utf-8')
        for token in ('R4-A', 'R4-B', 'Stop-Process', 'R4-A-RESTART', 'Audit 중복 검출', 'Audit 누락 검출', 'MASK_ME'):
            self.assertIn(token, text)


if __name__ == '__main__':
    unittest.main()
