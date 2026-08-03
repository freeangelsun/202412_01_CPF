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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
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

    public BatchRuntimeApprovalOwnerCommandAdapter(
            CpfBatchOperationsPort batch, ObjectMapper objectMapper) {
        this.batch = batch;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        String owner = normalize(ownerModule);
        return (owner.equals("bat") || owner.contains("batch")) && COMMANDS.contains(ownerCommand);
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (!supports(command.ownerModule(), command.ownerCommand())) {
            return failed("BAT_COMMAND_UNSUPPORTED", "지원하지 않는 BAT Runtime 승인 Command입니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("BAT_SELF_APPROVAL", "요청자와 승인 실행자는 달라야 합니다.");
        }
        if (!sha256(command.payloadSnapshot()).equalsIgnoreCase(command.payloadHash())) {
            return failed("BAT_APPROVAL_HASH_MISMATCH", "승인 Payload Snapshot hash가 일치하지 않습니다.");
        }
        Map<String,Object> payload = read(command.payloadSnapshot());
        Long expectedVersion = longOrNull(value(payload, "expectedVersion"));
        String targetType = expectedTargetType(command.ownerCommand());
        String targetId = command.ownerCommand().equals("runSchedulerOnce")
                ? "DUE_SCHEDULES" : command.targetId();
        String actionType = command.actionType().toUpperCase(Locale.ROOT);
        String ownerPayload = ownerPayload(command.ownerCommand(), actionType, payload);
        CpfBatchRiskCommand risk = new CpfBatchRiskCommand(
                command.ownerCommand(), targetType, targetId, actionType,
                command.approvedBy(), command.reason(), String.valueOf(command.approvalRequestId()),
                command.commandRequestId(), expectedVersion, ownerPayload);
        try {
            switch (command.ownerCommand()) {
                case "releaseLock" -> batch.releaseLock(command.targetId(), risk);
                case "actGhostExecution" -> batch.actGhostExecution(
                        Long.parseLong(command.targetId()), ghostAction(actionType, payload), risk);
                case "requestRetry" -> batch.requestRetry(Long.parseLong(command.targetId()), risk);
                case "requestStop" -> batch.requestStop(Long.parseLong(command.targetId()), risk);
                case "updateScheduleEnabled" -> batch.updateScheduleEnabled(
                        command.targetId(), scheduleEnabled(actionType, payload), risk);
                case "requestRun" -> batch.requestRun(command.targetId(), ownerPayload, risk);
                case "runSchedulerOnce" -> batch.runSchedulerOnce(risk);
                default -> throw new IllegalArgumentException("unsupported BAT command");
            }
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.SUCCEEDED, "BAT_COMMAND_SUCCEEDED", "BAT Runtime 위험조치 완료");
        } catch (CpfBatchOwnerUnknownResultException unresolved) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN, unresolved.code(), "BAT Runtime 결과 재확인이 필요합니다.");
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            return failed("BAT_COMMAND_REJECTED", "BAT Runtime 위험조치가 상태·버전 검증에서 거부되었습니다.");
        } catch (RuntimeException unexpected) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN, "BAT_COMMAND_UNKNOWN", "BAT Runtime 결과를 확정할 수 없습니다.");
        }
    }

    private Map<String,Object> read(String snapshot) {
        try {
            Map<String,Object> value = objectMapper.readValue(snapshot, new TypeReference<>() {});
            return value == null ? Map.of() : value;
        } catch (Exception invalid) {
            throw new IllegalArgumentException("승인 Payload Snapshot JSON이 올바르지 않습니다.", invalid);
        }
    }

    private String ownerPayload(String command, String actionType, Map<String,Object> payload) {
        return switch (command) {
            case "requestRun" -> jsonOrText(value(payload, "jobParameters"));
            case "actGhostExecution" -> ghostAction(actionType, payload);
            case "updateScheduleEnabled" -> scheduleEnabled(actionType, payload)
                    ? "enabled=true" : "enabled=false";
            default -> "";
        };
    }

    private String jsonOrText(Object value) {
        if (value == null) return "{}";
        if (value instanceof String text) return text;
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception invalid) { throw new IllegalArgumentException("jobParameters 직렬화 실패", invalid); }
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

    private static String ghostAction(String actionType, Map<String,Object> payload) {
        if (actionType.startsWith("BATCH_GHOST_")) {
            return actionType.substring("BATCH_GHOST_".length());
        }
        Object action = value(payload, "actionType");
        if (action == null || String.valueOf(action).isBlank()) {
            throw new IllegalArgumentException("Ghost actionType이 필요합니다.");
        }
        return String.valueOf(action).trim().toUpperCase(Locale.ROOT);
    }

    private static boolean scheduleEnabled(String actionType, Map<String,Object> payload) {
        if (actionType.endsWith("ENABLE")) return true;
        if (actionType.endsWith("DISABLE")) return false;
        Object enabled = value(payload, "enabled");
        if (enabled instanceof Boolean flag) return flag;
        if (enabled != null && Set.of("true", "false").contains(String.valueOf(enabled).toLowerCase(Locale.ROOT))) {
            return Boolean.parseBoolean(String.valueOf(enabled));
        }
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

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
