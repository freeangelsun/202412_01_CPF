package com.cpf.backoffice.online.auth.repository;

import com.cpf.backoffice.online.base.BackofficeBaseRepository;

import com.cpf.data.persistence.api.CpfRepository;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** 최초 특권 계정 Bootstrap용 1회 승인 Token 원장입니다. */
@CpfRepository
public final class BackofficeBootstrapApprovalRepository extends BackofficeBaseRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcProvider;
    private final CpfVendorSqlCatalog sql;

    public BackofficeBootstrapApprovalRepository(
            @Qualifier("MBW_JDBC_TEMPLATE") ObjectProvider<NamedParameterJdbcTemplate> jdbcProvider,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbcProvider = jdbcProvider;
        this.sql = sqlCatalogProvider.forModule("backoffice");
    }

    public boolean claim(
            String tokenHash,
            String environmentFingerprint,
            String operationId,
            String claimOwnerId,
            Instant now,
            Instant leaseUntil) {
        int updated = jdbc().update(sql.required("auth-bootstrap-approval-claim"), new MapSqlParameterSource()
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
        int updated = jdbc().update(
                sql.required("auth-bootstrap-approval-reconcile-complete"),
                new MapSqlParameterSource()
                        .addValue("completedAt", Timestamp.from(now))
                        .addValue("adminUserId", adminUserId)
                        .addValue("operationId", operationId)
                        .addValue("tokenHash", tokenHash));
        if (updated != 1) throw new IllegalStateException("MBW_BOOTSTRAP_RECONCILE_COMPLETE_FAILED");
    }

    public void cleanup(String tokenHash, String cleanupStatus, String failureCode, Instant now) {
        int updated = jdbc().update(sql.required("auth-bootstrap-approval-cleanup"), new MapSqlParameterSource()
                .addValue("cleanupStatus", cleanupStatus)
                .addValue("failureCode", sanitize(failureCode))
                .addValue("updatedAt", Timestamp.from(now))
                .addValue("tokenHash", tokenHash));
        if (updated != 1) throw new IllegalStateException("MBW_BOOTSTRAP_CLEANUP_UPDATE_FAILED");
    }

    public Optional<ApprovalState> find(String tokenHash) {
        return findBySql(sql.required("auth-bootstrap-approval-find-by-token-hash"), tokenHash);
    }

    public Optional<ApprovalState> findByOperationId(String operationId) {
        return findBySql(sql.required("auth-bootstrap-approval-find-by-operation-id"), operationId);
    }

    private Optional<ApprovalState> findBySql(String querySql, String value) {
        return jdbc().queryForList(
                querySql,
                new MapSqlParameterSource("value", value)).stream().findFirst().map(this::state);
    }

    private void updateTerminal(
            String tokenHash, String operationId, String claimOwnerId,
            String status, Long adminUserId, String failureCode, Instant now) {
        int updated = jdbc().update(sql.required("auth-bootstrap-approval-update-terminal"), new MapSqlParameterSource()
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
                string(row, "TOKEN_HASH", "token_hash", "tokenHash"),
                string(row, "ENV_FINGERPRINT", "env_fingerprint", "environmentFingerprint"),
                string(row, "STATUS", "status"),
                nullable(row, "OPERATION_ID", "operation_id", "operationId"),
                instant(value(row, "EXPIRES_AT", "expires_at", "expiresAt")),
                instant(value(row, "CLAIMED_AT", "claimed_at", "claimedAt")),
                nullable(row, "CLAIM_OWNER_ID", "claim_owner_id", "claimOwnerId"),
                instant(value(row, "CLAIM_EXPIRES_AT", "claim_expires_at", "claimExpiresAt")),
                instant(value(row, "COMPLETED_AT", "completed_at", "completedAt")),
                number(row, "ADMIN_USER_ID", "admin_user_id", "adminUserId"),
                nullable(row, "FAILURE_CODE", "failure_code", "failureCode"),
                nullable(row, "CLEANUP_STATUS", "cleanup_status", "cleanupStatus"),
                nullable(row, "CLEANUP_FAILURE_CODE", "cleanup_failure_code", "cleanupFailureCode"),
                instant(value(row, "CLEANUP_UPDATED_AT", "cleanup_updated_at", "cleanupUpdatedAt")));
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbc = jdbcProvider.getIfAvailable();
        if (jdbc == null) throw new IllegalStateException("MBW_BOOTSTRAP_DATASOURCE_REQUIRED");
        return jdbc;
    }

    private static Object value(Map<String, Object> row, String... names) {
        for (String name : names) if (row.containsKey(name)) return row.get(name);
        return null;
    }

    private static String string(Map<String, Object> row, String... names) {
        Object value = value(row, names);
        return value == null ? "" : String.valueOf(value);
    }

    private static String nullable(Map<String, Object> row, String... names) {
        Object value = value(row, names);
        return value == null ? null : String.valueOf(value);
    }

    private static Long number(Map<String, Object> row, String... names) {
        Object value = value(row, names);
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
