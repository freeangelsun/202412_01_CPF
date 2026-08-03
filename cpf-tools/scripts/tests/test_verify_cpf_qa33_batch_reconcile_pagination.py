from __future__ import annotations

from pathlib import Path
import unittest


class BatchReconcilePaginationContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.root = Path(__file__).resolve().parents[3]
        self.source = (
            self.root
            / "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java"
        ).read_text(encoding="utf-8")
        self.gate = (
            self.root / "cpf-tools/scripts/verify-cpf-qa33-batch-control-plane.py"
        ).read_text(encoding="utf-8")

    def test_reconcile_scans_all_job_instance_pages(self) -> None:
        for marker in (
            "RECONCILE_PAGE_SIZE",
            "while (true)",
            "getJobInstances(jobName, start, RECONCILE_PAGE_SIZE)",
            "start += instances.size()",
        ):
            self.assertIn(marker, self.source)

    def test_quality_gate_protects_the_pagination_contract(self) -> None:
        for marker in ("RECONCILE_PAGE_SIZE", "while (true)", "start += instances.size()"):
            self.assertIn(marker, self.gate)


if __name__ == "__main__":
    unittest.main()
