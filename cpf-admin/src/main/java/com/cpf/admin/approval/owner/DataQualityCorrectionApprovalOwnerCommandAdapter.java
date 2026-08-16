package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.security.AdmDataQualityApprovalProofService;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import com.cpf.data.api.quality.CpfDataQualityOperations;
import com.cpf.data.spi.quality.CpfDataQualityCorrectionPort;
import com.cpf.admin.approval.security.AdmDataQualityCorrectionGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Executes only a database-reserved, immutable data-quality correction command. */
public final class DataQualityCorrectionApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private final AdmDataQualityCorrectionGateway correctionGateway;
    private final CpfDataQualityOperations qualityQuery;
    private final ObjectMapper objectMapper;
    private final AdmApprovalRepository repository;
    private final AdmApprovalSnapshotIntegrity snapshotIntegrity;
    private final AdmDataQualityApprovalProofService proofService;

    public DataQualityCorrectionApprovalOwnerCommandAdapter(
            AdmDataQualityCorrectionGateway correctionGateway,
            CpfDataQualityOperations qualityQuery,
            ObjectMapper objectMapper,
            AdmApprovalRepository repository,
            AdmApprovalSnapshotIntegrity snapshotIntegrity,
            AdmDataQualityApprovalProofService proofService) {
        this.correctionGateway = Objects.requireNonNull(correctionGateway, "correctionGateway");
        this.qualityQuery = Objects.requireNonNull(qualityQuery, "qualityQuery");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.snapshotIntegrity = Objects.requireNonNull(snapshotIntegrity, "snapshotIntegrity");
        this.proofService = Objects.requireNonNull(proofService, "proofService");
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        return AdmIntegrationClosureService.DATA_QUALITY_OWNER.equals(ownerModule)
                && AdmIntegrationClosureService.DATA_QUALITY_COMMAND.equals(ownerCommand);
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        return supports(ownerModule, ownerCommand)
                && AdmIntegrationClosureService.DATA_QUALITY_ACTION.equals(actionType)
                && AdmIntegrationClosureService.DATA_QUALITY_TARGET.equals(targetType);
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        Map<String, Object> reserved = repository.findReservedExecutionCommand(
                        command.approvalRequestId(), command.commandRequestId())
                .orElse(null);
        if (reserved == null) {
            return failed("DQ-EXECUTION-NOT-RESERVED", "서버가 단회 예약한 승인 실행이 아닙니다.");
        }
        if (!matchesReservedCommand(command, reserved)) {
            return failed("DQ-EXECUTION-ENVELOPE-MISMATCH", "예약된 승인 실행 Envelope와 Owner 명령이 일치하지 않습니다.");
        }
        AdmApprovalSnapshotIntegrity.Verification verification = snapshotIntegrity.verify(reserved);
        if (!verification.valid()) {
            return failed("DQ-SNAPSHOT-HASH-MISMATCH", "승인 Snapshot 무결성 검증에 실패했습니다.");
        }
        if (!AdmIntegrationClosureService.DATA_QUALITY_ACTION.equals(command.actionType())
                || !AdmIntegrationClosureService.DATA_QUALITY_TARGET.equals(command.targetType())) {
            return failed("DQ-APPROVAL-MISMATCH", "승인 Action/Target 유형이 데이터 정정과 일치하지 않습니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("DQ-SOD-VIOLATION", "요청자와 실행 승인자는 분리되어야 합니다.");
        }
        if (!repository.isApprovedParticipant(command.approvalRequestId(), command.approvedBy())) {
            return failed("DQ-APPROVER-NOT-PARTICIPANT", "승인 참여자가 아닌 실행자입니다.");
        }
        Instant expiresAt = instant(reserved.get("expireAt"));
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            return failed("DQ-APPROVAL-EXPIRED", "만료된 승인 실행입니다.");
        }
        try {
            String reservedSnapshot = Objects.toString(reserved.get("payloadSnapshot"), "{}");
            Map<String, Object> payload = objectMapper.readValue(reservedSnapshot, new TypeReference<>() { });
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
            CpfDataQualityOperations.QuarantineItem before = qualityQuery.quarantine(quarantineId)
                    .orElseThrow(() -> new NoSuchElementException(quarantineId));
            String beforeHash = hash(before.original(), before.corrected(), before.state(), before.version());
            String approvalRef = "ADM-APPROVAL:" + command.approvalRequestId() + ":" + command.commandRequestId();
            AdmDataQualityApprovalProofService.IssuedCapability capability = proofService.issue(
                    quarantineId, expectedVersion, approvalRef, command.payloadHash());
            CpfDataQualityOperations.QuarantineItem after = correctionGateway.correctApproved(
                    new CpfDataQualityCorrectionPort.ApprovedCorrection(
                            quarantineId,
                            expectedVersion,
                            Collections.unmodifiableMap(new LinkedHashMap<>(corrected)),
                            command.approvedBy(),
                            command.reason(),
                            approvalRef,
                            command.payloadHash(),
                            capability.nonce(),
                            capability.proof(),
                            capability.approvedAt()));
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


    @Override
    public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        Map<String, Object> reserved = repository.findReservedExecutionCommand(
                        command.approvalRequestId(), command.commandRequestId())
                .orElse(null);
        if (reserved == null) {
            return failed("DQ-RECONCILE-NOT-RESERVED", "서버가 예약한 UNKNOWN Reconcile이 아닙니다.");
        }
        if (!matchesReservedCommand(command, reserved)) {
            return failed("DQ-RECONCILE-ENVELOPE-MISMATCH", "예약된 Reconcile Envelope가 일치하지 않습니다.");
        }
        AdmApprovalSnapshotIntegrity.Verification verification = snapshotIntegrity.verify(reserved);
        if (!verification.valid()) {
            return failed("DQ-SNAPSHOT-HASH-MISMATCH", "승인 Snapshot 무결성 검증에 실패했습니다.");
        }
        if (!AdmIntegrationClosureService.DATA_QUALITY_ACTION.equals(command.actionType())
                || !AdmIntegrationClosureService.DATA_QUALITY_TARGET.equals(command.targetType())) {
            return failed("DQ-RECONCILE-APPROVAL-MISMATCH", "승인 Action/Target 유형이 데이터 정정과 일치하지 않습니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("DQ-RECONCILE-SOD-VIOLATION", "요청자와 Reconcile 운영자는 분리되어야 합니다.");
        }
        if (!repository.isApprovedParticipant(command.approvalRequestId(), command.approvedBy())) {
            return failed("DQ-RECONCILE-APPROVER-NOT-PARTICIPANT", "승인 참여자가 아닌 운영자입니다.");
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(
                    Objects.toString(reserved.get("payloadSnapshot"), "{}"), new TypeReference<>() { });
            String quarantineId = text(payload.get("quarantineId"));
            long expectedVersion = number(payload.get("expectedVersion"));
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedCorrected = payload.get("corrected") instanceof Map<?, ?> map
                    ? (Map<String, Object>) map : Map.of();
            CpfDataQualityOperations.QuarantineItem current = qualityQuery.quarantine(quarantineId).orElse(null);
            if (current == null) {
                return unknown("DQ-RECONCILE-TARGET-MISSING", "대상이 없어 Side Effect를 확정할 수 없습니다.");
            }
            String currentHash = hash(current.original(), current.corrected(), current.state(), current.version());
            if (("CORRECTED".equals(current.state()) || "REPLAYED".equals(current.state()))
                    && current.version() == expectedVersion + 1
                    && canonicalMapEquals(expectedCorrected, current.corrected())) {
                return new AdmApprovedOperationResult(
                        AdmApprovalExecutionStatus.RECOVERED,
                        "DQ-CORRECTED-RECONCILED",
                        "approvalId=" + command.approvalRequestId()
                                + ",target=" + quarantineId
                                + ",currentHash=" + currentHash
                                + ",reconciliation=SIDE_EFFECT_APPLIED");
            }
            if ("QUARANTINED".equals(current.state())
                    && current.version() == expectedVersion
                    && current.corrected().isEmpty()) {
                return new AdmApprovedOperationResult(
                        AdmApprovalExecutionStatus.FAILED,
                        "DQ-CORRECTION-NOT-APPLIED",
                        "approvalId=" + command.approvalRequestId()
                                + ",target=" + quarantineId
                                + ",currentHash=" + currentHash
                                + ",reconciliation=SIDE_EFFECT_NOT_APPLIED");
            }
            return unknown("DQ-RECONCILE-AMBIGUOUS",
                    "현재 상태가 승인 Snapshot과 일치하지 않아 UNKNOWN을 유지합니다. currentHash=" + currentHash);
        } catch (JsonProcessingException | IllegalArgumentException invalid) {
            return unknown("DQ-RECONCILE-SNAPSHOT-INVALID", "Reconcile Snapshot을 해석할 수 없어 UNKNOWN을 유지합니다.");
        } catch (RuntimeException failure) {
            return unknown("DQ-RECONCILE-UNKNOWN", "Owner 상태 조회 결과를 확정할 수 없어 UNKNOWN을 유지합니다.");
        }
    }

    private boolean matchesReservedCommand(AdmApprovedOperationCommand command, Map<String, Object> reserved) {
        return equalsText(command.actionType(), reserved.get("actionType"))
                && equalsText(command.ownerModule(), reserved.get("ownerModule"))
                && equalsText(command.ownerCommand(), reserved.get("ownerCommand"))
                && equalsText(command.targetType(), reserved.get("targetType"))
                && equalsText(command.targetId(), reserved.get("targetId"))
                && equalsText(command.requestedBy(), reserved.get("requestedBy"))
                && equalsText(command.transactionId(), reserved.get("transactionId"))
                && snapshotIntegrity.constantTimeEquals(
                        snapshotIntegrity.sha256Canonical(snapshotIntegrity.canonicalPayload(command.payloadSnapshot())),
                        snapshotIntegrity.sha256Canonical(snapshotIntegrity.canonicalPayload(
                                Objects.toString(reserved.get("payloadSnapshot"), "{}"))))
                && snapshotIntegrity.constantTimeEquals(command.payloadHash(), Objects.toString(reserved.get("payloadHash"), ""))
                && snapshotIntegrity.constantTimeEquals(
                        snapshotIntegrity.hash(reserved),
                        Objects.toString(reserved.get("payloadHash"), ""));
    }


    private boolean canonicalMapEquals(Map<String, Object> left, Map<String, Object> right) {
        try {
            String leftJson = objectMapper.writeValueAsString(left == null ? Map.of() : left);
            String rightJson = objectMapper.writeValueAsString(right == null ? Map.of() : right);
            return snapshotIntegrity.constantTimeEquals(
                    snapshotIntegrity.sha256Canonical(snapshotIntegrity.canonicalPayload(leftJson)),
                    snapshotIntegrity.sha256Canonical(snapshotIntegrity.canonicalPayload(rightJson)));
        } catch (JsonProcessingException failure) {
            return false;
        }
    }

    private AdmApprovedOperationResult unknown(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN, code, message);
    }

    private AdmApprovedOperationResult failed(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, code, message);
    }

    private static boolean equalsText(String expected, Object actual) {
        return expected != null && expected.equals(String.valueOf(actual == null ? "" : actual).trim());
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

    private static Instant instant(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        return Instant.parse(String.valueOf(value));
    }

    private String hash(Object... values) {
        try {
            Map<String, Object> auditState = new java.util.LinkedHashMap<>();
            auditState.put("original", values.length > 0 ? values[0] : null);
            auditState.put("corrected", values.length > 1 ? values[1] : null);
            auditState.put("state", values.length > 2 ? values[2] : null);
            auditState.put("version", values.length > 3 ? values[3] : null);
            String json = objectMapper.writeValueAsString(auditState);
            return snapshotIntegrity.sha256Canonical(snapshotIntegrity.canonicalPayload(json));
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("audit hash generation failed", failure);
        }
    }
}
