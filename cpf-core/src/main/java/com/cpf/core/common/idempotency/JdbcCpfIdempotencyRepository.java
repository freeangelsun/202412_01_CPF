package com.cpf.core.common.idempotency;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
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
    private final CpfVendorSqlCatalog sql;

    public JdbcCpfIdempotencyRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new StandardEnvironment());
    }

    public JdbcCpfIdempotencyRepository(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.sql = CpfVendorSqlCatalog.create(environment, "cpf");
    }

    @Override
    public boolean reserve(CpfIdempotencyRecord record) {
        try {
            jdbcTemplate.update(sql.required("idempotency-reserve"),
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
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql.required("idempotency-find"), scope, idempotencyKey);
        return rows.stream().findFirst().map(this::mapRecord);
    }

    @Override
    public void complete(String scope, String idempotencyKey, String status, String storedResponse, boolean retryAllowed) {
        jdbcTemplate.update(sql.required("idempotency-complete"),
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
        int updated = jdbcTemplate.update(sql.required("idempotency-restart"),
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
        return jdbcTemplate.update(
                sql.required("idempotency-expire-before"),
                timestamp(now == null ? Instant.now() : now),
                safeLimit(limit));
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
