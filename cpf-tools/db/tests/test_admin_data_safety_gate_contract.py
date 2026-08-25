from __future__ import annotations

import subprocess
import shutil
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
CHECK = ROOT / "cpf-tools/verification/tools/check-admin-data-safety.ps1"


class AdminDataSafetyGateContractTest(unittest.TestCase):
    def test_gate_requires_canonical_cpf_transaction_annotation(self):
        source = CHECK.read_text(encoding="utf-8-sig")
        self.assertIn('@CpfTransactional\\(transactionManager\\s*=\\s*"admTransactionManager"\\)', source)
        self.assertNotIn('@Transactional\\(transactionManager = "admTransactionManager"\\)', source)
        self.assertIn("BackofficeManagementService.java", source)
        self.assertIn("BackofficeBusinessAuditService.java", source)
        self.assertIn("BackofficeManagementController.java", source)
        self.assertIn("check-data-safety-schema-contract.ps1", source)
        self.assertNotIn("BzaBackofficeService.java", source)
        self.assertNotIn("BzaBusinessAuditService.java", source)
        self.assertNotIn("BzaBackofficeController.java", source)

    def test_bootstrap_approval_repository_uses_repository_ia_and_catalog(self):
        old_path = ROOT / "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeBootstrapApprovalRepository.java"
        new_path = ROOT / "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/repository/BackofficeBootstrapApprovalRepository.java"
        self.assertFalse(old_path.exists())
        repository = new_path.read_text(encoding="utf-8-sig")
        self.assertIn("package com.cpf.backoffice.online.auth.repository;", repository)
        self.assertIn('sqlCatalogProvider.forModule("backoffice")', repository)
        self.assertNotIn('jdbc().update("""', repository)
        self.assertNotIn('jdbc().queryForList("""', repository)

    @unittest.skipUnless(shutil.which("pwsh"), "PowerShell runtime is unavailable; Windows Full Runtime executes this mandatory gate")
    def test_actual_admin_data_safety_gate_passes(self):
        result = subprocess.run(
            ["pwsh", "-NoProfile", "-File", str(CHECK), "-Root", str(ROOT)],
            cwd=ROOT,
            text=True,
            encoding="utf-8",
            errors="replace",
            capture_output=True,
            timeout=60,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn("PASS", result.stdout)


if __name__ == "__main__":
    unittest.main()
