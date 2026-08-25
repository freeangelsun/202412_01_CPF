from __future__ import annotations

import importlib.util
from pathlib import Path
import shutil

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-batch-agent-fail-closed.py"
SPEC = importlib.util.spec_from_file_location("batch_agent_gate", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def fixture(tmp_path: Path) -> Path:
    source_root = Path(__file__).resolve().parents[4]
    for rel in MODULE.REQUIRED:
        source = source_root / rel
        target = tmp_path / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
    return tmp_path


def test_current_host_agent_contract_passes(tmp_path: Path) -> None:
    result = MODULE.verify(fixture(tmp_path))
    assert result["status"] == "PASS"
    assert result["verifiedSources"] == 4


def test_os_file_lock_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.LEDGER
    path.write_text(path.read_text(encoding="utf-8").replace("FileLock ignored = channel.lock()", "FileLock ignored = null", 1), encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "FileLock ignored = channel.lock()" in str(exc)
    else:
        raise AssertionError("ledger without OS lock passed")


def test_handler_exception_failed_collapse_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.LEDGER
    text = path.read_text(encoding="utf-8").replace(
        "result = unknown(\n                        executing,\n                        \"COMMAND_HANDLER_RESULT_UNKNOWN\"",
        "result = new AgentCommandResult(\n                        commandId, serviceId, commandType, CommandState.FAILED,\n                        \"COMMAND_HANDLER_FAILED\"",
        1,
    )
    path.write_text(text, encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "FAILED" in str(exc) or "COMMAND_HANDLER" in str(exc)
    else:
        raise AssertionError("handler exception collapsed to FAILED passed")


def test_service_timeout_unknown_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.SERVICE
    path.write_text(path.read_text(encoding="utf-8").replace("Result.unknown(", "new Result(false,", 1), encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "unknown" in str(exc).lower()
    else:
        raise AssertionError("known timeout result passed")


def test_runtime_response_loss_failed_collapse_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.CONTROLLER
    path.write_text(path.read_text(encoding="utf-8").replace(
        "return result(stableId, serviceId, command, CommandState.UNKNOWN_RESULT,\n                        command + \"_RESPONSE_UNKNOWN\"",
        "return result(stableId, serviceId, command, CommandState.FAILED,\n                        command + \"_FAILED\"",
        1,
    ), encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "runtimeCommand" in str(exc) or "RESPONSE_UNKNOWN" in str(exc)
    else:
        raise AssertionError("runtime response loss collapsed to FAILED passed")


def test_artifact_publication_compensation_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.INSTALLER
    path.write_text(path.read_text(encoding="utf-8").replace(
        "Exception restoreFailure = restorePublication(",
        "Exception restoreFailure = null; // compensation removed\n                if (false) restorePublication(",
        1,
    ), encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "restorePublication" in str(exc) or "compensation" in str(exc)
    else:
        raise AssertionError("artifact publication without compensation passed")


def test_artifact_partial_publication_unknown_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.INSTALLER
    path.write_text(path.read_text(encoding="utf-8").replace("ARTIFACT_INSTALL_RESULT_UNKNOWN", "ARTIFACT_INSTALL_FAILED", 1), encoding="utf-8")
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert "ARTIFACT_INSTALL_RESULT_UNKNOWN" in str(exc) or "UNKNOWN" in str(exc)
    else:
        raise AssertionError("artifact partial publication collapse passed")
