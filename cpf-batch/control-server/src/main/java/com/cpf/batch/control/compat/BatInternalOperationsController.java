package com.cpf.batch.control.compat;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.util.CpfHeaders;
import com.cpf.core.api.execution.CpfSharedApi;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 같은 Control Plane이 BAT Owner 기능을 동일 계약으로 호출하기 위한 내부 전용 Adapter입니다.
 * 외부 Gateway 공개 Route가 아니며 {@link CpfSharedApi} 신뢰 경계가 적용됩니다.
 */
@RestController
@RequestMapping("/bat/internal/operations")
@CpfSharedApi(id="SBATOP0001", name="BatInternalOperations", ownerDomain="BAT",
        description="BAT Owner query/command contract", allowedCallers={"ADM"})
public class BatInternalOperationsController {
    private final CpfBatchOperationsPort operations;
    private final BatVerifiedActorResolver actorResolver;

    public BatInternalOperationsController(
            CpfBatchOperationsPort operations,
            BatVerifiedActorResolver actorResolver) {
        this.operations = operations;
        this.actorResolver = actorResolver;
    }

    @PostMapping("/{operation}")
    public ResponseEntity<?> invoke(
            @PathVariable String operation,
            @RequestBody(required=false) Map<String,Object> p,
            HttpServletRequest request) {
        Map<String,Object> a = p == null ? Map.of() : p;
        return ResponseEntity.ok(switch (operation) {
            case "findJobs" -> operations.findJobs();
            case "findJobDetail" -> operations.findJobDetail(text(a,"jobId"));
            case "findSchedules" -> operations.findSchedules();
            case "findExecutions" -> operations.findExecutions(
                    textOrNull(a,"jobId"),
                    textOrNull(a,"transactionId"),
                    nullableLong(a,"springBatchJobInstanceId"),
                    textOrNull(a,"workerId"),
                    textOrNull(a,"serverInstanceId"),
                    textOrNull(a,"fromDate"),
                    textOrNull(a,"toDate"),
                    integer(a,"limit",100));
            case "findExecutionPage" -> operations.findExecutionPage(
                    textOrNull(a,"jobId"),
                    textOrNull(a,"transactionId"),
                    nullableLong(a,"springBatchJobInstanceId"),
                    textOrNull(a,"workerId"),
                    textOrNull(a,"serverInstanceId"),
                    textOrNull(a,"status"),
                    textOrNull(a,"fromDate"),
                    textOrNull(a,"toDate"),
                    integer(a,"page",0),
                    integer(a,"size",50));
            case "findExecutionDetail" -> operations.findExecutionDetail(longValue(a,"executionId"));
            case "findInstances" -> operations.findInstances();
            case "findWorkers" -> operations.findWorkers(integer(a,"heartbeatTimeoutSeconds",120));
            case "findStepExecutions" -> operations.findStepExecutions(nullableLong(a,"executionId"), textOrNull(a,"jobId"), integer(a,"limit",100));
            case "findRelations" -> operations.findRelations(textOrNull(a,"jobId"));
            case "findExecutionTargets" -> operations.findExecutionTargets(textOrNull(a,"jobId"), textOrNull(a,"dispatchStatus"), integer(a,"limit",100));
            case "findLocks" -> operations.findLocks(textOrNull(a,"jobId"));
            case "releaseLock" -> operations.releaseLock(text(a,"lockKey"), risk(request,a,"releaseLock","bat_lock",text(a,"lockKey")));
            case "findGhostCandidates" -> operations.findGhostCandidates(integer(a,"heartbeatTimeoutSeconds",120));
            case "actGhostExecution" -> operations.actGhostExecution(longValue(a,"executionId"), text(a,"actionType"), risk(request,a,"actGhostExecution","bat_execution",String.valueOf(longValue(a,"executionId"))));
            case "findOperationLogs" -> operations.findOperationLogs(textOrNull(a,"jobId"), nullableLong(a,"executionId"), integer(a,"limit",100));
            case "simulateSchedule" -> operations.simulateSchedule(text(a,"scheduleId"), textOrNull(a,"baseDate"), integer(a,"days",14));
            case "registerJob" -> operations.registerJob(text(a,"jobId"), textOrNull(a,"jobName"), textOrNull(a,"jobType"), textOrNull(a,"description"), actor(request,a));
            case "requestRun" -> operations.requestRun(text(a,"jobId"), textOrNull(a,"jobParameters"), risk(request,a,"requestRun","bat_job",text(a,"jobId")));
            case "requestScheduledRun" -> operations.requestScheduledRun(text(a,"scheduleId"), text(a,"jobId"), textOrNull(a,"jobParameters"), actor(request,a), text(a,"reason"));
            case "requestRetry" -> operations.requestRetry(longValue(a,"executionId"), risk(request,a,"requestRetry","bat_execution",String.valueOf(longValue(a,"executionId"))));
            case "requestStop" -> operations.requestStop(longValue(a,"executionId"), risk(request,a,"requestStop","bat_execution",String.valueOf(longValue(a,"executionId"))));
            case "updateScheduleEnabled" -> operations.updateScheduleEnabled(text(a,"scheduleId"), bool(a,"enabled"), risk(request,a,"updateScheduleEnabled","bat_schedule",text(a,"scheduleId")));
            case "runSchedulerOnce" -> operations.runSchedulerOnce(risk(request,a,"runSchedulerOnce","bat_schedule","DUE_SCHEDULES"));
            default -> throw new IllegalArgumentException("지원하지 않는 BAT operation입니다: " + operation);
        });
    }

