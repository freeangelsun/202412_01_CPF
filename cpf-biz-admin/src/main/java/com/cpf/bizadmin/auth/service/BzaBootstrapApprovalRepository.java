package com.cpf.bizadmin.auth.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** 최초 특권 계정 Bootstrap용 1회 승인 Token 원장입니다. */
@Repository
public final class BzaBootstrapApprovalRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcProvider;

    public BzaBootstrapApprovalRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcProvider) {
        this.jdbcProvider = jdbcProvider;
    }

    public boolean claim(String tokenHash, String environmentFingerprint, String operationId, Instant now) {
        int updated = jdbc().update("""
                UPDATE BZA_BOOTSTRAP_APPROVAL
                   SET STATUS = 'CLAIMED', OPERATION_ID = :operationId, CLAIMED_AT = :claimedAt,
                       UPDATED_AT = :claimedAt
                 WHERE TOKEN_HASH = :tokenHash
                   AND ENV_FINGERPRINT = :environmentFingerprint
                   AND STATUS = 'APPROVED'
                   AND EXPIRES_AT > :claimedAt
                """, new MapSqlParameterSource()
                .addValue("tokenHash", tokenHash)
                .addValue("environmentFingerprint", environmentFingerprint)
                .addValue("operationId", operationId)
                .addValue("claimedAt", Timestamp.from(now)));
        return updated == 1;
    }

    public void complete(String tokenHash, long adminUserId, Instant now) {
        updateTerminal(tokenHash, "COMPLETED", adminUserId, null, now);
    }

    public void fail(String tokenHash, String failureCode, Instant now) {
        updateTerminal(tokenHash, "FAILED", null, sanitize(failureCode), now);
    }

    public Optional<ApprovalState> find(String tokenHash) {
        return jdbc().queryForList("""
                SELECT TOKEN_HASH, ENV_FINGERPRINT, STATUS, OPERATION_ID, EXPIRES_AT,
                       CLAIMED_AT, COMPLETED_AT, ADMIN_USER_ID, FAILURE_CODE
                  FROM BZA_BOOTSTRAP_APPROVAL
                 WHERE TOKEN_HASH = :tokenHash
                """, new MapSqlParameterSource("tokenHash", tokenHash)).stream().findFirst().map(this::state);
    }

    private void updateTerminal(
            String tokenHash, String status, Long adminUserId, String failureCode, Instant now) {
        int updated = jdbc().update("""
                UPDATE BZA_BOOTSTRAP_APPROVAL
                   SET STATUS = :status, COMPLETED_AT = :completedAt, ADMIN_USER_ID = :adminUserId,
                       FAILURE_CODE = :failureCode, UPDATED_AT = :completedAt
                 WHERE TOKEN_HASH = :tokenHash AND STATUS = 'CLAIMED'
                """, new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("completedAt", Timestamp.from(now))
                .addValue("adminUserId", adminUserId)
                .addValue("failureCode", failureCode)
                .addValue("tokenHash", tokenHash));
        if (updated != 1) throw new IllegalStateException("BZA_BOOTSTRAP_TERMINAL_UPDATE_FAILED");
    }

    private ApprovalState state(Map<String, Object> row) {
        return new ApprovalState(
                string(row, "TOKEN_HASH", "token_hash"),
                string(row, "ENV_FINGERPRINT", "env_fingerprint"),
                string(row, "STATUS", "status"),
                nullable(row, "OPERATION_ID", "operation_id"),
                instant(value(row, "EXPIRES_AT", "expires_at")),
                instant(value(row, "CLAIMED_AT", "claimed_at")),
                instant(value(row, "COMPLETED_AT", "completed_at")),
                number(row, "ADMIN_USER_ID", "admin_user_id"),
                nullable(row, "FAILURE_CODE", "failure_code"));
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbc = jdbcProvider.getIfAvailable();
        if (jdbc == null) throw new IllegalStateException("BZA_BOOTSTRAP_DATASOURCE_REQUIRED");
        return jdbc;
    }

    private static Object value(Map<String, Object> row, String upper, String lower) {
        return row.containsKey(upper) ? row.get(upper) : row.get(lower);
    }
    private static String string(Map<String, Object> row, String upper, String lower) {
        Object value = value(row, upper, lower);
        return value == null ? "" : String.valueOf(value);
    }
    private static String nullable(Map<String, Object> row, String upper, String lower) {
        Object value = value(row, upper, lower);
        return value == null ? null : String.valueOf(value);
    }
    private static Long number(Map<String, Object> row, String upper, String lower) {
        Object value = value(row, upper, lower);
        return value instanceof Number number ? number.longValue() : null;
    }
    private static Instant instant(Object value) {
        return value instanceof Timestamp timestamp ? timestamp.toInstant() : null;
    }
    private static String sanitize(String value) {
        String sanitized = value == null ? "UNKNOWN" : value.replaceAll("[^A-Za-z0-9_.:-]", "_");
        return sanitized.substring(0, Math.min(100, sanitized.length()));
    }

    public record ApprovalState(
            String tokenHash, String environmentFingerprint, String status, String operationId,
            Instant expiresAt, Instant claimedAt, Instant completedAt, Long adminUserId, String failureCode) {}
}
