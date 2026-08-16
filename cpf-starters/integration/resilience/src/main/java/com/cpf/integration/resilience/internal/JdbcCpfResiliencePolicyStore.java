package com.cpf.integration.resilience.internal;

import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.security.api.CpfSensitiveData;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyStore;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** JDBC policy store with active/pending uniqueness, bounded inputs and atomic two-person approval. */
public final class JdbcCpfResiliencePolicyStore implements CpfResiliencePolicyStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    public JdbcCpfResiliencePolicyStore(JdbcTemplate jdbc, TransactionTemplate tx) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.tx = Objects.requireNonNull(tx, "tx");
    }

    @Override
    public Optional<CpfResiliencePolicy> findActive(String operationId) {
        String operation = identifier(operationId, "operationId", 256);
        var rows = jdbc.query(
                "select operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms," +
                        "circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent," +
                        "rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag " +
                        "from cpf_resilience_policy where operation_id=? and policy_status='ACTIVE'",
                this::map,
                operation);
        return rows.stream().findFirst();
    }

    @Override
    public List<CpfResiliencePolicy> search(String filter, int offset, int limit) {
        if (offset < 0 || limit < 1 || limit > 500) {
            throw new IllegalArgumentException("invalid paging");
        }
        int end = Math.addExact(offset, limit);
        String normalizedFilter = filter == null || filter.isBlank()
                ? ""
                : identifier(filter, "filter", 256);
        String value = "%" + normalizedFilter + "%";
        String sql = "select operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms," +
                "circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent," +
                "rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag " +
                "from (select operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms," +
                "circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent," +
                "rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag," +
                "row_number() over(order by operation_id) cpf_rn from cpf_resilience_policy " +
                "where policy_status='ACTIVE' and operation_id like ?) cpf_page " +
                "where cpf_rn>? and cpf_rn<=? order by cpf_rn";
        return jdbc.query(sql, this::map, value, offset, end);
    }

    @Override
    public String request(CpfResiliencePolicy policy, String requesterId, String reason) {
        CpfResiliencePolicy requested = Objects.requireNonNull(policy, "policy");
        String requester = actor(requesterId, "requesterId");
        String sanitizedReason = CpfSensitiveData.sanitizeAuditReason(reason);
        String id = UUID.randomUUID().toString();
        try {
            jdbc.update(
                    "insert into cpf_resilience_policy_request(" +
                            "request_id,operation_id,requested_revision,policy_payload,requester_id," +
                            "request_reason,request_status,active_operation_key) " +
                            "values(?,?,?,?,?,?,'PENDING',?)",
                    id,
                    requested.operationId(),
                    requested.revision(),
                    encode(requested),
                    requester,
                    sanitizedReason,
                    requested.operationId());
        } catch (DuplicateKeyException conflict) {
            throw new IllegalStateException(
                    "pending policy request already exists: " + requested.operationId(), conflict);
        }
        return id;
    }

    @Override
    public CpfResiliencePolicy approve(String requestId, String approverId, String reason) {
        String id = identifier(requestId, "requestId", 128);
        String approver = actor(approverId, "approverId");
        String sanitizedReason = CpfSensitiveData.sanitizeAuditReason(reason);
        CpfResiliencePolicy approved = tx.execute(status -> {
            var rows = jdbc.query(
                    "select operation_id,policy_payload,requester_id " +
                            "from cpf_resilience_policy_request " +
                            "where request_id=? and request_status='PENDING' for update",
                    (rs, rowNumber) -> new RequestRow(
                            rs.getString(1), rs.getString(2), rs.getString(3)),
                    id);
            if (rows.size() != 1) throw new IllegalArgumentException("pending request not found");
            RequestRow row = rows.getFirst();
            if (row.requesterId().equals(approver)) {
                throw new IllegalArgumentException("self approval is forbidden");
            }
            long revision = Optional.ofNullable(jdbc.queryForObject(
                    "select max(revision) from cpf_resilience_policy where operation_id=?",
                    Long.class,
                    row.operationId())).orElse(0L) + 1L;
            CpfResiliencePolicy policy = decode(row.payload(), revision);
            jdbc.update(
                    "update cpf_resilience_policy set policy_status='SUPERSEDED'," +
                            "active_operation_key=null,updated_by=?,updated_at=CURRENT_TIMESTAMP " +
                            "where operation_id=? and policy_status='ACTIVE'",
                    approver,
                    row.operationId());
            insertActive(policy, approver);
            int updated = jdbc.update(
                    "update cpf_resilience_policy_request set request_status='APPROVED'," +
                            "approver_id=?,approval_reason=?,active_operation_key=null " +
                            "where request_id=? and request_status='PENDING'",
                    approver,
                    sanitizedReason,
                    id);
            if (updated != 1) throw new IllegalStateException("approval conflict");
            return policy;
        });
        return Objects.requireNonNull(approved, "approval transaction returned null");
    }

    @Override
    public void reject(String requestId, String approverId, String reason) {
        String id = identifier(requestId, "requestId", 128);
        String approver = actor(approverId, "approverId");
        String sanitizedReason = CpfSensitiveData.sanitizeAuditReason(reason);
        int updated = jdbc.update(
                "update cpf_resilience_policy_request set request_status='REJECTED'," +
                        "approver_id=?,approval_reason=?,active_operation_key=null " +
                        "where request_id=? and request_status='PENDING' and requester_id<>?",
                approver,
                sanitizedReason,
                id,
                approver);
        if (updated != 1) {
            throw new IllegalArgumentException("request missing, conflicted or self-approved");
        }
    }

    private void insertActive(CpfResiliencePolicy policy, String operator) {
        jdbc.update(
                "insert into cpf_resilience_policy(" +
                        "policy_id,operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms," +
                        "circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent," +
                        "rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag," +
                        "policy_status,active_operation_key,updated_by,updated_at) " +
                        "values(?,?,?,?,?,?,?,?,?,?,?,?,?,'ACTIVE',?,?,CURRENT_TIMESTAMP)",
                UUID.randomUUID().toString(),
                policy.operationId(),
                policy.revision(),
                policy.timeoutBudget().toMillis(),
                policy.maxAttempts(),
                policy.retryBackoff().toMillis(),
                policy.circuitFailureThreshold(),
                policy.circuitOpenDuration().toMillis(),
                policy.bulkheadMaxConcurrent(),
                policy.rateLimitPermits(),
                policy.rateLimitWindow().toMillis(),
                policy.idempotent() ? "Y" : "N",
                policy.unknownResultReconcileEnabled() ? "Y" : "N",
                policy.operationId(),
                operator);
    }

    private CpfResiliencePolicy map(ResultSet result, int rowNumber) throws SQLException {
        return new CpfResiliencePolicy(
                result.getString(1),
                result.getLong(2),
                Duration.ofMillis(result.getLong(3)),
                result.getInt(4),
                Duration.ofMillis(result.getLong(5)),
                result.getInt(6),
                Duration.ofMillis(result.getLong(7)),
                result.getInt(8),
                result.getInt(9),
                Duration.ofMillis(result.getLong(10)),
                "Y".equals(result.getString(11)),
                "Y".equals(result.getString(12)));
    }

    private static String encode(CpfResiliencePolicy policy) {
        return String.join("|",
                policy.operationId(),
                Long.toString(policy.timeoutBudget().toMillis()),
                Integer.toString(policy.maxAttempts()),
                Long.toString(policy.retryBackoff().toMillis()),
                Integer.toString(policy.circuitFailureThreshold()),
                Long.toString(policy.circuitOpenDuration().toMillis()),
                Integer.toString(policy.bulkheadMaxConcurrent()),
                Integer.toString(policy.rateLimitPermits()),
                Long.toString(policy.rateLimitWindow().toMillis()),
                policy.idempotent() ? "Y" : "N",
                policy.unknownResultReconcileEnabled() ? "Y" : "N");
    }

    private static CpfResiliencePolicy decode(String value, long revision) {
        String[] parts = value.split("\\|", -1);
        if (parts.length != 11) throw new IllegalStateException("stored policy payload is invalid");
        return new CpfResiliencePolicy(
                parts[0],
                revision,
                Duration.ofMillis(Long.parseLong(parts[1])),
                Integer.parseInt(parts[2]),
                Duration.ofMillis(Long.parseLong(parts[3])),
                Integer.parseInt(parts[4]),
                Duration.ofMillis(Long.parseLong(parts[5])),
                Integer.parseInt(parts[6]),
                Integer.parseInt(parts[7]),
                Duration.ofMillis(Long.parseLong(parts[8])),
                "Y".equals(parts[9]),
                "Y".equals(parts[10]));
    }

    private static String actor(String value, String name) {
        String normalized = identifier(value, name, 128);
        if (!normalized.matches("[A-Za-z0-9][A-Za-z0-9._:@-]*")) {
            throw new IllegalArgumentException(name + " contains unsupported characters");
        }
        return normalized;
    }

    private static String identifier(String value, String name, int maximum) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maximum) {
            throw new IllegalArgumentException(name + " exceeds " + maximum + " characters");
        }
        if (normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(name + " contains control characters");
        }
        return normalized;
    }

    private record RequestRow(String operationId, String payload, String requesterId) { }
}
