from __future__ import annotations

import subprocess
from pathlib import Path

QA39 = Path(__file__).parents[1] / "Qa39Tool.java"


def write(root: Path, rel: str, text: str) -> None:
    p = root / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(text, encoding="utf-8")


def compile_tool(tmp_path: Path) -> Path:
    out = tmp_path / "classes"
    out.mkdir()
    result = subprocess.run(["javac", "-d", str(out), str(QA39)], text=True, capture_output=True)
    assert result.returncode == 0, result.stdout + result.stderr
    return out


def fixture(root: Path, scheduler: str) -> None:
    files = {
        "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerDispatchService.java": scheduler,
        "cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentEngine.java": "UNKNOWN_RESULT ROLLBACK reconcile idempotencyKey",
        "cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentCellLock.java": "acquire release",
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerCompletionFilter.java": "UNKNOWN_RESULT recovery",
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayAuditRecoverySpool.java": "spool replay",
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerRecoverySpool.java": "spool replay",
        "cpf-tools/scripts/smoke-bat-two-worker-runtime.ps1": "Stop-Process two worker",
    }
    for rel, text in files.items():
        write(root, rel, text)


def run_gate(tmp_path: Path, scheduler: str) -> subprocess.CompletedProcess[str]:
    repo = tmp_path / "repo"
    fixture(repo, scheduler)
    classes = compile_tool(tmp_path)
    return subprocess.run(["java", "-cp", str(classes), "Qa39Tool", "runtime-contracts", "--root", str(repo)], text=True, capture_output=True)


def test_runtime_contract_accepts_durable_dispatch_pending_reconciliation(tmp_path: Path) -> None:
    result = run_gate(tmp_path, "UNKNOWN fencing outbox 재조정 dispatchPending() scheduler-trigger-mark-unknown")
    assert result.returncode == 0, result.stdout + result.stderr


def test_runtime_contract_rejects_unknown_without_reconciliation_path(tmp_path: Path) -> None:
    result = run_gate(tmp_path, "UNKNOWN fencing outbox")
    assert result.returncode == 1
    assert "scheduler reconciliation path" in result.stderr
