package com.cpf.core.common.idempotency;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CPF 공통 idempotency 테이블을 사용하는 JDBC reference adapter입니다.
 *
 * <p>운영 DB에서는 {@code cpf_idempotency_record}의 unique key가 동시 요청 race를 최종 차단합니다.
 * 애플리케이션은 먼저 reserve를 호출하고, 처리 결과가 확정되면 complete로 저장 응답과 상태를 남깁니다.</p>
 */
public class JdbcCpfIdempotencyRepository implements CpfIdempotencyPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCpfIdempotencyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean reserve(CpfIdempotencyRecord record) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO cpf_idempotency_record (
                        scope, idempotency_key, request_hash, payload_hash, record_status,
                        retry_allowed_yn, expires_at, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, 'CPF_IDEMPOTENCY', 'CPF_IDEMPOTENCY')
                    """,
                    record.scope(),
                    record.idempotencyKey(),
                    record.requestHash(),
                    record.payloadHash(),
                    record.status(),
                    record.retryAllowed() ? "Y" : "N",
                    timestamp(record.expiresAt()));
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    @Override
    public Optional<CpfIdempotencyRecord> find(String scope, String idempotencyKey) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT scope,
                       idempotency_key AS idempotencyKey,
                       request_hash AS requestHash,
                       payload_hash AS payloadHash,
                       record_status AS recordStatus,
                       stored_response AS storedResponse,
                       retry_allowed_yn AS retryAllowedYn,
                       created_at AS createdAt,
                       completed_at AS completedAt,
                       expires_at AS expiresAt
                FROM cpf_idempotency_record
                WHERE scope = ?
                  AND idempotency_key = ?
                LIMIT 1
                """, scope, idempotencyKey);
        return rows.stream().findFirst().map(this::mapRecord);
    }

    @Override
    public void complete(String scope, String idempotencyKey, String status, String storedResponse, boolean retryAllowed) {
        jdbcTemplate.update("""
                UPDATE cpf_idempotency_record
                SET record_status = ?,
                    stored_response = ?,
                    retry_allowed_yn = ?,
                    completed_at = CURRENT_TIMESTAMP(3),
                    updated_by = 'CPF_IDEMPOTENCY',
                    updated_at = CURRENT_TIMESTAMP
                WHERE scope = ?
                  AND idempotency_key = ?
                """,
                normalizeStatus(status),
                storedResponse,
                retryAllowed ? "Y" : "N",
                scope,
                idempotencyKey);
    }

    @Override
    public boolean restart(
            String scope,
            String idempotencyKey,
            String requestHash,
            String payloadHash,
            Instant now,
            Instant expiresAt) {
        int updated = jdbcTemplate.update("""
                UPDATE cpf_idempotency_record
                SET record_status = 'PROCESSING',
                    stored_response = NULL,
                    retry_allowed_yn = 'N',
                    completed_at = NULL,
                    expires_at = ?,
                    updated_by = 'CPF_IDEMPOTENCY',
                    updated_at = CURRENT_TIMESTAMP
                WHERE scope = ?
                  AND idempotency_key = ?
                  AND request_hash = ?
                  AND payload_hash = ?
                  AND (
                      (record_status IN ('FAILED', 'UNKNOWN') AND retry_allowed_yn = 'Y')
                      OR record_status = 'EXPIRED'
                      OR (record_status = 'PROCESSING' AND expires_at <= ?)
                  )
                """,
                timestamp(expiresAt),
                scope,
                idempotencyKey,
                requestHash,
                payloadHash,
                timestamp(now));
        return updated == 1;
    }

    @Override
    public void expire(String scope, String idempotencyKey) {
        complete(scope, idempotencyKey, "EXPIRED", null, false);
    }

    @Override
    public int expireBefore(Instant now, int limit) {
        return jdbcTemplate.update("""
                UPDATE cpf_idempotency_record
                SET record_status = 'EXPIRED',
                    retry_allowed_yn = 'Y',
                    updated_by = 'CPF_IDEMPOTENCY_CLEANUP',
                    updated_at = CURRENT_TIMESTAMP
                WHERE record_status = 'PROCESSING'
                  AND expires_at IS NOT NULL
                  AND expires_at <= ?
                ORDER BY idempotency_id
                LIMIT ?
                """, timestamp(now == null ? Instant.now() : now), safeLimit(limit));
    }

    private CpfIdempotencyRecord mapRecord(Map<String, Object> row) {
        return new CpfIdempotencyRecord(
                string(row, "scope"),
                string(row, "idempotencyKey"),
                string(row, "requestHash"),
                string(row, "payloadHash"),
                string(row, "recordStatus"),
                string(row, "storedResponse"),
                "Y".equalsIgnoreCase(string(row, "retryAllowedYn")),
                instant(row, "createdAt"),
                instant(row, "completedAt"),
                instant(row, "expiresAt"));
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "UNKNOWN" : status.trim().toUpperCase();
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 1000));
    }

    private Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private Instant instant(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        return null;
    }

    private String string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
