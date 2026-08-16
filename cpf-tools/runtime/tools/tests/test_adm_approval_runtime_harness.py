from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
EVALUATOR = ROOT / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmApprovalDecisionEvaluator.java"
RISK = ROOT / "cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchRiskCommand.java"

HARNESS = r'''
import com.cpf.admin.opr.service.AdmApprovalDecisionEvaluator;
import com.cpf.batch.api.CpfBatchRiskCommand;
import java.util.List;

public class ApprovalRuntimeHarness {
    private static int assertions;
    private static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }
    private static AdmApprovalDecisionEvaluator.StepDecision step(
            int no, String rule, Long required, long total, long approved) {
        return new AdmApprovalDecisionEvaluator.StepDecision(no, rule, required, total, approved);
    }
    public static void main(String[] args) {
        var evaluator = new AdmApprovalDecisionEvaluator();
        check(evaluator.evaluate(false, List.of(step(1,"ALL",null,2,1))).status()
                == AdmApprovalDecisionEvaluator.Status.PENDING, "ALL must wait for every participant");
        check(evaluator.evaluate(false, List.of(step(1,"ALL",null,2,2))).status()
                == AdmApprovalDecisionEvaluator.Status.APPROVED, "ALL must approve when all decided");
        check(evaluator.evaluate(false, List.of(step(1,"ANY",null,3,1))).status()
                == AdmApprovalDecisionEvaluator.Status.APPROVED, "ANY requires one participant");
        check(evaluator.evaluate(false, List.of(step(1,"N_OF_M",2L,4,1))).status()
                == AdmApprovalDecisionEvaluator.Status.PENDING, "N_OF_M must not approve early");
        check(evaluator.evaluate(false, List.of(step(1,"N_OF_M",2L,4,2))).status()
                == AdmApprovalDecisionEvaluator.Status.APPROVED, "N_OF_M threshold");
        check(evaluator.evaluate(false, List.of(step(1,"ANY",null,2,1),step(2,"ALL",null,2,1))).status()
                == AdmApprovalDecisionEvaluator.Status.PENDING, "all steps must complete");
        check(evaluator.evaluate(false, List.of(step(1,"ANY",null,2,1),step(2,"ALL",null,2,2))).status()
                == AdmApprovalDecisionEvaluator.Status.APPROVED, "multi-step approval");
        check(evaluator.evaluate(true, List.of(step(1,"ALL",null,2,2))).status()
                == AdmApprovalDecisionEvaluator.Status.REJECTED, "any rejection must reject");
        check(evaluator.evaluate(false, List.of()).status()
                == AdmApprovalDecisionEvaluator.Status.PENDING, "empty policy must not approve");
        try { evaluator.evaluate(false, List.of(step(1,"N_OF_M",2L,1,1))); throw new AssertionError(); }
        catch (IllegalStateException expected) { assertions++; }
        try { evaluator.evaluate(false, List.of(step(1,"ALL",null,0,0))); throw new AssertionError(); }
        catch (IllegalStateException expected) { assertions++; }
        try { evaluator.evaluate(false, List.of(step(1,"BOGUS",null,1,0))); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { assertions++; }

        var command = new CpfBatchRiskCommand(
                "requestStop", "bat_execution", "101", "BATCH_STOP",
                "operator-a", "incident response", "91", "idem-91", 7L, "");
        var same = new CpfBatchRiskCommand(
                "requestStop", "bat_execution", "101", "BATCH_STOP",
                "operator-a", "incident response", "91", "idem-91", 7L, "");
        var changed = new CpfBatchRiskCommand(
                "requestStop", "bat_execution", "101", "BATCH_STOP",
                "operator-a", "different reason", "91", "idem-91", 7L, "");
        check(command.fingerprint().length() == 64, "SHA-256 length");
        check(command.fingerprint().equals(same.fingerprint()), "stable canonical fingerprint");
        check(!command.fingerprint().equals(changed.fingerprint()), "changed payload must change fingerprint");
        check(command.requiredExpectedVersion() == 7L, "expected version retained");
        command.assertOperation("requestStop", "bat_execution", "101"); assertions++;
        try { command.assertOperation("requestRetry", "bat_execution", "101"); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { assertions++; }
        try { new CpfBatchRiskCommand("requestStop","bat_execution","101","BATCH_STOP", "operator-a","x","91","idem",-1L,""); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { assertions++; }
        System.out.println("APPROVAL_RUNTIME_ASSERTIONS=" + assertions);
    }
}
'''


def test_java21_approval_decision_and_command_runtime() -> None:
    with tempfile.TemporaryDirectory(prefix="cpf-approval-") as temp:
        temp_path = Path(temp)
        harness = temp_path / "ApprovalRuntimeHarness.java"
        harness.write_text(HARNESS, encoding="utf-8")
        classes = temp_path / "classes"
        classes.mkdir()
        compile_result = subprocess.run(
            ["javac", "--release", "21", "-d", str(classes), str(EVALUATOR), str(RISK), str(harness)],
            capture_output=True, text=True, check=False,
        )
        assert compile_result.returncode == 0, compile_result.stderr
        run_result = subprocess.run(
            ["java", "-cp", str(classes), "ApprovalRuntimeHarness"],
            capture_output=True, text=True, check=False,
        )
        assert run_result.returncode == 0, run_result.stderr
        assert "APPROVAL_RUNTIME_ASSERTIONS=19" in run_result.stdout


def test_approval_engine_consumer_closure_is_fail_closed() -> None:
    service = (ROOT / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmApprovalEngineService.java").read_text(encoding="utf-8")
    facade = (ROOT / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBatchOperationService.java").read_text(encoding="utf-8")
    dispatcher = (ROOT / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBatchApprovalCommandDispatcher.java").read_text(encoding="utf-8")
    assert "SELECT DISTINCT s.step_no" in service
    assert "requestKey is already used with a different command payload" in service
    assert "inline sensitive value is not allowed" in service
    assert "automatic replay is blocked" in facade
    assert "AdmBatchApprovalService.Reservation" in facade
    for operation in ["releaseLock", "actGhostExecution", "requestRetry", "requestStop", "updateScheduleEnabled", "requestRun", "runSchedulerOnce"]:
        assert f'case "{operation}"' in dispatcher
