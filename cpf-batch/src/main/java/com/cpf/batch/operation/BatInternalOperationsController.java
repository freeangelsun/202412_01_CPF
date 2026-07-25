package com.cpf.batch.operation;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.common.execution.CpfSharedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 같은 Control Plane이 BAT Owner 기능을 동일 계약으로 호출하기 위한 내부 전용 Adapter입니다.
 * 외부 Gateway 공개 Route가 아니며 {@link CpfSharedApi} 신뢰 경계가 적용됩니다.
 */
@RestController
@Tag(name = "BAT Internal Operations", description = "ADM 등 신뢰된 내부 Control Plane이 사용하는 BAT Owner 내부 API")
@RequestMapping("/bat/internal/operations")
@CpfSharedApi(id="SBATOP0001", name="BatInternalOperations", ownerDomain="BAT",
        description="BAT Owner query/command contract", allowedCallers={"ADM"})
public class BatInternalOperationsController {
    private final CpfBatchOperationsPort operations;

    public BatInternalOperationsController(CpfBatchOperationsPort operations) {
        this.operations = operations;
    }

    @PostMapping("/{operation}")
    @Operation(summary = "BAT Owner 내부 작업 실행", description = "operation에 따라 조회/실행/재시도/중지 등 BAT Owner 기능을 동일 신뢰 경계로 호출합니다.")
    public ResponseEntity<?> invoke(@PathVariable String operation, @RequestBody(required=false) Map<String,Object> p) {
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
                    integer(a,"limit",100));
            case "findExecutionDetail" -> operations.findExecutionDetail(longValue(a,"executionId"));
            case "findInstances" -> operations.findInstances();
            case "findWorkers" -> operations.findWorkers(integer(a,"heartbeatTimeoutSeconds",120));
            case "findStepExecutions" -> operations.findStepExecutions(nullableLong(a,"executionId"), textOrNull(a,"jobId"), integer(a,"limit",100));
            case "findRelations" -> operations.findRelations(textOrNull(a,"jobId"));
            case "findExecutionTargets" -> operations.findExecutionTargets(textOrNull(a,"jobId"), textOrNull(a,"dispatchStatus"), integer(a,"limit",100));
            case "findLocks" -> operations.findLocks(textOrNull(a,"jobId"));
            case "releaseLock" -> operations.releaseLock(text(a,"lockKey"), text(a,"requestUser"), text(a,"reason"));
            case "findGhostCandidates" -> operations.findGhostCandidates(integer(a,"heartbeatTimeoutSeconds",120));
            case "actGhostExecution" -> operations.actGhostExecution(longValue(a,"executionId"), text(a,"actionType"), text(a,"requestUser"), text(a,"reason"));
            case "findOperationLogs" -> operations.findOperationLogs(textOrNull(a,"jobId"), nullableLong(a,"executionId"), integer(a,"limit",100));
            case "simulateSchedule" -> operations.simulateSchedule(text(a,"scheduleId"), textOrNull(a,"baseDate"), integer(a,"days",14));
            case "registerJob" -> operations.registerJob(text(a,"jobId"), textOrNull(a,"jobName"), textOrNull(a,"jobType"), textOrNull(a,"description"), text(a,"requestUser"));
            case "requestRun" -> operations.requestRun(text(a,"jobId"), textOrNull(a,"jobParameters"), text(a,"requestUser"), text(a,"reason"));
            case "requestScheduledRun" -> operations.requestScheduledRun(text(a,"scheduleId"), text(a,"jobId"), textOrNull(a,"jobParameters"), text(a,"requestUser"), text(a,"reason"));
            case "requestRetry" -> operations.requestRetry(longValue(a,"executionId"), text(a,"requestUser"), text(a,"reason"));
            case "requestStop" -> operations.requestStop(longValue(a,"executionId"), text(a,"requestUser"), text(a,"reason"));
            case "updateScheduleEnabled" -> operations.updateScheduleEnabled(text(a,"scheduleId"), bool(a,"enabled"), text(a,"requestUser"), text(a,"reason"));
            case "runSchedulerOnce" -> operations.runSchedulerOnce(text(a,"requestUser"));
            default -> throw new IllegalArgumentException("지원하지 않는 BAT operation입니다: " + operation);
        });
    }

    private static String text(Map<String,Object> p,String k){String v=textOrNull(p,k);if(v==null)throw new IllegalArgumentException(k+"는 필수입니다.");return v;}
    private static String textOrNull(Map<String,Object> p,String k){Object v=p.get(k);return v==null||String.valueOf(v).isBlank()?null:String.valueOf(v).trim();}
    private static int integer(Map<String,Object> p,String k,int d){Object v=p.get(k);return v==null?d:Integer.parseInt(String.valueOf(v));}
    private static long longValue(Map<String,Object> p,String k){Object v=p.get(k);if(v==null)throw new IllegalArgumentException(k+"는 필수입니다.");return Long.parseLong(String.valueOf(v));}
    private static Long nullableLong(Map<String,Object> p,String k){Object v=p.get(k);return v==null||String.valueOf(v).isBlank()?null:Long.parseLong(String.valueOf(v));}
    private static boolean bool(Map<String,Object> p,String k){Object v=p.get(k);return v instanceof Boolean b?b:Boolean.parseBoolean(String.valueOf(v));}
}
