package com.cpf.batch.control.compat;

import com.cpf.batch.api.CpfBatchRiskCommand;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;

/** BAT Owner 위험조치 멱등/복구 Ledger. 모든 상태 변경은 REQUIRES_NEW로 보존합니다. */
@Component
public final class JdbcBatchRiskCommandLedger {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate requiresNew;
    private final Duration inProgressTimeout;

    public JdbcBatchRiskCommandLedger(
            JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.inProgressTimeout = Duration.ofMinutes(10);
    }

    public Decision reserve(CpfBatchRiskCommand command) {
        Map<String,Object> existing = find(command.idempotencyKey());
        if (existing != null) return decision(existing, command);
        try {
            requiresNew.executeWithoutResult(status -> jdbc.update(
                    "INSERT INTO bat_operation_request("
                            + "idempotency_key,request_hash,operation_type,target_type,target_id,action_type,"
                            + "approval_request_id,requested_by,expected_version,request_state,created_by,updated_by) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,'RESERVED',?,?)",
                    command.idempotencyKey(), command.fingerprint(), command.operation(),
                    command.targetType(), command.targetId(), command.actionType(),
                    command.approvalRequestId(), command.requestUser(), command.expectedVersion(),
                    command.requestUser(), command.requestUser()));
            return Decision.created();
        } catch (DataIntegrityViolationException raced) {
            Map<String,Object> winner = find(command.idempotencyKey());
            if (winner == null) throw raced;
            return decision(winner, command);
        }
    }

    public void complete(CpfBatchRiskCommand command, String resultPayload) {
        update(command, "COMPLETED", resultPayload, null, null);
    }

    public void fail(CpfBatchRiskCommand command, String code, String message) {
        update(command, "FAILED", null, code, message);
    }

    public void unknown(CpfBatchRiskCommand command, String code, String message) {
        update(command, "UNKNOWN", null, code, message);
    }

    private void update(
            CpfBatchRiskCommand command,
            String state,
            String resultPayload,
            String failureCode,
            String failureMessage) {
        Integer changed = requiresNew.execute(status -> jdbc.update(
                "UPDATE bat_operation_request SET request_state=?,result_payload=?,failure_code=?,"
                        + "failure_message=?,completed_at=CURRENT_TIMESTAMP,updated_by=?,updated_at=CURRENT_TIMESTAMP "
                        + "WHERE idempotency_key=? AND request_hash=? AND request_state IN ('RESERVED','UNKNOWN')",
                state, resultPayload, trim(failureCode, 80), trim(failureMessage, 1000),
                command.requestUser(), command.idempotencyKey(), command.fingerprint()));
        if (changed == null || changed != 1) {
            Map<String,Object> current = find(command.idempotencyKey());
            if (current != null && state.equalsIgnoreCase(text(current, "request_state"))
                    && command.fingerprint().equalsIgnoreCase(text(current, "request_hash"))) return;
            throw new IllegalStateException("BAT risk command ledger finalization failed: " + command.idempotencyKey());
        }
    }

    private Decision decision(Map<String,Object> row, CpfBatchRiskCommand command) {
        String hash = text(row, "request_hash");
        if (!command.fingerprint().equalsIgnoreCase(hash)) {
            return Decision.conflict("idempotency key is already bound to another BAT command");
        }
        String state = text(row, "request_state").toUpperCase(Locale.ROOT);
        return switch (state) {
            case "COMPLETED" -> Decision.replay(nullableText(row, "result_payload"));
            case "FAILED" -> Decision.failed(nullableText(row, "failure_code"), nullableText(row, "failure_message"));
            case "UNKNOWN" -> Decision.unknown(nullableText(row, "failure_code"), nullableText(row, "failure_message"));
            case "RESERVED" -> isStale(row)
                    ? Decision.unknown("STALE_RESERVED", "previous BAT command outcome requires reconciliation")
                    : Decision.inProgress();
            default -> Decision.unknown("INVALID_LEDGER_STATE", "unsupported BAT risk ledger state: " + state);
        };
    }

    private boolean isStale(Map<String,Object> row) {
        Instant updated = instant(value(row, "updated_at"));
        return updated != null && updated.plus(inProgressTimeout).isBefore(Instant.now());
    }

    private Map<String,Object> find(String idempotencyKey) {
        try {
            return jdbc.queryForMap(
                    "SELECT idempotency_key,request_hash,request_state,result_payload,failure_code,"
                            + "failure_message,updated_at FROM bat_operation_request WHERE idempotency_key=?",
                    idempotencyKey);
        } catch (EmptyResultDataAccessException missing) {
            return null;
        }
    }

    private static Object value(Map<String,Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value : row.get(key.toUpperCase(Locale.ROOT));
    }
    private static String text(Map<String,Object> row, String key) {
        String value = nullableText(row,key);
        if (value == null) throw new IllegalStateException(key + " is missing from BAT risk ledger");
        return value;
    }
    private static String nullableText(Map<String,Object> row, String key) {
        Object value = value(row,key);
        return value == null ? null : String.valueOf(value);
    }
    private static Instant instant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        if (value instanceof OffsetDateTime offset) return offset.toInstant();
        if (value instanceof LocalDateTime local) return local.toInstant(ZoneOffset.UTC);
        return null;
    }
    private static String trim(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String sanitized = value.replaceAll("(?i)(password|secret|token|authorization)\\s*[:=]\\s*[^,;\\s]+", "$1=***");
        return sanitized.length() <= max ? sanitized : sanitized.substring(0,max);
    }

    public record Decision(Kind kind, String resultPayload, String code, String message) {
        static Decision created(){return new Decision(Kind.CREATED,null,null,null);}
        static Decision replay(String value){return new Decision(Kind.REPLAY,value,null,null);}
        static Decision conflict(String message){return new Decision(Kind.CONFLICT,null,"IDEMPOTENCY_CONFLICT",message);}
        static Decision inProgress(){return new Decision(Kind.IN_PROGRESS,null,"COMMAND_IN_PROGRESS","BAT risk command is already in progress");}
        static Decision failed(String code,String message){return new Decision(Kind.FAILED,null,code,message);}
        static Decision unknown(String code,String message){return new Decision(Kind.UNKNOWN,null,code,message);}
    }
    public enum Kind { CREATED, REPLAY, CONFLICT, IN_PROGRESS, FAILED, UNKNOWN }
}
