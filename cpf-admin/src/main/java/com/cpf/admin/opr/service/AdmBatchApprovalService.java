package com.cpf.admin.opr.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.batch.api.CpfBatchRiskCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.cpf.foundation.annotation.CpfService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * ADM 승인 원장의 APPROVED Snapshot을 실제 BAT Owner Command와 결합합니다.
 *
 * <p>사용자가 입력한 approvalRequestId만 신뢰하지 않습니다. Owner/Command/Target/Action,
 * Canonical payload hash, 만료, 승인 참여자 분리, 멱등 실행 ID를 모두 검증한 뒤
 * APPROVED -> EXECUTING을 CAS 전이합니다.</p>
 */
@CpfService
public class AdmBatchApprovalService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate jdbc;

    public AdmBatchApprovalService(@Qualifier("admJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public Reservation reserve(CpfBatchRiskCommand command) {
        Objects.requireNonNull(command, "command");
        long approvalId = parseApprovalId(command.approvalRequestId());
        Map<String,Object> row = approval(approvalId);
        validateSnapshot(row, command);

        Map<String,Object> existing = executionOrNull(approvalId);
        if (existing != null) {
            String existingCommandId = text(existing, "command_request_id");
            if (!command.idempotencyKey().equals(existingCommandId)) {
                throw new IllegalStateException("approval request is already bound to another idempotency key");
            }
            String state = text(existing, "execution_status").toUpperCase(Locale.ROOT);
            return new Reservation(approvalId, command.idempotencyKey(), state, true, text(row, "requested_by"));
        }

        String status = text(row, "approval_status").toUpperCase(Locale.ROOT);
        if (!"APPROVED".equals(status)) {
            throw new IllegalStateException("approval request is not APPROVED: " + status);
        }
        assertIndependentApproval(approvalId, text(row, "requested_by"), command.requestUser());
        long version = number(row, "version_no");
        int changed = jdbc.update(
                "UPDATE ADM_APPROVAL_REQUEST SET approval_status='EXECUTING', version_no=version_no+1, "
                        + "updated_by=?, updated_at=CURRENT_TIMESTAMP WHERE approval_request_id=? "
                        + "AND approval_status='APPROVED' AND version_no=?",
                command.requestUser(), approvalId, version);
        if (changed != 1) {
            throw new IllegalStateException("approval request state changed concurrently");
        }
        jdbc.update(
                "INSERT INTO ADM_APPROVAL_EXECUTION("
                        + "approval_request_id,command_request_id,execution_status,started_at,"
                        + "recovery_required_yn,created_by,updated_by) "
                        + "VALUES(?,?,'RUNNING',CURRENT_TIMESTAMP,'N',?,?)",
                approvalId, command.idempotencyKey(), command.requestUser(), command.requestUser());
        return new Reservation(approvalId, command.idempotencyKey(), "RUNNING", false, text(row, "requested_by"));
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public void complete(Reservation reservation, String operatorId) {
        finish(reservation, operatorId, "SUCCEEDED", "COMPLETED", "N", null, null);
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public void fail(Reservation reservation, String operatorId, String code, String message) {
        finish(reservation, operatorId, "FAILED", "FAILED", "N", code, message);
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public void unknown(Reservation reservation, String operatorId, String code, String message) {
        finish(reservation, operatorId, "UNKNOWN", "UNKNOWN", "Y", code, message);
    }

    private void finish(
            Reservation reservation,
            String operatorId,
            String executionStatus,
            String approvalStatus,
            String recoveryRequired,
            String code,
            String message) {
        int executionChanged = jdbc.update(
                "UPDATE ADM_APPROVAL_EXECUTION SET execution_status=?,owner_result_code=?,"
                        + "owner_result_message=?,completed_at=CURRENT_TIMESTAMP,recovery_required_yn=?,"
                        + "updated_by=?,updated_at=CURRENT_TIMESTAMP "
                        + "WHERE approval_request_id=? AND command_request_id=?",
                executionStatus, nullableTrim(code), sanitize(message), recoveryRequired,
                required(operatorId, "operatorId"), reservation.approvalRequestId(),
                reservation.commandRequestId());
        if (executionChanged != 1) {
            throw new IllegalStateException("approval execution ledger update failed");
        }
        int approvalChanged = jdbc.update(
                "UPDATE ADM_APPROVAL_REQUEST SET approval_status=?,version_no=version_no+1,"
                        + "updated_by=?,updated_at=CURRENT_TIMESTAMP "
                        + "WHERE approval_request_id=? AND approval_status IN ('EXECUTING','UNKNOWN','COMPLETED','FAILED')",
                approvalStatus, operatorId, reservation.approvalRequestId());
        if (approvalChanged != 1) {
            throw new IllegalStateException("approval request final state update failed");
        }
    }

    private Map<String,Object> approval(long approvalId) {
        try {
            return jdbc.queryForMap(
                    "SELECT approval_request_id,action_type,owner_module,owner_command,target_type,target_id,"
                            + "requested_by,command_payload_hash,approval_status,expire_at,version_no "
                            + "FROM ADM_APPROVAL_REQUEST WHERE approval_request_id=?",
                    approvalId);
        } catch (EmptyResultDataAccessException missing) {
            throw new IllegalArgumentException("approval request does not exist: " + approvalId, missing);
        }
    }

    private Map<String,Object> executionOrNull(long approvalId) {
        try {
            return jdbc.queryForMap(
                    "SELECT approval_request_id,command_request_id,execution_status,recovery_required_yn "
                            + "FROM ADM_APPROVAL_EXECUTION WHERE approval_request_id=?",
                    approvalId);
        } catch (EmptyResultDataAccessException missing) {
            return null;
        }
    }

    private void validateSnapshot(Map<String,Object> row, CpfBatchRiskCommand command) {
        equalIgnoreCase(row, "owner_module", "BAT");
        equalIgnoreCase(row, "owner_command", command.operation());
        equalIgnoreCase(row, "action_type", command.actionType());
        equalIgnoreCase(row, "target_type", command.targetType());
        if (!command.targetId().equals(text(row, "target_id"))) {
            throw new IllegalArgumentException("approval target id does not match BAT command");
        }
        String approvedHash = text(row, "command_payload_hash");
        CpfBatchRiskCommand approvedSnapshot = withRequestUser(command, text(row, "requested_by"));
        if (!approvedHash.equalsIgnoreCase(approvedSnapshot.fingerprint())) {
            throw new IllegalArgumentException("approval payload hash does not match BAT command");
        }
        Instant expires = instant(value(row, "expire_at"));
        if (expires == null) {
            throw new IllegalStateException("approval expiry is required for BAT risk commands");
        }
        if (!expires.isAfter(Instant.now())) {
            throw new IllegalStateException("approval request has expired");
        }
    }

    /**
     * 승인 Snapshot의 requester와 현재 실행자를 분리합니다. Controller의 requestUser는 현재
     * 승인 실행자이며, Owner 전송용 fingerprint는 원장의 requested_by를 기준으로 검증합니다.
     */
    public static CpfBatchRiskCommand withRequestUser(CpfBatchRiskCommand command, String requestUser) {
        Objects.requireNonNull(command, "command");
        return new CpfBatchRiskCommand(
                command.operation(), command.targetType(), command.targetId(), command.actionType(),
                required(requestUser, "requestUser"), command.reason(), command.approvalRequestId(),
                command.idempotencyKey(), command.expectedVersion(), command.payload());
    }

    private void assertIndependentApproval(long approvalId, String requesterId, String executorId) {
        String executor = required(executorId, "executorId");
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ADM_APPROVAL_PARTICIPANT "
                        + "WHERE approval_request_id=? AND decision_status='APPROVED' "
                        + "AND operator_id=? AND operator_id<>?",
                Long.class, approvalId, executor, requesterId);
        if (count == null || count < 1) {
            throw new IllegalStateException(
                    "the executing operator must be an independent approved participant");
        }
    }

    private static void equalIgnoreCase(Map<String,Object> row, String key, String expected) {
        if (!text(row, key).equalsIgnoreCase(expected)) {
            throw new IllegalArgumentException("approval " + key + " does not match BAT command");
        }
    }

    private static long parseApprovalId(String value) {
        try {
            long parsed = Long.parseLong(required(value, "approvalRequestId"));
            if (parsed <= 0) throw new NumberFormatException("non-positive");
            return parsed;
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("approvalRequestId must be a positive numeric id", invalid);
        }
    }

    private static Instant instant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        if (value instanceof OffsetDateTime offset) return offset.toInstant();
        if (value instanceof LocalDateTime local) return local.toInstant(ZoneOffset.UTC);
        throw new IllegalStateException("unsupported approval expiry type: " + value.getClass().getName());
    }

    private static Object value(Map<String,Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value : row.get(key.toUpperCase(Locale.ROOT));
    }

    private static String text(Map<String,Object> row, String key) {
        Object value = value(row, key);
        return required(value == null ? null : String.valueOf(value), key);
    }

    private static long number(Map<String,Object> row, String key) {
        Object value = row.get(key);
        if (value == null) value = row.get(key.toUpperCase(Locale.ROOT));
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(required(value == null ? null : String.valueOf(value), key));
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static String nullableTrim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) return null;
        String sanitized = value.replaceAll("(?i)(password|secret|token|authorization)\\s*[:=]\\s*[^,;\\s]+", "$1=***");
        return sanitized.length() <= 1000 ? sanitized : sanitized.substring(0, 1000);
    }

    public record Reservation(
            long approvalRequestId,
            String commandRequestId,
            String executionStatus,
            boolean replay,
            String requestedBy) {
        public Reservation {
            commandRequestId = required(commandRequestId, "commandRequestId");
            executionStatus = required(executionStatus, "executionStatus");
            requestedBy = required(requestedBy, "requestedBy");
        }
    }
}
