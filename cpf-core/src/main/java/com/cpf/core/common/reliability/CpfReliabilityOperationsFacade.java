package com.cpf.core.common.reliability;

import com.cpf.core.api.reliability.CpfReliabilityOperationsPort;
import com.cpf.core.common.exception.CpfValidationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CPF 신뢰성 테이블의 조회와 상태 변경을 소유하는 공개 파사드 구현입니다.
 */
@Service
public class CpfReliabilityOperationsFacade implements CpfReliabilityOperationsPort {
    private static final Set<String> RESOLUTION_STATUSES = Set.of(
            "CHECK_PENDING",
            "CHECKING",
            "CONFIRMED_SUCCESS",
            "CONFIRMED_FAILURE",
            "RETRY_PENDING",
            "MANUAL_REVIEW",
            "RESOLVED");

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    public CpfReliabilityOperationsFacade(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    @Override
    public List<Map<String, Object>> findIdempotency(String scope, String status, String key, int limit) {
        if (!available()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT idempotency_seq, scope, idempotency_key, request_hash, payload_hash,
                       record_status, retry_allowed_yn, completed_at, expires_at, created_at, updated_at
                FROM cpf_idempotency_record
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "scope", scope);
        appendEquals(sql, args, "record_status", status);
        appendLike(sql, args, "idempotency_key", key);
        sql.append(" ORDER BY idempotency_seq DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    @Override
    public List<Map<String, Object>> findOutbox(
            String status,
            String transactionGlobalId,
            String topic,
            int limit) {
        if (!available()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT outbox_id, message_id, topic, message_key, transaction_global_id, segment_id,
                       producer_module, consumer_module, idempotency_key, outbox_status, worker_id,
                       broker_name, partition_key, failure_message, occurred_at, claimed_at, published_at,
                       created_at, updated_at
                FROM cpf_broker_outbox
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "outbox_status", status);
        appendEquals(sql, args, "transaction_global_id", transactionGlobalId);
        appendEquals(sql, args, "topic", topic);
        sql.append(" ORDER BY outbox_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    @Override
    public List<Map<String, Object>> findInbox(String status, String key, int limit) {
        if (!available()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT inbox_id, message_id, idempotency_key, inbox_status, result_detail,
                       received_at, consumed_at, created_at, updated_at
                FROM cpf_broker_inbox
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "inbox_status", status);
        appendLike(sql, args, "idempotency_key", key);
        sql.append(" ORDER BY inbox_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    @Override
    public List<Map<String, Object>> findDlq(
            String status,
            String transactionGlobalId,
            String topic,
            int limit) {
        if (!available()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT dlq_id, message_id, topic, transaction_global_id, segment_id, failure_reason,
                       replay_status, replay_count, replay_requested_at, replay_completed_at,
                       created_at, updated_at
                FROM cpf_broker_dlq
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "replay_status", status);
        appendEquals(sql, args, "transaction_global_id", transactionGlobalId);
        appendEquals(sql, args, "topic", topic);
        sql.append(" ORDER BY dlq_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    @Override
    public List<Map<String, Object>> findFileTransfers(
            String status,
            String transactionGlobalId,
            String endpointCode,
            int limit) {
        if (!available()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT history_id, transfer_id, transaction_global_id, segment_id, endpoint_code,
                       transfer_operation, local_path, remote_path, checksum, file_size, duplicate_key,
                       transfer_status, result_detail, completed_at, created_at, updated_at
                FROM cpf_file_transfer_history
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "transfer_status", status);
        appendEquals(sql, args, "transaction_global_id", transactionGlobalId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        sql.append(" ORDER BY history_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    @Override
    public List<Map<String, Object>> findUnknownResults(
            String type,
            String status,
            String transactionGlobalId,
            int limit) {
        if (!available()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT unknown_seq, unknown_id, unknown_type, unknown_status, transaction_global_id,
                       segment_id, external_key, failure_code, failure_message, next_action,
                       detected_at, resolved_at, resolved_by, audit_reason, created_at, updated_at
                FROM cpf_unknown_result
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "unknown_type", type);
        appendEquals(sql, args, "unknown_status", status);
        appendEquals(sql, args, "transaction_global_id", transactionGlobalId);
        sql.append(" ORDER BY unknown_seq DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public ChangeResult requestDlqReplay(String messageId, String operatorId, String reason) {
        Map<String, Object> before = findOne("cpf_broker_dlq", "message_id", messageId);
        int updated = jdbc().update("""
                UPDATE cpf_broker_dlq
                SET replay_status = 'REQUESTED',
                    replay_requested_at = CURRENT_TIMESTAMP(3),
                    replay_count = replay_count + 1,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ?
                  AND replay_status IN ('WAITING', 'FAILED')
                """, required(operatorId, "requestUser"), required(messageId, "messageId"));
        if (updated != 1) {
            throw new CpfValidationException("DLQ가 없거나 현재 상태에서는 재처리를 요청할 수 없습니다.");
        }
        jdbc().update("DELETE FROM cpf_broker_inbox WHERE message_id = ?", messageId);
        int requeued = jdbc().update("""
                UPDATE cpf_broker_outbox
                SET outbox_status = 'PENDING',
                    worker_id = NULL,
                    attempt_count = 0,
                    next_attempt_at = NULL,
                    lease_until = NULL,
                    claimed_at = NULL,
                    published_at = NULL,
                    failure_message = NULL,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ?
                """, operatorId, messageId);
        if (requeued != 1) {
            throw new CpfValidationException("DLQ 원본 outbox가 없어 실제 재처리를 시작할 수 없습니다.");
        }
        return new ChangeResult(before, findOne("cpf_broker_dlq", "message_id", messageId), required(reason, "reason"));
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public ChangeResult resolveUnknown(
            String unknownId,
            String targetStatus,
            String operatorId,
            String reason) {
        String normalizedStatus = required(targetStatus, "targetStatus").toUpperCase();
        if (!RESOLUTION_STATUSES.contains(normalizedStatus)) {
            throw new CpfValidationException("허용되지 않은 결과 미확정 상태입니다.");
        }
        Map<String, Object> before = findOne("cpf_unknown_result", "unknown_id", unknownId);
        boolean resolved = Set.of("CONFIRMED_SUCCESS", "CONFIRMED_FAILURE", "RESOLVED")
                .contains(normalizedStatus);
        int updated = jdbc().update("""
                UPDATE cpf_unknown_result
                SET unknown_status = ?,
                    resolved_at = CASE WHEN ? = 'Y' THEN CURRENT_TIMESTAMP(3) ELSE NULL END,
                    resolved_by = CASE WHEN ? = 'Y' THEN ? ELSE NULL END,
                    audit_reason = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE unknown_id = ?
                  AND unknown_status NOT IN ('CONFIRMED_SUCCESS', 'CONFIRMED_FAILURE', 'RESOLVED')
                """,
                normalizedStatus,
                resolved ? "Y" : "N",
                resolved ? "Y" : "N",
                required(operatorId, "requestUser"),
                required(reason, "reason"),
                operatorId,
                required(unknownId, "unknownId"));
        if (updated != 1) {
            throw new CpfValidationException("결과 미확정 건이 없거나 이미 최종 처리됐습니다.");
        }
        return new ChangeResult(before, findOne("cpf_unknown_result", "unknown_id", unknownId), reason.trim());
    }

    private Map<String, Object> findOne(String table, String keyColumn, String key) {
        List<Map<String, Object>> rows = jdbc().queryForList(
                "SELECT * FROM " + table + " WHERE " + keyColumn + " = ? LIMIT 1",
                required(key, keyColumn));
        if (rows.isEmpty()) {
            throw new CpfValidationException("운영 대상을 찾을 수 없습니다.");
        }
        return rows.getFirst();
    }

    private boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null;
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("CPF 신뢰성 운영 기능에는 cpfJdbcTemplate이 필요합니다.");
        }
        return jdbcTemplate;
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            args.add('%' + value.trim() + '%');
        }
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 500));
    }

    private String required(String value, String field) {
        if (!hasText(value)) {
            throw new CpfValidationException(field + "는 필수입니다.");
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
