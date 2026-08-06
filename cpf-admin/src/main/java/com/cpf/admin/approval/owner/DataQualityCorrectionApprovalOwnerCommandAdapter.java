package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.HexFormat;
import java.util.Map;
import java.util.NoSuchElementException;

/** Executes an immutable, server-approved data-quality correction snapshot. */
public final class DataQualityCorrectionApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private final CpfDataQualityOperations quality;
    private final ObjectMapper objectMapper;

    public DataQualityCorrectionApprovalOwnerCommandAdapter(CpfDataQualityOperations quality, ObjectMapper objectMapper) {
        this.quality = quality;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        return AdmIntegrationClosureService.DATA_QUALITY_OWNER.equalsIgnoreCase(ownerModule)
                && AdmIntegrationClosureService.DATA_QUALITY_COMMAND.equals(ownerCommand);
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (!AdmIntegrationClosureService.DATA_QUALITY_ACTION.equals(command.actionType())
                || !AdmIntegrationClosureService.DATA_QUALITY_TARGET.equals(command.targetType())) {
            return failed("DQ-APPROVAL-MISMATCH", "승인 Action/Target 유형이 데이터 정정과 일치하지 않습니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("DQ-SOD-VIOLATION", "요청자와 실행 승인자는 분리되어야 합니다.");
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(command.payloadSnapshot(), new TypeReference<>() {});
            String quarantineId = text(payload.get("quarantineId"));
            if (!command.targetId().equals(quarantineId)) {
                return failed("DQ-TARGET-MISMATCH", "승인 대상과 정정 대상이 일치하지 않습니다.");
            }
            long expectedVersion = number(payload.get("expectedVersion"));
            @SuppressWarnings("unchecked")
            Map<String, Object> corrected = payload.get("corrected") instanceof Map<?, ?> map
                    ? (Map<String, Object>) map : Map.of();
            if (expectedVersion < 1 || corrected.isEmpty()) {
                return failed("DQ-PAYLOAD-INVALID", "승인 Snapshot의 정정 정보가 올바르지 않습니다.");
            }
            CpfDataQualityOperations.QuarantineItem before = quality.quarantine(quarantineId)
                    .orElseThrow(() -> new NoSuchElementException(quarantineId));
            String beforeHash = hash(before.original(), before.corrected(), before.state(), before.version());
            CpfDataQualityOperations.QuarantineItem after = quality.correctAuthorized(
                    quarantineId,
                    expectedVersion,
                    Map.copyOf(corrected),
                    command.approvedBy(),
                    command.reason(),
                    new CpfDataQualityOperations.CorrectionAuthorization(
                            "ADM-APPROVAL:" + command.approvalRequestId() + ":" + command.commandRequestId(),
                            command.approvedBy(),
                            Instant.now()));
            String afterHash = hash(after.original(), after.corrected(), after.state(), after.version());
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.SUCCEEDED,
                    "DQ-CORRECTED",
                    "approvalId=" + command.approvalRequestId()
                            + ",target=" + quarantineId
                            + ",beforeHash=" + beforeHash
                            + ",afterHash=" + afterHash);
        } catch (JsonProcessingException invalidSnapshot) {
            return failed("DQ-PAYLOAD-JSON-INVALID", "승인 Snapshot JSON을 해석할 수 없습니다.");
        } catch (NoSuchElementException missing) {
            return failed("DQ-NOT-FOUND", "정정 대상 격리 데이터가 없습니다.");
        } catch (ConcurrentModificationException conflict) {
            return failed("DQ-VERSION-CONFLICT", "격리 데이터 버전 충돌이 감지되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException rejected) {
            return failed("DQ-CORRECTION-REJECTED", "격리 데이터 정정이 정책에 의해 거부되었습니다.");
        } catch (RuntimeException unknown) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN,
                    "DQ-CORRECTION-UNKNOWN",
                    "정정 호출 결과를 확정할 수 없습니다. Reconcile이 필요합니다.");
        }
    }

    private AdmApprovedOperationResult failed(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, code, message);
    }

    private static String text(Object value) {
        if (value == null || String.valueOf(value).isBlank()) throw new IllegalArgumentException("quarantineId is required");
        return String.valueOf(value).trim();
    }

    private static long number(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return 0;
        return Long.parseLong(String.valueOf(value));
    }

    private String hash(Object... values) {
        try {
            byte[] canonical = objectMapper.writeValueAsString(values).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
        } catch (Exception failure) {
            throw new IllegalStateException("audit hash generation failed", failure);
        }
    }
}
