package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** 정식 ADM Approval Engine에서 승인된 BAT Runtime 위험조치를 BAT Owner Port로 전달합니다. */
@Component("cpfBatchRuntimeApprovalOwnerCommandPort")
public final class BatchRuntimeApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private static final Set<String> COMMANDS = Set.of(
            "releaseLock", "actGhostExecution", "requestRetry", "requestStop",
            "updateScheduleEnabled", "requestRun", "runSchedulerOnce");

    private final CpfBatchOperationsPort batch;
    private final ObjectMapper objectMapper;

    public BatchRuntimeApprovalOwnerCommandAdapter(CpfBatchOperationsPort batch, ObjectMapper objectMapper) {
        this.batch = Objects.requireNonNull(batch, "batch");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        String owner = normalize(ownerModule);
        return (owner.equals("bat") || owner.contains("batch")) && COMMANDS.contains(ownerCommand);
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand())) {
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

    private CpfBatchRiskCommand approvedRisk(AdmApprovedOperationCommand command) {
        Map<String,Object> snapshot = read(command.payloadSnapshot());
        CpfBatchRiskCommand risk = new CpfBatchRiskCommand(
                text(snapshot, "operation"), text(snapshot, "targetType"), text(snapshot, "targetId"),
                text(snapshot, "actionType"), text(snapshot, "requestUser"), text(snapshot, "reason"),
                text(snapshot, "approvalRequestId"), text(snapshot, "idempotencyKey"),
                longOrNull(value(snapshot, "expectedVersion")), textOrEmpty(snapshot, "payload"));
        if (!risk.fingerprint().equalsIgnoreCase(command.payloadHash())
                || !risk.operation().equals(command.ownerCommand())
                || !risk.targetType().equalsIgnoreCase(expectedTargetType(command.ownerCommand()))
                || !risk.targetId().equals(command.targetId())
                || !risk.actionType().equalsIgnoreCase(command.actionType())
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

    private static Object value(Map<String,Object> map, String key) {
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

    private static String normalize(String value) {
        return Objects.toString(value, "").replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
    }
}
