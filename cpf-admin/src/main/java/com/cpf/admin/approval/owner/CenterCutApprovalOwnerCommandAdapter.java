package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.centercut.AdmCenterCutCommandClient;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Canonical ADM Approval Engine에서 승인된 Center-Cut execution-scope 명령을 BAT Owner로 전달합니다. */
@Component("cpfCenterCutApprovalOwnerCommandPort")
public final class CenterCutApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private static final Set<ApprovalOwnerTuple> ALLOWED = Set.of(
            new ApprovalOwnerTuple("BAT", "reprocessCenterCutFailed", "CENTER_CUT_REPROCESS_FAILED", "center_cut_execution"),
            new ApprovalOwnerTuple("BAT", "reconcileCenterCutUnknown", "CENTER_CUT_RECONCILE_UNKNOWN", "center_cut_execution"));

    private final AdmCenterCutCommandClient owner;
    private final ObjectMapper objectMapper;

    public CenterCutApprovalOwnerCommandAdapter(
            AdmCenterCutCommandClient owner, ObjectMapper objectMapper) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
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
                Objects.toString(ownerModule, "").trim(), Objects.toString(ownerCommand, "").trim(),
                Objects.toString(actionType, "").trim(), Objects.toString(targetType, "").trim());
        return ALLOWED.contains(candidate);
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("CENTER_CUT_COMMAND_UNSUPPORTED", "지원하지 않는 Center-Cut 승인 Command입니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("CENTER_CUT_SELF_APPROVAL", "요청자와 승인 실행자는 달라야 합니다.");
        }
        final CpfBatchRiskCommand risk;
        try {
            risk = approvedRisk(command);
        } catch (IllegalArgumentException invalid) {
            return failed("CENTER_CUT_APPROVAL_SNAPSHOT_MISMATCH",
                    "승인된 Center-Cut Snapshot이 실행 명령과 일치하지 않습니다.");
        }
        try {
            Map<String,Object> result = switch (command.ownerCommand()) {
                case "reprocessCenterCutFailed" -> owner.reprocessFailed(risk.targetId(), risk);
                case "reconcileCenterCutUnknown" -> owner.reconcileUnknown(risk.targetId(), risk);
                default -> throw new IllegalArgumentException("unsupported Center-Cut command");
            };
            if (result == null) {
                return new AdmApprovedOperationResult(
                        AdmApprovalExecutionStatus.UNKNOWN, "CENTER_CUT_OWNER_NULL",
                        "BAT Center-Cut Owner가 결과를 반환하지 않았습니다.");
            }
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.SUCCEEDED, "CENTER_CUT_COMMAND_SUCCEEDED",
                    "Center-Cut execution-scope 위험조치 완료");
        } catch (CpfBatchOwnerUnknownResultException unresolved) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN, unresolved.failureCode(),
                    "BAT Center-Cut 결과 재확인이 필요합니다.");
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            return failed("CENTER_CUT_COMMAND_REJECTED",
                    "BAT Center-Cut 위험조치가 상태 검증에서 거부되었습니다.");
        } catch (RuntimeException unexpected) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN, "CENTER_CUT_COMMAND_UNKNOWN",
                    "BAT Center-Cut 결과를 확정할 수 없습니다.");
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
                || !risk.targetType().equals("center_cut_execution")
                || !risk.targetId().equals(command.targetId())
                || !risk.actionType().equals(command.actionType())
                || !risk.requestUser().equals(command.requestedBy())
                || !risk.approvalRequestId().equals(String.valueOf(command.approvalRequestId()))) {
            throw new IllegalArgumentException("approved Center-Cut snapshot mismatch");
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
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
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


    private record ApprovalOwnerTuple(String ownerModule, String ownerCommand, String actionType, String targetType) { }

}
