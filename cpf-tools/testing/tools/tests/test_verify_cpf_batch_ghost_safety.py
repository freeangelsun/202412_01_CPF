from __future__ import annotations
import importlib.util
from pathlib import Path
import tempfile
import unittest

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-batch-ghost-safety.py"
spec = importlib.util.spec_from_file_location("batch_ghost_safety", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class BatchGhostSafetyTest(unittest.TestCase):
    def fixture(self) -> Path:
        root = Path(tempfile.mkdtemp())
        files = {
            "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/BatchOperationsCompatibilityService.java": '''
            GHOST_ACTIVE_STATUSES; last_heartbeat_at;
            exactOne("compat-lock-for-update", lockKey); requireExpiredLock(before, lockKey);
            requireSingleMutation(changed, "expired lock release", lockKey);
            exactOne("compat-execution-lock", executionId); requireGhostCandidate(before, executionId);
            exactOne("compat-lock-expired-for-job-for-update", jobId);
            requireSingleMutation(changed, "ghost lock release", lockKey);
            int changed = jdbc.update(sql.required("compat-execution-finish-ghost"));
            requireSingleMutation(changed, "ghost execution transition", String.valueOf(executionId));
            ''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmBatchController.java": '"BATCH_GHOST_" + result.get("action")',
            "cpf-admin/frontend/src/features/batch-runtime-control/api.ts": "const supported=['FAIL', 'ABANDON', 'RELEASE_LOCK']",
            "cpf-admin/frontend/src/features/batch-runtime-control/BatchOperationsWorkbench.vue": 'actionType:"ABANDON"',
        }
        finish = """UPDATE bat_execution SET execution_status = ?, updated_by = ? WHERE execution_id = ? AND execution_status IN ('RUNNING', 'CLAIMED', 'CLAIMING') AND last_heartbeat_at IS NOT NULL"""
        lock = """SELECT * FROM bat_lock WHERE job_id = ? AND expire_at < CURRENT_TIMESTAMP(3) FOR UPDATE"""
        for vendor in module.VENDORS:
            files[f"cpf-tools/db/vendor/{vendor}/runtime/bat/repository/compat-execution-finish-ghost.sql"] = finish
            files[f"cpf-tools/db/vendor/{vendor}/runtime/bat/repository/compat-lock-expired-for-job-for-update.sql"] = lock
        for rel, text in files.items():
            path = root / rel
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8")
        return root

    def test_valid(self):
        module.verify(self.fixture())

    def test_unsupported_reconcile_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/frontend/src/features/batch-runtime-control/api.ts"
        p.write_text("const supported=['FAIL', 'ABANDON', 'RELEASE_LOCK', 'RECONCILE']", encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_missing_exact_mutation_rejected(self):
        root = self.fixture()
        p = root / "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/BatchOperationsCompatibilityService.java"
        p.write_text(p.read_text(encoding="utf-8").replace(
            'requireSingleMutation(changed, "ghost lock release", lockKey);', ""), encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_vendor_drift_rejected(self):
        root = self.fixture()
        p = root / "cpf-tools/db/vendor/oracle/runtime/bat/repository/compat-execution-finish-ghost.sql"
        p.write_text(p.read_text(encoding="utf-8").replace("last_heartbeat_at IS NOT NULL", "1=1"), encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)


if __name__ == "__main__":
    unittest.main()