    private String actor(HttpServletRequest request,Map<String,Object> payload){
        return actorResolver.actor(request,textOrNull(payload,"requestUser"),"requestUser");
    }

    private CpfBatchRiskCommand risk(
            HttpServletRequest request,
            Map<String,Object> payload,
            String expectedOperation,
            String expectedTargetType,
            String expectedTargetId) {
        String verifiedActor = actor(request, payload);
        CpfBatchRiskCommand command = new CpfBatchRiskCommand(
                text(payload,"operation"),
                text(payload,"targetType"),
                text(payload,"targetId"),
                text(payload,"actionType"),
                verifiedActor,
                text(payload,"reason"),
                text(payload,"approvalRequestId"),
                text(payload,"idempotencyKey"),
                nullableLong(payload,"expectedVersion"),
                textOrNull(payload,"payload"));
        command.assertOperation(expectedOperation, expectedTargetType, expectedTargetId);
        String suppliedHash = text(payload,"requestHash");
        if (!command.fingerprint().equalsIgnoreCase(suppliedHash)) {
            throw new IllegalArgumentException("BAT risk command hash mismatch");
        }
        requireHeader(request, CpfHeaders.idempotencyKey(), command.idempotencyKey());
        requireHeader(request, CpfHeaders.approvalRequestId(), command.approvalRequestId());
        requireHeader(request, CpfHeaders.approvalRequesterId(), command.requestUser());
        return command;
    }

    private static void requireHeader(HttpServletRequest request, String name, String expected) {
        String value = request == null ? null : request.getHeader(name);
        if (value == null || !value.trim().equals(expected)) {
            throw new IllegalArgumentException(name + " does not match the verified BAT risk command");
        }
    }

    private static String text(Map<String,Object> p,String k){String v=textOrNull(p,k);if(v==null)throw new IllegalArgumentException(k+"는 필수입니다.");return v;}
    private static String textOrNull(Map<String,Object> p,String k){Object v=p.get(k);return v==null||String.valueOf(v).isBlank()?null:String.valueOf(v).trim();}
    private static int integer(Map<String,Object> p,String k,int d){Object v=p.get(k);return v==null?d:Integer.parseInt(String.valueOf(v));}
    private static long longValue(Map<String,Object> p,String k){Object v=p.get(k);if(v==null)throw new IllegalArgumentException(k+"는 필수입니다.");return Long.parseLong(String.valueOf(v));}
    private static Long nullableLong(Map<String,Object> p,String k){Object v=p.get(k);return v==null||String.valueOf(v).isBlank()?null:Long.parseLong(String.valueOf(v));}
    private static boolean bool(Map<String,Object> p,String k){Object v=p.get(k);return v instanceof Boolean b?b:Boolean.parseBoolean(String.valueOf(v));}
}
