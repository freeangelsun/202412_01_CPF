package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.admin.opr.batch.runtime.BatchRuntimeControlClient;
import com.cpf.core.api.data.CpfDataRow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 정식 ADM Approval Engine에서 승인된 BAT Runtime 위험조치를 BAT Owner Port로 전달합니다. */
@Component("cpfBatchRuntimeApprovalOwnerCommandPort")
public final class BatchRuntimeApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private static final Set<ApprovalOwnerTuple> ALLOWED = Set.of(
            tuple("releaseLock", "BATCH_LOCK_RELEASE", "bat_lock"),
            tuple("actGhostExecution", "BATCH_GHOST_FAIL", "bat_execution"),
            tuple("actGhostExecution", "BATCH_GHOST_ABANDON", "bat_execution"),
            tuple("actGhostExecution", "BATCH_GHOST_RELEASE_LOCK", "bat_execution"),
            tuple("requestRetry", "BATCH_RETRY", "bat_execution"),
            tuple("requestStop", "BATCH_STOP", "bat_execution"),
            tuple("updateScheduleEnabled", "BATCH_SCHEDULE_ENABLE", "bat_schedule"),
            tuple("updateScheduleEnabled", "BATCH_SCHEDULE_DISABLE", "bat_schedule"),
            tuple("requestRun", "BATCH_RUN", "bat_job"),
            tuple("runSchedulerOnce", "BATCH_SCHEDULER_RUN_ONCE", "bat_schedule"),
            tuple("runtimeCommand", "BATCH_RUNTIME_START", "bat_runtime"),
            tuple("runtimeCommand", "BATCH_RUNTIME_STOP", "bat_runtime"),
            tuple("runtimeCommand", "BATCH_RUNTIME_RESTART", "bat_runtime"),
            tuple("runtimeCommand", "BATCH_RUNTIME_DRAIN", "bat_runtime"),
            tuple("runtimeCommand", "BATCH_RUNTIME_RESUME", "bat_runtime"),
            tuple("runtimeCommand", "BATCH_RUNTIME_ROLLBACK", "bat_runtime"));

    private final CpfBatchOperationsPort batch;
    private final ObjectMapper objectMapper;
    private final BatchRuntimeControlClient runtimeClient;

    @org.springframework.beans.factory.annotation.Autowired
    public BatchRuntimeApprovalOwnerCommandAdapter(CpfBatchOperationsPort batch, ObjectMapper objectMapper, BatchRuntimeControlClient runtimeClient) {
        this.batch = Objects.requireNonNull(batch, "batch");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.runtimeClient = Objects.requireNonNull(runtimeClient, "runtimeClient");
    }

    BatchRuntimeApprovalOwnerCommandAdapter(CpfBatchOperationsPort batch, ObjectMapper objectMapper) {
        this.batch = Objects.requireNonNull(batch, "batch");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.runtimeClient = null;
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        String module = Objects.toString(ownerModule, "").trim();
        String command = Objects.toString(ownerCommand, "").trim();
        return ALLOWED.stream().anyMatch(tuple -> tuple.ownerModule().equals(module)
                && tuple.ownerCommand().equals(command));
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        ApprovalOwnerTuple candidate = new ApprovalOwnerTuple(
                Objects.toString(ownerModule, "").trim(),
                Objects.toString(ownerCommand, "").trim(),
                Objects.toString(actionType, "").trim(),
                Objects.toString(targetType, "").trim());
        return ALLOWED.contains(candidate);
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("BAT_COMMAND_UNSUPPORTED", "지원하지 않는 BAT Runtime 승인 Command입니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("BAT_SELF_APPROVAL", "요청자와 승인 실행자는 달라야 합니다.");
        }
        final CpfBatchRiskCommand risk;
        try {
            risk = approvedRisk(command);
        } catch (IllegalArgumentException invalid) {
            return failed("BAT_APPROVAL_SNAPSHOT_MISMATCH", "승인 BAT Command Snapshot이 실행 명령과 일치하지 않습니다.");
        }
        try {
            switch (command.ownerCommand()) {
                case "releaseLock" -> batch.releaseLock(risk.targetId(), risk);
                case "actGhostExecution" -> batch.actGhostExecution(
                        Long.parseLong(risk.targetId()), ghostAction(risk), risk);
                case "requestRetry" -> batch.requestRetry(Long.parseLong(risk.targetId()), risk);
                case "requestStop" -> batch.requestStop(Long.parseLong(risk.targetId()), risk);
                case "updateScheduleEnabled" -> batch.updateScheduleEnabled(
                        risk.targetId(), scheduleEnabled(risk), risk);
                case "requestRun" -> batch.requestRun(risk.targetId(), risk.payload(), risk);
                case "runSchedulerOnce" -> batch.runSchedulerOnce(risk);
                case "runtimeCommand" -> executeRuntimeCommand(command, risk);
                default -> throw new IllegalArgumentException("unsupported BAT command");
            }
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.SUCCEEDED, "BAT_COMMAND_SUCCEEDED", "BAT Runtime 위험조치 완료");
        } catch (CpfBatchOwnerUnknownResultException unresolved) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN, unresolved.failureCode(), "BAT Runtime 결과 재확인이 필요합니다.");
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            return failed("BAT_COMMAND_REJECTED", "BAT Runtime 위험조치가 상태·버전 검증에서 거부되었습니다.");
        } catch (RuntimeException unexpected) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN, "BAT_COMMAND_UNKNOWN", "BAT Runtime 결과를 확정할 수 없습니다.");
        }
    }

    @Override
    public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("BAT_COMMAND_UNSUPPORTED", "지원하지 않는 BAT Runtime 승인 Command입니다.");
        }
        final CpfBatchRiskCommand risk;
        try {
            risk = approvedRisk(command);
        } catch (RuntimeException invalid) {
            return failed("BAT_APPROVAL_SNAPSHOT_MISMATCH", "승인 BAT Command Snapshot이 실행 명령과 일치하지 않습니다.");
        }
        try {
            return switch (command.ownerCommand()) {
                case "releaseLock" -> observeReleasedLock(risk);
                case "requestStop" -> observeExecutionState(risk, Set.of("STOPPED", "ABANDONED"));
                case "actGhostExecution" -> observeGhostState(risk);
                case "updateScheduleEnabled" -> observeScheduleState(risk);
                case "requestRetry", "requestRun", "runSchedulerOnce" -> observeOperationLedger(command, risk);
                case "runtimeCommand" -> observeRuntimeCommand(command);
                default -> unknown("BAT_RECONCILE_UNSUPPORTED", "Owner 상태 관측 계약이 없습니다.");
            };
        } catch (RuntimeException readFailure) {
            return unknown("BAT_RECONCILE_READ_FAILED", "BAT Owner 상태 조회에 실패해 UNKNOWN을 유지합니다.");
        }
    }


    private void executeRuntimeCommand(AdmApprovedOperationCommand command, CpfBatchRiskCommand risk) {
        if (runtimeClient == null) throw new IllegalStateException("BAT runtime client is unavailable");
        Map<String,Object> payload = read(risk.payload());
        String commandType = text(payload, "commandType").toUpperCase(Locale.ROOT);
        if (!("BATCH_RUNTIME_" + commandType).equals(risk.actionType()))
            throw new IllegalArgumentException("runtime actionType mismatch");
        String targetType = text(payload, "targetType").toUpperCase(Locale.ROOT);
        if (!Set.of("INSTANCE", "POOL", "AGENT").contains(targetType))
            throw new IllegalArgumentException("runtime targetType mismatch");
        Object targetIdsValue = value(payload, "targetIds");
        if (!(targetIdsValue instanceof List<?> ids) || ids.isEmpty() || ids.stream().map(String::valueOf).noneMatch(risk.targetId()::equals))
            throw new IllegalArgumentException("approved targetId is absent from runtime targetIds");
        if (risk.expectedVersion() == null) throw new IllegalArgumentException("runtime expectedVersion is required");
        Map<String,Object> request = new LinkedHashMap<>(payload);
        for (String field : List.of("requestedBy","requestUser","actorId","operatorId","operatorIdOverride","approvedBy")) request.remove(field);
        request.put("commandId", command.commandRequestId());
        request.put("idempotencyKey", risk.idempotencyKey());
        request.put("expectedVersion", risk.expectedVersion());
        request.put("requestedBy", command.requestedBy());
        request.put("reason", risk.reason());
        request.put("approvalRequestId", String.valueOf(command.approvalRequestId()));
        request.put("approvedBy", command.approvedBy());
        runtimeClient.commandApproved(request, String.valueOf(command.approvalRequestId()), command.requestedBy());
    }

    private AdmApprovedOperationResult observeRuntimeCommand(AdmApprovedOperationCommand command) {
        if (runtimeClient == null) return unknown("BAT_RUNTIME_CLIENT_UNAVAILABLE", "BAT Runtime 상태 Client가 없습니다.");
        CpfDataRow row = runtimeClient.commandState(command.commandRequestId());
        String state = upper(first(row, "state", "status", "commandState"));
        if (Set.of("SUCCEEDED", "ROLLED_BACK").contains(state)) return succeeded("BAT_RUNTIME_RECONCILED_" + state, "Runtime command 최종 성공 상태를 관측했습니다.");
        if (Set.of("FAILED", "PARTIALLY_ROLLED_BACK").contains(state)) return failed("BAT_RUNTIME_RECONCILED_" + state, "Runtime command 실패 상태를 관측했습니다.");
        return unknown("BAT_RUNTIME_RECONCILE_PENDING", "Runtime command가 아직 최종 상태가 아닙니다.");
    }
    private AdmApprovedOperationResult observeReleasedLock(CpfBatchRiskCommand risk) {
        boolean stillPresent = batch.findLocks(null).stream().anyMatch(row ->
                risk.targetId().equals(first(row, "lockKey", "lock_key", "id", "targetId")));
        return stillPresent ? unknown("BAT_LOCK_RELEASE_PENDING", "Lock이 아직 Owner에 존재합니다.")
                : succeeded("BAT_LOCK_RELEASE_RECONCILED", "Owner 조회에서 Lock 해제를 관측했습니다.");
    }

    private AdmApprovedOperationResult observeExecutionState(CpfBatchRiskCommand risk, Set<String> accepted) {
        CpfDataRow row = batch.findExecutionDetail(Long.parseLong(risk.targetId()));
        String state = upper(first(row, "status", "executionStatus", "batchStatus"));
        if (accepted.contains(state)) return succeeded("BAT_EXECUTION_RECONCILED_" + state, "Owner 실행 상태를 관측했습니다.");
        if (Set.of("FAILED", "ERROR").contains(state)) return failed("BAT_EXECUTION_RECONCILED_" + state, "Owner 실행 실패를 관측했습니다.");
        return unknown("BAT_EXECUTION_RECONCILE_PENDING", "실행 상태가 아직 최종 결과를 증명하지 못합니다.");
    }

    private AdmApprovedOperationResult observeGhostState(CpfBatchRiskCommand risk) {
        String action = ghostAction(risk);
        if ("RELEASE_LOCK".equals(action)) {
            return observeReleasedLock(risk);
        }
        return observeExecutionState(risk, "FAIL".equals(action) ? Set.of("FAILED") : Set.of("ABANDONED"));
    }

    private AdmApprovedOperationResult observeScheduleState(CpfBatchRiskCommand risk) {
        boolean expected = scheduleEnabled(risk);
        CpfDataRow row = batch.findSchedules().stream()
                .filter(item -> risk.targetId().equals(first(item, "scheduleId", "schedule_id", "id")))
                .findFirst().orElse(null);
        if (row == null) return unknown("BAT_SCHEDULE_NOT_OBSERVED", "Schedule을 Owner에서 찾지 못했습니다.");
        String actual = upper(first(row, "enabledYn", "enabled", "activeYn", "status"));
        boolean observed = expected ? Set.of("Y", "TRUE", "ENABLED", "ACTIVE").contains(actual)
                : Set.of("N", "FALSE", "DISABLED", "INACTIVE").contains(actual);
        return observed ? succeeded("BAT_SCHEDULE_RECONCILED", "Owner Schedule 상태가 승인 명령과 일치합니다.")
                : unknown("BAT_SCHEDULE_RECONCILE_PENDING", "Owner Schedule 상태가 승인 명령 결과를 아직 증명하지 못합니다.");
    }

    private AdmApprovedOperationResult observeOperationLedger(AdmApprovedOperationCommand command, CpfBatchRiskCommand risk) {
        Long executionId = "bat_execution".equals(risk.targetType()) ? Long.parseLong(risk.targetId()) : null;
        String jobId = "bat_job".equals(risk.targetType()) ? risk.targetId() : null;
        CpfDataRow row = batch.findOperationLogs(jobId, executionId, 1000).stream()
                .filter(item -> matchesOperationIdentity(item, command, risk))
                .findFirst().orElse(null);
        if (row == null) return unknown("BAT_OPERATION_NOT_OBSERVED", "원 승인 명령의 Owner operation ledger를 찾지 못했습니다.");
        String state = upper(first(row, "status", "resultState", "operationStatus", "commandState"));
        if (Set.of("SUCCEEDED", "SUCCESS", "COMPLETED").contains(state))
            return succeeded("BAT_OPERATION_RECONCILED_" + state, "원 승인 명령의 Owner operation ledger를 관측했습니다.");
        if (Set.of("FAILED", "REJECTED", "ERROR").contains(state))
            return failed("BAT_OPERATION_RECONCILED_" + state, "Owner operation ledger에서 실패를 관측했습니다.");
        return unknown("BAT_OPERATION_RECONCILE_PENDING", "Owner operation ledger가 아직 최종 결과를 증명하지 못합니다.");
    }

    private static boolean matchesOperationIdentity(Map<String, ?> row, AdmApprovedOperationCommand command, CpfBatchRiskCommand risk) {
        String rowCommandId = first(row, "commandRequestId", "command_request_id", "operationId", "operation_id");
        String rowIdempotencyKey = first(row, "idempotencyKey", "idempotency_key", "requestKey", "request_key");
        String rowApprovalRequestId = first(row, "approvalRequestId", "approval_request_id");
        String rowOperation = first(row, "operation", "ownerCommand", "owner_command", "action");
        String rowTargetType = first(row, "targetType", "target_type");
        String rowTargetId = first(row, "targetId", "target_id", "executionId", "execution_id", "jobId", "job_id");
        return command.commandRequestId().equals(rowCommandId)
                && risk.idempotencyKey().equals(rowIdempotencyKey)
                && String.valueOf(command.approvalRequestId()).equals(rowApprovalRequestId)
                && risk.operation().equals(rowOperation)
                && risk.targetType().equals(rowTargetType)
                && risk.targetId().equals(rowTargetId);
    }

    private static String first(Map<String, ?> row, String... keys) {
        if (row == null) return "";
        for (String key : keys) {
            Object v=value(row,key); if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v).trim();
        }
        return "";
    }

    private static String upper(String value) { return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT); }
    private static AdmApprovedOperationResult succeeded(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED, code, message);
    }
    private static AdmApprovedOperationResult unknown(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN, code, message);
    }

    private CpfBatchRiskCommand approvedRisk(AdmApprovedOperationCommand command) {
        Map<String,Object> snapshot = read(command.payloadSnapshot());
        CpfBatchRiskCommand risk = new CpfBatchRiskCommand(
                text(snapshot, "operation"), text(snapshot, "targetType"), text(snapshot, "targetId"),
                text(snapshot, "actionType"), text(snapshot, "requestUser"), text(snapshot, "reason"),
                text(snapshot, "approvalRequestId"), text(snapshot, "idempotencyKey"),
                longOrNull(value(snapshot, "expectedVersion")), textOrEmpty(snapshot, "payload"));
        if (!risk.fingerprint().equalsIgnoreCase(command.payloadHash())
                || !risk.operation().equals(command.ownerCommand())
                || !risk.targetType().equals(expectedTargetType(command.ownerCommand()))
                || !risk.targetId().equals(command.targetId())
                || !risk.actionType().equals(command.actionType())
                || !risk.requestUser().equals(command.requestedBy())
                || !risk.approvalRequestId().equals(String.valueOf(command.approvalRequestId()))) {
            throw new IllegalArgumentException("approved BAT snapshot mismatch");
        }
        return risk;
    }

    private Map<String,Object> read(String snapshot) {
        try {
            Map<String,Object> value = objectMapper.readValue(snapshot, new TypeReference<>() {});
            return value == null ? Map.of() : value;
        } catch (Exception invalid) {
            throw new IllegalArgumentException("승인 Payload Snapshot JSON이 올바르지 않습니다.", invalid);
        }
    }

    private static String expectedTargetType(String command) {
        return switch (command) {
            case "releaseLock" -> "bat_lock";
            case "actGhostExecution", "requestRetry", "requestStop" -> "bat_execution";
            case "updateScheduleEnabled", "runSchedulerOnce" -> "bat_schedule";
            case "requestRun" -> "bat_job";
            case "runtimeCommand" -> "bat_runtime";
            default -> throw new IllegalArgumentException("unsupported BAT command: " + command);
        };
    }

    private static String ghostAction(CpfBatchRiskCommand risk) {
        if (!risk.payload().isBlank()) return risk.payload().trim().toUpperCase(Locale.ROOT);
        String action = risk.actionType();
        return action.startsWith("BATCH_GHOST_") ? action.substring("BATCH_GHOST_".length()) : action;
    }

    private static boolean scheduleEnabled(CpfBatchRiskCommand risk) {
        if (risk.actionType().endsWith("ENABLE")) return true;
        if (risk.actionType().endsWith("DISABLE")) return false;
        String payload = risk.payload().trim().toLowerCase(Locale.ROOT);
        if (payload.equals("enabled=true") || payload.equals("true")) return true;
        if (payload.equals("enabled=false") || payload.equals("false")) return false;
        throw new IllegalArgumentException("Schedule enabled 값이 필요합니다.");
    }

    private static Object value(Map<String,?> map, String key) {
        Object value = map.get(key);
        if (value != null) return value;
        value = map.get(key.toUpperCase(Locale.ROOT));
        if (value != null) return value;
        String snake = key.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        value = map.get(snake);
        return value != null ? value : map.get(snake.toUpperCase(Locale.ROOT));
    }

    private static String text(Map<String,Object> map, String key) {
        Object value = value(map, key);
        if (value == null || String.valueOf(value).isBlank()) throw new IllegalArgumentException(key + " is required");
        return String.valueOf(value).trim();
    }

    private static String textOrEmpty(Map<String,Object> map, String key) {
        Object value = value(map, key);
        return value == null ? "" : String.valueOf(value);
    }

    private static Long longOrNull(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        long parsed = Long.parseLong(String.valueOf(value));
        if (parsed < 0) throw new IllegalArgumentException("expectedVersion은 음수일 수 없습니다.");
        return parsed;
    }

    private static AdmApprovedOperationResult failed(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, code, message);
    }

    private static ApprovalOwnerTuple tuple(String ownerCommand, String actionType, String targetType) {
        return new ApprovalOwnerTuple("BAT", ownerCommand, actionType, targetType);
    }


    private record ApprovalOwnerTuple(String ownerModule, String ownerCommand, String actionType, String targetType) { }
}
