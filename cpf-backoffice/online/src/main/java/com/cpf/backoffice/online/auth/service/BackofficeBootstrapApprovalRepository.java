package com.cpf.backoffice.online.auth.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.cpf.data.persistence.api.CpfRepository;

/** 최초 특권 계정 Bootstrap용 1회 승인 Token 원장입니다. */
@CpfRepository
public final class BackofficeBootstrapApprovalRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcProvider;

    public BackofficeBootstrapApprovalRepository(
            @Qualifier("MBW_JDBC_TEMPLATE") ObjectProvider<NamedParameterJdbcTemplate> jdbcProvider) {
        this.jdbcProvider = jdbcProvider;
    }

    public boolean claim(
            String tokenHash,
            String environmentFingerprint,
            String operationId,
            String claimOwnerId,
            Instant now,
            Instant leaseUntil) {
        int updated = jdbc().update("""
                UPDATE MBW_BOOTSTRAP_APPROVAL
                   SET STATUS = 'CLAIMED', OPERATION_ID = :operationId, CLAIM_OWNER_ID = :claimOwnerId,
                       CLAIMED_AT = :claimedAt, CLAIM_EXPIRES_AT = :claimExpiresAt,
                       UPDATED_AT = :claimedAt
                 WHERE TOKEN_HASH = :tokenHash
                   AND ENV_FINGERPRINT = :environmentFingerprint
                   AND EXPIRES_AT > :claimedAt
                   AND (
                        STATUS = 'APPROVED'
                        OR (STATUS = 'CLAIMED' AND OPERATION_ID = :operationId AND CLAIM_EXPIRES_AT <= :claimedAt)
                   )
                """, new MapSqlParameterSource()
                .addValue("tokenHash", tokenHash)
                .addValue("environmentFingerprint", environmentFingerprint)
                .addValue("operationId", operationId)
                .addValue("claimOwnerId", claimOwnerId)
                .addValue("claimedAt", Timestamp.from(now))
                .addValue("claimExpiresAt", Timestamp.from(leaseUntil)));
        return updated == 1;
    }

    public void complete(String tokenHash, String operationId, String claimOwnerId, long adminUserId, Instant now) {
        updateTerminal(tokenHash, operationId, claimOwnerId, "COMPLETED", adminUserId, null, now);
    }

    public void fail(String tokenHash, String operationId, String claimOwnerId, String failureCode, Instant now) {
        updateTerminal(tokenHash, operationId, claimOwnerId, "FAILED", null, sanitize(failureCode), now);
    }

    public void reconcileComplete(String tokenHash, String operationId, long adminUserId, Instant now) {
        int updated = jdbc().update("""
                UPDATE MBW_BOOTSTRAP_APPROVAL
                   SET STATUS = 'COMPLETED', COMPLETED_AT = :completedAt, ADMIN_USER_ID = :adminUserId,
                       FAILURE_CODE = NULL, CLAIM_EXPIRES_AT = NULL, UPDATED_AT = :completedAt
                 WHERE TOKEN_HASH = :tokenHash AND OPERATION_ID = :operationId
                   AND STATUS IN ('CLAIMED', 'FAILED', 'COMPLETED')
                   AND (ADMIN_USER_ID IS NULL OR ADMIN_USER_ID = :adminUserId)
                """, new MapSqlParameterSource()
                .addValue("completedAt", Timestamp.from(now))
                .addValue("adminUserId", adminUserId)
                .addValue("operationId", operationId)
                .addValue("tokenHash", tokenHash));
        if (updated != 1) throw new IllegalStateException("MBW_BOOTSTRAP_RECONCILE_COMPLETE_FAILED");
    }

    public void cleanup(String tokenHash, String cleanupStatus, String failureCode, Instant now) {
        int updated = jdbc().update("""
                UPDATE MBW_BOOTSTRAP_APPROVAL
                   SET CLEANUP_STATUS = :cleanupStatus,
                       CLEANUP_FAILURE_CODE = :failureCode,
                       CLEANUP_UPDATED_AT = :updatedAt,
                       UPDATED_AT = :updatedAt
                 WHERE TOKEN_HASH = :tokenHash
                """, new MapSqlParameterSource()
                .addValue("cleanupStatus", cleanupStatus)
                .addValue("failureCode", sanitize(failureCode))
                .addValue("updatedAt", Timestamp.from(now))
                .addValue("tokenHash", tokenHash));
        if (updated != 1) throw new IllegalStateException("MBW_BOOTSTRAP_CLEANUP_UPDATE_FAILED");
    }

    public Optional<ApprovalState> find(String tokenHash) {
        return findBy("TOKEN_HASH = :value", tokenHash);
    }

    public Optional<ApprovalState> findByOperationId(String operationId) {
        return findBy("OPERATION_ID = :value", operationId);
    }

    private Optional<ApprovalState> findBy(String predicate, String value) {
        return jdbc().queryForList("""
                SELECT TOKEN_HASH, ENV_FINGERPRINT, STATUS, OPERATION_ID, EXPIRES_AT,
                       CLAIMED_AT, CLAIM_OWNER_ID, CLAIM_EXPIRES_AT, COMPLETED_AT,
                       ADMIN_USER_ID, FAILURE_CODE, CLEANUP_STATUS, CLEANUP_FAILURE_CODE, CLEANUP_UPDATED_AT
                  FROM MBW_BOOTSTRAP_APPROVAL
                 WHERE """ + predicate,
                new MapSqlParameterSource("value", value)).stream().findFirst().map(this::state);
    }

    private void updateTerminal(
            String tokenHash, String operationId, String claimOwnerId,
            String status, Long adminUserId, String failureCode, Instant now) {
        int updated = jdbc().update("""
                UPDATE MBW_BOOTSTRAP_APPROVAL
                   SET STATUS = :status, COMPLETED_AT = :completedAt, ADMIN_USER_ID = :adminUserId,
                       FAILURE_CODE = :failureCode, CLAIM_EXPIRES_AT = NULL, UPDATED_AT = :completedAt
                 WHERE TOKEN_HASH = :tokenHash AND STATUS = 'CLAIMED'
                   AND OPERATION_ID = :operationId AND CLAIM_OWNER_ID = :claimOwnerId
                """, new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("completedAt", Timestamp.from(now))
                .addValue("adminUserId", adminUserId)
                .addValue("failureCode", failureCode)
                .addValue("operationId", operationId)
                .addValue("claimOwnerId", claimOwnerId)
                .addValue("tokenHash", tokenHash));
        if (updated != 1) throw new IllegalStateException("MBW_BOOTSTRAP_TERMINAL_UPDATE_FAILED");
    }

    private ApprovalState state(Map<String, Object> row) {
        return new ApprovalState(
                string(row, "TOKEN_HASH", "token_hash"),
                string(row, "ENV_FINGERPRINT", "env_fingerprint"),
                string(row, "STATUS", "status"),
                nullable(row, "OPERATION_ID", "operation_id"),
                instant(value(row, "EXPIRES_AT", "expires_at")),
                instant(value(row, "CLAIMED_AT", "claimed_at")),
                nullable(row, "CLAIM_OWNER_ID", "claim_owner_id"),
                instant(value(row, "CLAIM_EXPIRES_AT", "claim_expires_at")),
                instant(value(row, "COMPLETED_AT", "completed_at")),
                number(row, "ADMIN_USER_ID", "admin_user_id"),
                nullable(row, "FAILURE_CODE", "failure_code"),
                nullable(row, "CLEANUP_STATUS", "cleanup_status"),
                nullable(row, "CLEANUP_FAILURE_CODE", "cleanup_failure_code"),
                instant(value(row, "CLEANUP_UPDATED_AT", "cleanup_updated_at")));
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbc = jdbcProvider.getIfAvailable();
        if (jdbc == null) throw new IllegalStateException("MBW_BOOTSTRAP_DATASOURCE_REQUIRED");
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
            Instant expiresAt, Instant claimedAt, String claimOwnerId, Instant claimExpiresAt,
            Instant completedAt, Long adminUserId, String failureCode,
            String cleanupStatus, String cleanupFailureCode, Instant cleanupUpdatedAt) {}
}
