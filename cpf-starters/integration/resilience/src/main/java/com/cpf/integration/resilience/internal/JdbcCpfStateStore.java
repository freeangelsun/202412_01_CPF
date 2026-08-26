package com.cpf.integration.resilience.internal;

import com.cpf.platform.operations.api.state.CpfOperationState;
import com.cpf.platform.operations.api.state.CpfStateIdentifiers;
import com.cpf.platform.operations.api.state.CpfStateRuntimeStatus;
import com.cpf.platform.operations.api.state.CpfStateSearchRequest;
import com.cpf.platform.operations.api.state.CpfStateSnapshot;
import com.cpf.platform.operations.spi.state.CpfStateStore;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Shared JDBC state store with atomic version CAS, operation-id dedupe and bounded
 * durable command retention.
 *
 * <p>All mutations lock the capacity, command-ledger and key shard rows in a fixed order.
 * This deliberately trades some write throughput for deterministic multi-process safety.
 * Deployments requiring higher throughput can provide another {@link CpfStateStore}
 * implementation without changing the public state contract.</p>
 */
final class JdbcCpfStateStore implements CpfStateStore, CpfStateRuntimeStatus {
    static final int KEY_SHARD_COUNT = 256;
    static final int STATE_CAPACITY_SHARD = 256;
    static final int COMMAND_CAPACITY_SHARD = 257;
    static final int REQUIRED_SHARD_ROWS = 258;
    static final int DEFAULT_MAXIMUM_STATES = 1_000_000;
    static final int DEFAULT_MAXIMUM_COMMANDS = 10_000_000;
    static final Duration DEFAULT_COMMAND_TTL = Duration.ofHours(24);

    private final Access access;
    private final int maximumStates;
    private final int maximumCommands;
    private final Duration commandTtl;
    private final Clock clock;
    private final AtomicLong applied = new AtomicLong();
    private final AtomicLong replays = new AtomicLong();
    private final AtomicLong versionConflicts = new AtomicLong();
    private final AtomicLong operationConflicts = new AtomicLong();
    private final AtomicLong resourceExhausted = new AtomicLong();
    private final AtomicLong providerFailures = new AtomicLong();

    JdbcCpfStateStore(JdbcTemplate jdbc, TransactionTemplate transactions) {
        this(new SpringAccess(jdbc, transactions), DEFAULT_MAXIMUM_STATES,
                DEFAULT_MAXIMUM_COMMANDS, DEFAULT_COMMAND_TTL, Clock.systemUTC());
    }

    JdbcCpfStateStore(
            JdbcTemplate jdbc,
            TransactionTemplate transactions,
            int maximumStates,
            int maximumCommands,
            Duration commandTtl,
            Clock clock) {
        this(new SpringAccess(jdbc, transactions), maximumStates, maximumCommands, commandTtl, clock);
    }

    JdbcCpfStateStore(Access access) {
        this(access, DEFAULT_MAXIMUM_STATES, DEFAULT_MAXIMUM_COMMANDS,
                DEFAULT_COMMAND_TTL, Clock.systemUTC());
    }

