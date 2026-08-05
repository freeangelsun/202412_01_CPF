package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.locking.CpfLockManager;
import com.cpf.core.spi.locking.CpfLockStore;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * JDBC lock store using a pre-seeded shard row as the cross-instance serialization point.
 *
 * <p>The companion vendor migrations must create {@code cpf_lock_shard} rows 0..255 and retain
 * {@code cpf_distributed_lock} rows after release so fencing tokens remain monotonic. The shard
 * lock removes the absent-row insert race without vendor-specific UPSERT syntax.</p>
 */
final class JdbcCpfLockStore implements CpfLockStore {
    static final int SHARD_COUNT = 256;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    JdbcCpfLockStore(JdbcTemplate jdbc, TransactionTemplate transactions) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    @Override
    public UpdateResult update(String key, UnaryOperator<StoredLock> transition) {
        String normalized = requireKey(key);
        Objects.requireNonNull(transition, "transition");
        UpdateResult result = transactions.execute(status -> {
            lockShard(normalized);
            StoredLock before = select(normalized).orElse(null);
            StoredLock after = transition.apply(before);
            if (Objects.equals(before, after)) return new UpdateResult(before, after);
            if (after == null) {
                throw new IllegalStateException("CpfLockStore transitions must retain a tombstone row");
            }
            validateTransitionKey(normalized, after);
            if (before == null) insert(after);
            else updateRow(after, before);
            return new UpdateResult(before, after);
        });
        if (result == null) throw new IllegalStateException("lock store transaction returned no result");
        return result;
    }

    @Override
    public Optional<StoredLock> find(String key) {
        return select(requireKey(key));
    }

    @Override
    public List<StoredLock> list(int limit) {
        int bounded = Math.max(1, Math.min(limit, 10_000));
        String sql = """
                SELECT lock_key, owner_id, request_id, fencing_token, acquired_at, lease_until,
                       lock_state, last_reason, last_audit_id, row_version
                  FROM cpf_distributed_lock
                 ORDER BY lease_until DESC, lock_key ASC
                """;
        return jdbc.query(connection -> {
            var statement = connection.prepareStatement(sql);
            statement.setMaxRows(bounded);
            return statement;
        }, (resultSet, rowNumber) -> map(resultSet));
    }

    @Override
    public long nextFence(String key) {
        String normalized = requireKey(key);
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("nextFence must be called inside CpfLockStore.update transaction");
        }
        lockShard(normalized);
        Long current = jdbc.query("SELECT fencing_token FROM cpf_distributed_lock WHERE lock_key = ?",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null, normalized);
        if (current == null) return 1L;
        if (current == Long.MAX_VALUE) throw new IllegalStateException("fencing token exhausted for key");
        return current + 1L;
    }

    private void lockShard(String key) {
        int shard = Math.floorMod(key.hashCode(), SHARD_COUNT);
        Integer found = jdbc.query("SELECT shard_id FROM cpf_lock_shard WHERE shard_id = ? FOR UPDATE",
                resultSet -> resultSet.next() ? resultSet.getInt(1) : null, shard);
        if (found == null) {
            throw new IllegalStateException("cpf_lock_shard seed row is missing: " + shard);
        }
    }

    private Optional<StoredLock> select(String key) {
        List<StoredLock> rows = jdbc.query("""
                SELECT lock_key, owner_id, request_id, fencing_token, acquired_at, lease_until,
                       lock_state, last_reason, last_audit_id, row_version
                  FROM cpf_distributed_lock
                 WHERE lock_key = ?
                """, (resultSet, rowNumber) -> map(resultSet), key);
        if (rows.size() > 1) throw new IllegalStateException("duplicate lock rows for key");
        return rows.stream().findFirst();
    }

    private void insert(StoredLock value) {
        int inserted = jdbc.update("""
                INSERT INTO cpf_distributed_lock
                       (lock_key, owner_id, request_id, fencing_token, acquired_at, lease_until,
                        lock_state, last_reason, last_audit_id, row_version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, value.key(), value.ownerId(), value.requestId(), value.fencingToken(),
                Timestamp.from(value.acquiredAt()), Timestamp.from(value.leaseUntil()),
                value.state().name(), value.lastReason(), value.lastAuditId(), value.rowVersion());
        if (inserted != 1) throw new IllegalStateException("lock insert affected " + inserted + " rows");
    }

    private void updateRow(StoredLock value, StoredLock expected) {
        if (expected.rowVersion() == Long.MAX_VALUE
                || value.rowVersion() != expected.rowVersion() + 1L) {
            throw new IllegalStateException("lock update must increment row version exactly once");
        }
        int updated = jdbc.update("""
                UPDATE cpf_distributed_lock
                   SET owner_id = ?, request_id = ?, fencing_token = ?, acquired_at = ?, lease_until = ?,
                       lock_state = ?, last_reason = ?, last_audit_id = ?, row_version = ?
                 WHERE lock_key = ? AND fencing_token = ? AND row_version = ?
                """, value.ownerId(), value.requestId(), value.fencingToken(),
                Timestamp.from(value.acquiredAt()), Timestamp.from(value.leaseUntil()), value.state().name(),
                value.lastReason(), value.lastAuditId(), value.rowVersion(), value.key(),
                expected.fencingToken(), expected.rowVersion());
        if (updated != 1) throw new IllegalStateException("stale lock writer detected for key");
    }

    private static StoredLock map(ResultSet resultSet) throws SQLException {
        Timestamp acquired = resultSet.getTimestamp("acquired_at");
        Timestamp lease = resultSet.getTimestamp("lease_until");
        if (acquired == null || lease == null) throw new SQLException("lock timestamps are required");
        long fence = resultSet.getLong("fencing_token");
        long version = resultSet.getLong("row_version");
        if (version < 1) throw new SQLException("lock row version is required");
        return new StoredLock(
                resultSet.getString("lock_key"), resultSet.getString("owner_id"),
                resultSet.getString("request_id"), fence, fence, version,
                acquired.toInstant(), lease.toInstant(),
                CpfLockManager.State.valueOf(resultSet.getString("lock_state")),
                resultSet.getString("last_reason"), resultSet.getString("last_audit_id"));
    }

    private static String requireKey(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("lock key is required");
        String normalized = value.trim();
        if (normalized.length() > 200) throw new IllegalArgumentException("lock key exceeds 200 characters");
        return normalized;
    }

    private static void validateTransitionKey(String key, StoredLock value) {
        if (!key.equals(value.key())) throw new IllegalStateException("lock transition changed the key");
        if (value.fencingToken() < 1 || value.ownerEpoch() < 1 || value.rowVersion() < 1
                || value.acquiredAt() == null || value.leaseUntil() == null) {
            throw new IllegalStateException("invalid persisted lock state");
        }
        if (value.ownerEpoch() != value.fencingToken()) {
            throw new IllegalStateException("owner epoch must equal fencing epoch");
        }
    }
}
