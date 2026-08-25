from __future__ import annotations

import importlib.util
from pathlib import Path
import shutil

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-batch-execution-fencing.py"
SPEC = importlib.util.spec_from_file_location("batch_fencing_gate", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def fixture(tmp_path: Path) -> Path:
    source_root = Path(__file__).resolve().parents[4]
    for rel in MODULE.CHECKS:
        src = source_root / rel
        dst = tmp_path / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
    return tmp_path


def test_current_product_contract_passes(tmp_path: Path) -> None:
    result = MODULE.verify(fixture(tmp_path))
    assert result["status"] == "PASS"
    assert result["verifiedSources"] == len(MODULE.CHECKS)


def test_recover_without_fencing_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    rel = "cpf-batch/runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java"
    path = root / rel
    text = path.read_text(encoding="utf-8")
    recover_start = text.index("public BatchExecutionLink recover(")
    fence_start = text.index("fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);", recover_start)
    path.write_text(text[:fence_start] + "// fence removed" + text[fence_start + len(
        "fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);"):], encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "fencing.assertCurrent" in str(exc)
    else:
        raise AssertionError("missing recover fence passed")


def test_stable_remote_owner_reintroduction_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    rel = "cpf-batch/remote-kafka/src/main/java/com/cpf/batch/execution/CpfBatchKafkaInboundBridge.java"
    path = root / rel
    path.write_text(path.read_text(encoding="utf-8").replace(
        "ledger.complete(direction, envelope.messageId(), attemptOwnerId)",
        "ledger.complete(direction, envelope.messageId(), ownerId)", 1), encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "stale-owner transition" in str(exc) or "attemptOwnerId" in str(exc)
    else:
        raise AssertionError("stable remote owner passed")