    JdbcCpfStateStore(
            Access access,
            int maximumStates,
            int maximumCommands,
            Duration commandTtl,
            Clock clock) {
        this.access = Objects.requireNonNull(access, "access");
        if (maximumStates < 1 || maximumStates > 1_000_000) {
            throw new IllegalArgumentException("maximumStates must be between 1 and 1000000");
        }
        if (maximumCommands < 1 || maximumCommands > 10_000_000) {
            throw new IllegalArgumentException("maximumCommands must be between 1 and 10000000");
        }
        if (maximumCommands < maximumStates) {
            throw new IllegalArgumentException("maximumCommands must be >= maximumStates");
        }
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("commandTtl must be positive and <= 365 days");
        }
        this.maximumStates = maximumStates;
        this.maximumCommands = maximumCommands;
        this.commandTtl = commandTtl;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public Optional<CpfStateSnapshot> find(String stateKey) {
        try {
            return access.findState(CpfStateIdentifiers.stateKey(stateKey));
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public WriteResult compareAndSet(
            String stateKey,
            long expectedVersion,
            String operationId,
            String commandHash,
            CpfStateSnapshot next) {
        try {
            String key = CpfStateIdentifiers.stateKey(stateKey);
            String operation = CpfStateIdentifiers.operationId(operationId);
            validateCommand(key, expectedVersion, operation, commandHash, next);
            WriteResult result = access.transaction(() -> {
                access.lockShard(STATE_CAPACITY_SHARD);
                access.lockShard(COMMAND_CAPACITY_SHARD);
                access.lockShard(Math.floorMod(key.hashCode(), KEY_SHARD_COUNT));
                access.deleteCommandsBefore(safeMinus(clock.instant(), commandTtl));

                Optional<CommandRow> previous = access.findCommand(key, operation);
                if (previous.isPresent()) {
                    CommandRow command = previous.get();
                    if (command.commandHash().equals(commandHash)) {
                        return new WriteResult(Status.IDEMPOTENT_REPLAY, command.snapshot());
                    }
                    return new WriteResult(
                            Status.OPERATION_CONFLICT, access.findState(key).orElse(null));
                }

                Optional<CpfStateSnapshot> found = access.findState(key);
                CpfStateSnapshot current = found.orElse(null);
                boolean matches = expectedVersion < 0L
                        ? current == null
                        : current != null && current.version() == expectedVersion;
                if (!matches) return new WriteResult(Status.CONFLICT, current);
                if (current == null && access.countStates() >= maximumStates) {
                    return new WriteResult(Status.RESOURCE_EXHAUSTED, null);
                }
                if (access.countCommands() >= maximumCommands) {
                    return new WriteResult(Status.RESOURCE_EXHAUSTED, current);
                }

                if (current == null) access.insertState(next);
                else access.updateState(next, expectedVersion);
                access.insertCommand(key, operation, commandHash, next, clock.instant());
                return new WriteResult(Status.APPLIED, next);
            });
            WriteResult normalized = result == null
                    ? new WriteResult(Status.UNKNOWN, null)
                    : result;
            record(normalized.status());
            return normalized;
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public SearchResult search(CpfStateSearchRequest request) {
        Objects.requireNonNull(request, "request");
        try {
            List<CpfStateSnapshot> rows = access.search(request, request.pageSize() + 1);
            boolean more = rows.size() > request.pageSize();
            List<CpfStateSnapshot> page = more
                    ? List.copyOf(rows.subList(0, request.pageSize()))
                    : List.copyOf(rows);
            String nextCursor = more && !page.isEmpty()
                    ? page.get(page.size() - 1).stateKey()
                    : null;
            return new SearchResult(SearchStatus.SUCCESS, page, nextCursor);
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            return new SearchResult(SearchStatus.UNKNOWN, List.of(), null);
        }
    }

    @Override
    public RuntimeSnapshot stateRuntimeSnapshot() {
        try {
            long count = access.countStates();
            boolean overCapacity = count > maximumStates;
            int reported = (int) Math.min(count, maximumStates);
            Health health = overCapacity
                    ? Health.DOWN
                    : providerFailures.get() > 0L || resourceExhausted.get() > 0L
                            ? Health.DEGRADED
                            : Health.UP;
            return new RuntimeSnapshot(
                    health,
                    reported,
                    maximumStates,
                    applied.get(),
                    replays.get(),
                    versionConflicts.get(),
                    operationConflicts.get(),
                    resourceExhausted.get(),
                    providerFailures.get(),
                    clock.instant());
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            return new RuntimeSnapshot(
                    Health.DOWN,
                    0,
                    maximumStates,
                    applied.get(),
                    replays.get(),
                    versionConflicts.get(),
                    operationConflicts.get(),
                    resourceExhausted.get(),
                    providerFailures.get(),
                    clock.instant());
        }
    }

    private void record(Status status) {
        switch (status) {
            case APPLIED -> applied.incrementAndGet();
            case IDEMPOTENT_REPLAY -> replays.incrementAndGet();
            case CONFLICT -> versionConflicts.incrementAndGet();
            case OPERATION_CONFLICT -> operationConflicts.incrementAndGet();
            case RESOURCE_EXHAUSTED -> resourceExhausted.incrementAndGet();
            case UNKNOWN -> providerFailures.incrementAndGet();
        }
    }

    private static void validateCommand(
            String stateKey,
            long expectedVersion,
            String operationId,
            String commandHash,
            CpfStateSnapshot next) {
        if (next == null) throw new IllegalArgumentException("next snapshot is required");
        if (!stateKey.equals(next.stateKey())) {
            throw new IllegalArgumentException("stateKey does not match snapshot");
        }
        if (!operationId.equals(next.lastOperationId())) {
            throw new IllegalArgumentException("operationId does not match snapshot");
        }
        if (commandHash == null || !commandHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("commandHash must be lowercase SHA-256");
        }
        long requiredVersion = expectedVersion < 0L ? 0L : Math.addExact(expectedVersion, 1L);
        if (next.version() != requiredVersion) {
            throw new IllegalArgumentException("snapshot version does not follow expectedVersion");
        }
    }

    private static Instant safeMinus(Instant instant, Duration duration) {
        try {
            return instant.minus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MIN;
        }
    }

    interface Access {
        <T> T transaction(java.util.function.Supplier<T> callback);
        void lockShard(int shard);
        Optional<CpfStateSnapshot> findState(String stateKey);
        Optional<CommandRow> findCommand(String stateKey, String operationId);
        long countStates();
        long countCommands();
        int deleteCommandsBefore(Instant cutoff);
        void insertState(CpfStateSnapshot snapshot);
        void updateState(CpfStateSnapshot snapshot, long expectedVersion);
        void insertCommand(
                String stateKey,
                String operationId,
                String commandHash,
                CpfStateSnapshot snapshot,
                Instant recordedAt);
        List<CpfStateSnapshot> search(CpfStateSearchRequest request, int maximumRows);
    }

    record CommandRow(String commandHash, CpfStateSnapshot snapshot) {
        CommandRow {
            if (commandHash == null || snapshot == null) {
                throw new IllegalArgumentException("command row fields are required");
            }
        }
    }

    private static final class SpringAccess implements Access {
        private final JdbcTemplate jdbc;
        private final TransactionTemplate transactions;

        private SpringAccess(JdbcTemplate jdbc, TransactionTemplate transactions) {
            this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
            this.transactions = Objects.requireNonNull(transactions, "transactions");
        }

        @Override
        public <T> T transaction(java.util.function.Supplier<T> callback) {
            return transactions.execute(status -> callback.get());
        }

        @Override
        public void lockShard(int shard) {
            Integer found = jdbc.query(
                    "SELECT shard_id FROM cpf_state_shard WHERE shard_id = ? FOR UPDATE",
                    resultSet -> resultSet.next() ? resultSet.getInt(1) : null,
                    shard);
            if (found == null) {
                throw new IllegalStateException("cpf_state_shard seed row is missing: " + shard);
            }
        }

        @Override
        public Optional<CpfStateSnapshot> findState(String stateKey) {
            List<CpfStateSnapshot> rows = jdbc.query("""
                    SELECT state_key, state_code, state_version, last_operation_id,
                           actor_id, state_reason, updated_at
                      FROM cpf_operation_state
                     WHERE state_key = ?
                    """, (resultSet, rowNumber) -> mapSnapshot(resultSet), stateKey);
            if (rows.size() > 1) throw new IllegalStateException("duplicate state rows for key");
            return rows.stream().findFirst();
        }

        @Override
        public Optional<CommandRow> findCommand(String stateKey, String operationId) {
            List<CommandRow> rows = jdbc.query("""
                    SELECT command_hash, result_state_code, result_version, result_actor_id,
                           result_reason, result_updated_at
                      FROM cpf_state_command
                     WHERE state_key = ? AND operation_id = ?
                    """, (resultSet, rowNumber) -> new CommandRow(
                            resultSet.getString("command_hash"),
                            new CpfStateSnapshot(
                                    stateKey,
                                    CpfOperationState.valueOf(
                                            resultSet.getString("result_state_code")),
                                    resultSet.getLong("result_version"),
                                    operationId,
                                    resultSet.getString("result_actor_id"),
                                    resultSet.getString("result_reason"),
                                    requiredTimestamp(
                                            resultSet, "result_updated_at").toInstant())),
                    stateKey, operationId);
            if (rows.size() > 1) throw new IllegalStateException("duplicate state command rows");
            return rows.stream().findFirst();
        }

        @Override
        public long countStates() {
            Long found = jdbc.query(
                    "SELECT COUNT(*) FROM cpf_operation_state",
                    resultSet -> resultSet.next() ? resultSet.getLong(1) : null);
            if (found == null || found < 0L) {
                throw new IllegalStateException("invalid state count result");
            }
            return found;
        }

        @Override
        public long countCommands() {
            Long found = jdbc.query(
                    "SELECT COUNT(*) FROM cpf_state_command",
                    resultSet -> resultSet.next() ? resultSet.getLong(1) : null);
            if (found == null || found < 0L) {
                throw new IllegalStateException("invalid state command count result");
            }
            return found;
        }

        @Override
        public int deleteCommandsBefore(Instant cutoff) {
            return jdbc.update(
                    "DELETE FROM cpf_state_command WHERE recorded_at < ?",
                    Timestamp.from(cutoff));
        }

        @Override
        public void insertState(CpfStateSnapshot value) {
            int inserted = jdbc.update("""
                    INSERT INTO cpf_operation_state
                           (state_key, state_code, state_version, last_operation_id,
                            actor_id, state_reason, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, value.stateKey(), value.state().name(), value.version(),
                    value.lastOperationId(), value.actor(), value.reason(),
                    Timestamp.from(value.updatedAt()));
            if (inserted != 1) {
                throw new IllegalStateException("state insert affected " + inserted + " rows");
            }
        }

        @Override
        public void updateState(CpfStateSnapshot value, long expectedVersion) {
            int updated = jdbc.update("""
                    UPDATE cpf_operation_state
                       SET state_code = ?, state_version = ?, last_operation_id = ?,
                           actor_id = ?, state_reason = ?, updated_at = ?
                     WHERE state_key = ? AND state_version = ?
                    """, value.state().name(), value.version(), value.lastOperationId(),
                    value.actor(), value.reason(), Timestamp.from(value.updatedAt()),
                    value.stateKey(), expectedVersion);
            if (updated != 1) {
                throw new IllegalStateException("stale state writer detected for key");
            }
        }

        @Override
        public void insertCommand(
                String stateKey,
                String operationId,
                String commandHash,
                CpfStateSnapshot value,
                Instant recordedAt) {
            int inserted = jdbc.update("""
                    INSERT INTO cpf_state_command
                           (state_key, operation_id, command_hash, result_state_code,
                            result_version, result_actor_id, result_reason,
                            result_updated_at, recorded_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, stateKey, operationId, commandHash, value.state().name(), value.version(),
                    value.actor(), value.reason(), Timestamp.from(value.updatedAt()),
                    Timestamp.from(recordedAt));
            if (inserted != 1) {
                throw new IllegalStateException(
                        "state command insert affected " + inserted + " rows");
            }
        }

        @Override
        public List<CpfStateSnapshot> search(
                CpfStateSearchRequest request, int maximumRows) {
            StringBuilder sql = new StringBuilder("""
                    SELECT state_key, state_code, state_version, last_operation_id,
                           actor_id, state_reason, updated_at
                      FROM cpf_operation_state
                     WHERE 1 = 1
                    """);
            List<Object> arguments = new ArrayList<>();
            if (!request.stateKeyPrefix().isEmpty()) {
                sql.append(" AND state_key LIKE ? ESCAPE '\\\\'");
                arguments.add(escapeLike(request.stateKeyPrefix()) + "%");
            }
            if (request.afterStateKey() != null) {
                sql.append(" AND state_key > ?");
                arguments.add(request.afterStateKey());
            }
            StringJoiner placeholders = new StringJoiner(",", "(", ")");
            for (CpfOperationState _ : request.states()) placeholders.add("?");
            sql.append(" AND state_code IN ").append(placeholders);
            for (CpfOperationState state : request.states()) arguments.add(state.name());
            sql.append(" ORDER BY state_key ASC");
            return jdbc.query(connection -> {
                PreparedStatement statement = connection.prepareStatement(sql.toString());
                for (int index = 0; index < arguments.size(); index++) {
                    statement.setObject(index + 1, arguments.get(index));
                }
                statement.setMaxRows(maximumRows);
                return statement;
            }, (resultSet, rowNumber) -> mapSnapshot(resultSet));
        }

        private static CpfStateSnapshot mapSnapshot(ResultSet resultSet) throws SQLException {
            return new CpfStateSnapshot(
                    resultSet.getString("state_key"),
                    CpfOperationState.valueOf(resultSet.getString("state_code")),
                    resultSet.getLong("state_version"),
                    resultSet.getString("last_operation_id"),
                    resultSet.getString("actor_id"),
                    resultSet.getString("state_reason"),
                    requiredTimestamp(resultSet, "updated_at").toInstant());
        }

        private static Timestamp requiredTimestamp(
                ResultSet resultSet, String column) throws SQLException {
            Timestamp value = resultSet.getTimestamp(column);
            if (value == null) throw new SQLException(column + " is required");
            return value;
        }

        private static String escapeLike(String value) {
            return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        }
    }
}
