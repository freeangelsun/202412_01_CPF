package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.reliability.AdmBrokerDlqReplayApprovalSnapshot;
import com.cpf.core.api.reliability.CpfReliabilityOperationsPort;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** 승인된 BROKER_DLQ_REPLAY 명령만 CPF reliability Owner에 전달하는 Adapter입니다. */
@Component("cpfStartersMessagingReliabilityJdbcApprovalOwnerCommandPort")
public final class BrokerReliabilityApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    static final String OWNER_MODULE = "cpf-starters-messaging-reliability-jdbc";
    static final String OWNER_COMMAND = "BROKER_DLQ_REPLAY";
    static final String TARGET_TYPE = "CPF_BROKER_DLQ";

    private final CpfReliabilityOperationsPort operations;
    private final AdmApprovalRepository approvals;
    private final Clock clock;

    public BrokerReliabilityApprovalOwnerCommandAdapter(
            CpfReliabilityOperationsPort operations,
            AdmApprovalRepository approvals) {
        this(operations, approvals, Clock.systemUTC());
    }

    BrokerReliabilityApprovalOwnerCommandAdapter(
            CpfReliabilityOperationsPort operations,
            AdmApprovalRepository approvals,
            Clock clock) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.approvals = Objects.requireNonNull(approvals, "approvals");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        return OWNER_MODULE.equals(Objects.toString(ownerModule, "").trim())
                && OWNER_COMMAND.equals(Objects.toString(ownerCommand, "").trim());
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        return supports(ownerModule, ownerCommand)
                && OWNER_COMMAND.equals(Objects.toString(actionType, "").trim())
                && TARGET_TYPE.equals(Objects.toString(targetType, "").trim());
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null) {
            return failed("BROKER_DLQ_COMMAND_REQUIRED", "승인 실행 명령이 없습니다.");
        }
        if (!supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("BROKER_DLQ_OWNER_MISMATCH", "Broker reliability Owner Command가 아닙니다.");
        }
        if (!OWNER_COMMAND.equals(command.actionType())) {
            return failed("BROKER_DLQ_ACTION_MISMATCH", "승인 Action과 Owner Command가 일치하지 않습니다.");
        }
        if (!TARGET_TYPE.equals(command.targetType())) {
            return failed("BROKER_DLQ_TARGET_MISMATCH", "승인 대상 유형이 DLQ가 아닙니다.");
        }
        if (same(command.requestedBy(), command.approvedBy())) {
            return failed("BROKER_DLQ_SELF_APPROVAL", "DLQ 재처리 요청자와 승인 실행자는 달라야 합니다.");
        }
        AdmApprovedOperationResult approvalValidation = validateApprovalLedger(command);
        if (approvalValidation != null) {
            return approvalValidation;
        }

        Map<String, Object> current = findDlqMessage(command.targetId());
        if (current == null) {
            return failed("BROKER_DLQ_NOT_FOUND", "승인 대상 DLQ를 찾을 수 없습니다.");
        }
        final AdmBrokerDlqReplayApprovalSnapshot.Snapshot snapshot;
        try {
            snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(current);
        } catch (RuntimeException invalidState) {
            return failed("BROKER_DLQ_STATE_INVALID", "DLQ 상태 Snapshot을 검증할 수 없습니다.");
        }
        if (!AdmBrokerDlqReplayApprovalSnapshot.sameHash(command.payloadHash(), snapshot.hash())) {
            return failed("BROKER_DLQ_APPROVAL_HASH_MISMATCH", "승인 Snapshot 이후 DLQ 상태가 변경되었습니다.");
        }

        try {
            operations.requestDlqReplay(command.targetId(), command.approvedBy(), command.reason());
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.SUCCEEDED,
                    "BROKER_DLQ_REPLAY_REQUESTED",
                    "승인된 DLQ 재처리가 요청되었습니다.");
        } catch (UnsupportedOperationException configurationFailure) {
            return failed("BROKER_DLQ_OWNER_NOT_CONFIGURED", "DLQ 재처리 Owner가 구성되지 않았습니다.");
        } catch (RuntimeException uncertain) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN,
                    "BROKER_DLQ_REPLAY_UNKNOWN",
                    "DLQ 재처리 결과를 확정할 수 없어 재확인이 필요합니다.");
        }
    }

    private Map<String, Object> findDlqMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return null;
        }
        String normalized = messageId.trim();
        return operations.findDlq(null, null, null, 1_000).stream()
                .filter(row -> normalized.equals(value(row, "messageId")))
                .findFirst()
                .orElse(null);
    }

    private AdmApprovedOperationResult validateApprovalLedger(AdmApprovedOperationCommand command) {
        Map<String, Object> request = approvals.findRequest(command.approvalRequestId()).orElse(null);
        if (request == null) {
            return failed("BROKER_DLQ_APPROVAL_NOT_FOUND", "승인 요청을 찾을 수 없습니다.");
        }
        if (!"EXECUTING".equals(upper(value(request, "approvalStatus")))) {
            return failed("BROKER_DLQ_APPROVAL_STATE_INVALID", "실행 예약된 승인 요청이 아닙니다.");
        }
        if (!same(value(request, "actionType"), command.actionType())
                || !same(value(request, "ownerModule"), command.ownerModule())
                || !same(value(request, "ownerCommand"), command.ownerCommand())
                || !same(value(request, "targetType"), command.targetType())
                || !same(value(request, "targetId"), command.targetId())
                || !same(value(request, "requestedBy"), command.requestedBy())
                || !same(value(request, "requestReason"), command.reason())
                || !same(value(request, "transactionId"), command.transactionId())
                || !same(value(request, "payloadHash"), command.payloadHash())) {
            return failed("BROKER_DLQ_APPROVAL_LEDGER_MISMATCH", "승인 원장과 실행 명령이 일치하지 않습니다.");
        }
        final Instant expireAt;
        try {
            expireAt = instant(valueObject(request, "expireAt"));
        } catch (RuntimeException invalidExpiry) {
            return failed("BROKER_DLQ_APPROVAL_EXPIRY_INVALID", "DLQ 재처리 승인 만료시각 형식이 올바르지 않습니다.");
        }
        if (expireAt == null) {
            return failed("BROKER_DLQ_APPROVAL_EXPIRY_REQUIRED", "DLQ 재처리 승인 만료시각이 없습니다.");
        }
        if (!expireAt.isAfter(clock.instant())) {
            return failed("BROKER_DLQ_APPROVAL_EXPIRED", "DLQ 재처리 승인이 만료되었습니다.");
        }
        List<Map<String, Object>> participants = approvals.findParticipants(command.approvalRequestId());
        boolean independentApproval = participants.stream()
                .anyMatch(row -> "APPROVED".equals(upper(value(row, "decisionStatus")))
                        && same(command.approvedBy(), value(row, "operatorId"))
                        && !same(command.requestedBy(), value(row, "operatorId")));
        if (!independentApproval) {
            return failed("BROKER_DLQ_EXECUTOR_APPROVAL_REQUIRED", "실행자가 승인한 독립 승인 결정이 없습니다.");
        }
        return null;
    }

    private static Object valueObject(Map<String, ?> row, String key) {
        String normalized = normalize(key);
        for (Map.Entry<String, ?> entry : row.entrySet()) {
            if (normalize(entry.getKey()).equals(normalized)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String value(Map<String, ?> row, String key) {
        Object value = valueObject(row, key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Instant instant(Object value) {
        if (value == null) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return null;
        try {
            return Instant.parse(text);
        } catch (RuntimeException ignored) {
            return Timestamp.valueOf(text).toInstant();
        }
    }

    private static boolean same(String left, String right) {
        return Objects.equals(Objects.toString(left, "").trim(), Objects.toString(right, "").trim());
    }

    private static AdmApprovedOperationResult failed(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, code, message);
    }

    private static String upper(String value) {
        return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
    }
}
