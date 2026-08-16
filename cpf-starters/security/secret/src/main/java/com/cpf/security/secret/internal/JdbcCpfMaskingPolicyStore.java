package com.cpf.security.secret.internal;

import com.cpf.security.api.CpfMaskingPolicyRuntimeStatus;
import com.cpf.security.api.CpfMaskingPolicySnapshot;
import com.cpf.security.spi.CpfMaskingPolicyStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.function.Supplier;
import javax.sql.DataSource;

/**
 * Shared JDBC masking-policy store with a single serialized control shard, optimistic
 * versioning and durable command deduplication.
 *
 * <p>The provider deliberately stores only a SHA-256 digest of command identifiers. The
 * canonical three-vendor DDL is owned by the DB workstream; this runtime provider fails
 * fast when the required shard/head/version/command schema is unavailable.</p>
 */
public final class JdbcCpfMaskingPolicyStore implements CpfMaskingPolicyStore {
    public static final int CONTROL_SHARD = 0;
    public static final int DEFAULT_MAXIMUM_HISTORY = 256;
    public static final int DEFAULT_MAXIMUM_COMMAND_RECORDS = 16_384;
    public static final Duration DEFAULT_COMMAND_TTL = Duration.ofDays(7);
    private static final Pattern COMMAND_ID = Pattern.compile("[A-Za-z0-9_.:-]{8,128}");

    private final Access access;
    private final int maximumHistory;
    private final int maximumCommandRecords;
    private final Duration commandTtl;
    private final Clock clock;
    private final AtomicLong rejectedCommands = new AtomicLong();
    private final AtomicLong providerFailures = new AtomicLong();

    public JdbcCpfMaskingPolicyStore(DataSource dataSource, CpfMaskingPolicySnapshot initial) {
        this(new DataSourceAccess(dataSource), initial, DEFAULT_MAXIMUM_HISTORY,
                DEFAULT_MAXIMUM_COMMAND_RECORDS, DEFAULT_COMMAND_TTL, Clock.systemUTC());
    }

    public JdbcCpfMaskingPolicyStore(
            DataSource dataSource,
            CpfMaskingPolicySnapshot initial,
            int maximumHistory,
            int maximumCommandRecords,
            Duration commandTtl,
            Clock clock) {
        this(new DataSourceAccess(dataSource), initial, maximumHistory,
                maximumCommandRecords, commandTtl, clock);
    }

    JdbcCpfMaskingPolicyStore(
            Access access,
            CpfMaskingPolicySnapshot initial,
            int maximumHistory,
            int maximumCommandRecords,
            Duration commandTtl,
            Clock clock) {
        this.access = Objects.requireNonNull(access, "access");
        if (maximumHistory < 2 || maximumHistory > 4_096) {
            throw new IllegalArgumentException("maximumHistory must be between 2 and 4096");
        }
        if (maximumCommandRecords < 16 || maximumCommandRecords > 65_536) {
            throw new IllegalArgumentException(
                    "maximumCommandRecords must be between 16 and 65536");
        }
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("commandTtl must be positive and <= 365d");
        }
        this.maximumHistory = maximumHistory;
        this.maximumCommandRecords = maximumCommandRecords;
        this.commandTtl = commandTtl;
        this.clock = Objects.requireNonNull(clock, "clock");
        initialize(Objects.requireNonNull(initial, "initial"));
    }

    @Override
    public Optional<CpfMaskingPolicySnapshot> current() {
        try {
            return access.current();
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public Optional<CpfMaskingPolicySnapshot> findVersion(long version) {
        if (version < 1L) throw new IllegalArgumentException("version must be positive");
        try {
            return access.findVersion(version);
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public List<CpfMaskingPolicySnapshot> history(int limit) {
        if (limit < 1 || limit > 1_000) {
            throw new IllegalArgumentException("limit must be between 1 and 1000");
        }
        try {
            return List.copyOf(access.history(Math.min(limit, maximumHistory)));
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public WriteResult compareAndSet(
            long expectedVersion,
            String commandId,
            String commandHash,
            CpfMaskingPolicySnapshot next) {
        String commandIdHash = hashIdentifier(required(commandId, "commandId"));
        String normalizedCommandHash = sha256(commandHash, "commandHash");
        CpfMaskingPolicySnapshot requested = Objects.requireNonNull(next, "next");
        if (expectedVersion < 1L || requested.version() != Math.addExact(expectedVersion, 1L)) {
            throw new IllegalArgumentException("next version must follow expectedVersion");
        }
        try {
            WriteResult result = access.transaction(() -> {
                access.lockControlShard(CONTROL_SHARD);
                access.deleteCommandsBefore(safeMinus(clock.instant(), commandTtl));

                Optional<CommandRow> previous = access.findCommand(commandIdHash);
                if (previous.isPresent()) {
                    CommandRow row = previous.get();
                    if (MessageDigest.isEqual(
                            row.commandHash().getBytes(StandardCharsets.US_ASCII),
                            normalizedCommandHash.getBytes(StandardCharsets.US_ASCII))) {
                        return new WriteResult(Status.IDEMPOTENT_REPLAY, row.snapshot());
                    }
                    return new WriteResult(Status.COMMAND_CONFLICT, access.current().orElse(null));
                }

                CpfMaskingPolicySnapshot active = access.current().orElseThrow(
                        () -> new IllegalStateException("masking policy head is not initialized"));
                if (active.version() != expectedVersion) {
                    return new WriteResult(Status.VERSION_CONFLICT, active);
                }
                if (access.countCommands() >= maximumCommandRecords) {
                    return new WriteResult(Status.RESOURCE_EXHAUSTED, active);
                }

                access.insertVersion(requested);
                if (access.compareAndSetHead(expectedVersion, requested.version()) != 1) {
                    throw new IllegalStateException("masking policy head changed while locked");
                }
                access.insertCommand(commandIdHash, normalizedCommandHash, requested, clock.instant());
                trimHistory(requested.version());
                return new WriteResult(Status.APPLIED, requested);
            });
            WriteResult normalized = result == null
                    ? new WriteResult(Status.RESOURCE_EXHAUSTED, current().orElse(null))
                    : result;
            if (normalized.status() == Status.COMMAND_CONFLICT
                    || normalized.status() == Status.VERSION_CONFLICT
                    || normalized.status() == Status.RESOURCE_EXHAUSTED) {
                rejectedCommands.incrementAndGet();
            }
            return normalized;
        } catch (CommitOutcomeUnknownException unknown) {
            providerFailures.incrementAndGet();
            return new WriteResult(Status.UNKNOWN, requested);
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public CpfMaskingPolicyRuntimeStatus runtimeStatus() {
        try {
            CpfMaskingPolicySnapshot active = access.current().orElseThrow(
                    () -> new IllegalStateException("masking policy head is not initialized"));
            long historyCount = access.countVersions();
            long commandCount = access.countCommands();
            boolean invalidCapacity = historyCount > maximumHistory
                    || commandCount > maximumCommandRecords;
            CpfMaskingPolicyRuntimeStatus.Health health = invalidCapacity
                    ? CpfMaskingPolicyRuntimeStatus.Health.DOWN
                    : providerFailures.get() > 0L || rejectedCommands.get() > 0L
                            ? CpfMaskingPolicyRuntimeStatus.Health.DEGRADED
                            : CpfMaskingPolicyRuntimeStatus.Health.UP;
            return new CpfMaskingPolicyRuntimeStatus(
                    health,
                    active.version(),
                    boundedInt(historyCount, maximumHistory),
                    boundedInt(commandCount, maximumCommandRecords),
                    maximumHistory,
                    maximumCommandRecords,
                    rejectedCommands.get(),
                    0L,
                    clock.instant());
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            return new CpfMaskingPolicyRuntimeStatus(
                    CpfMaskingPolicyRuntimeStatus.Health.DOWN,
                    1L,
                    0,
                    0,
                    maximumHistory,
                    maximumCommandRecords,
                    rejectedCommands.get(),
                    0L,
                    clock.instant());
        }
    }

    private void initialize(CpfMaskingPolicySnapshot initial) {
        try {
            access.transaction(() -> {
                access.lockControlShard(CONTROL_SHARD);
                Optional<CpfMaskingPolicySnapshot> current = access.current();
                if (current.isEmpty()) {
                    access.insertVersion(initial);
                    access.insertHead(initial.version());
                }
                return null;
            });
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw new IllegalStateException(
                    "masking policy JDBC schema is unavailable or not seeded", failure);
        }
    }

    private void trimHistory(long activeVersion) {
        long count = access.countVersions();
        int guard = maximumHistory + 1;
        while (count > maximumHistory && guard-- > 0) {
            Optional<Long> oldest = access.oldestDeletableVersion(activeVersion);
            if (oldest.isEmpty()) {
                throw new IllegalStateException("masking policy history cannot be bounded");
            }
            if (access.deleteVersion(oldest.get()) != 1) {
                throw new IllegalStateException("masking policy history trim conflicted");
            }
            count--;
        }
        if (count > maximumHistory) {
            throw new IllegalStateException("masking policy history capacity remains exceeded");
        }
    }

    private static int boundedInt(long value, int maximum) {
        if (value < 0L) throw new IllegalStateException("negative store count");
        return (int) Math.min(value, maximum);
    }

    private static Instant safeMinus(Instant instant, Duration duration) {
        try {
            return instant.minus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MIN;
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String normalized = value.trim();
        if (!COMMAND_ID.matcher(normalized).matches()
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }

    private static String sha256(String value, String field) {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(field + " must be lowercase SHA-256");
        }
        return value;
    }

    private static String hashIdentifier(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 unavailable", unavailable);
        }
    }

    static final class CommitOutcomeUnknownException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        CommitOutcomeUnknownException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    interface Access {
        <T> T transaction(Supplier<T> callback);
        void lockControlShard(int shardId);
        Optional<CpfMaskingPolicySnapshot> current();
        Optional<CpfMaskingPolicySnapshot> findVersion(long version);
        List<CpfMaskingPolicySnapshot> history(int limit);
        Optional<CommandRow> findCommand(String commandIdHash);
        long countVersions();
        long countCommands();
        int deleteCommandsBefore(Instant cutoff);
        void insertVersion(CpfMaskingPolicySnapshot snapshot);
        void insertHead(long activeVersion);
        int compareAndSetHead(long expectedVersion, long nextVersion);
        void insertCommand(
                String commandIdHash,
                String commandHash,
                CpfMaskingPolicySnapshot snapshot,
                Instant recordedAt);
        Optional<Long> oldestDeletableVersion(long activeVersion);
        int deleteVersion(long version);
    }

    record CommandRow(String commandHash, CpfMaskingPolicySnapshot snapshot) {
        CommandRow {
            sha256(commandHash, "commandHash");
            Objects.requireNonNull(snapshot, "snapshot");
        }
    }

    private static final class DataSourceAccess implements Access {
        private final DataSource dataSource;
        private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

        private DataSourceAccess(DataSource dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        }

        @Override
        public <T> T transaction(Supplier<T> callback) {
            if (transactionConnection.get() != null) {
                throw new IllegalStateException("nested masking policy transaction is forbidden");
            }
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                transactionConnection.set(connection);
                final T result;
                try {
                    result = callback.get();
                } catch (RuntimeException failure) {
                    rollback(connection, failure);
                    throw failure;
                }
                try {
                    connection.commit();
                } catch (SQLException uncertain) {
                    throw new CommitOutcomeUnknownException(
                            "masking policy commit outcome is unknown", uncertain);
                }
                return result;
            } catch (SQLException failure) {
                throw new IllegalStateException("masking policy transaction failed", failure);
            } finally {
                transactionConnection.remove();
                closeQuietly(connection);
            }
        }

        @Override
        public void lockControlShard(int shardId) {
            queryOne("SELECT shard_id FROM cpf_masking_policy_shard "
                    + "WHERE shard_id = ? FOR UPDATE", statement -> statement.setInt(1, shardId),
                    resultSet -> resultSet.getInt(1)).orElseThrow(
                            () -> new IllegalStateException(
                                    "cpf_masking_policy_shard seed row is missing: " + shardId));
        }

        @Override
        public Optional<CpfMaskingPolicySnapshot> current() {
            return queryOne("SELECT v.policy_version, v.sensitive_keys_csv, v.max_length, "
                            + "v.mask_bearer_flag, v.updated_at, v.updated_by, v.update_reason "
                            + "FROM cpf_masking_policy_head h JOIN cpf_masking_policy_version v "
                            + "ON v.policy_version = h.active_version WHERE h.singleton_id = 1",
                    statement -> { }, DataSourceAccess::mapSnapshot);
        }

        @Override
        public Optional<CpfMaskingPolicySnapshot> findVersion(long version) {
            return queryOne("SELECT policy_version, sensitive_keys_csv, max_length, "
                            + "mask_bearer_flag, updated_at, updated_by, update_reason "
                            + "FROM cpf_masking_policy_version WHERE policy_version = ?",
                    statement -> statement.setLong(1, version), DataSourceAccess::mapSnapshot);
        }

        @Override
        public List<CpfMaskingPolicySnapshot> history(int limit) {
            return queryMany(
                    "SELECT policy_version, sensitive_keys_csv, max_length, mask_bearer_flag, "
                            + "updated_at, updated_by, update_reason FROM ("
                            + "SELECT policy_version, sensitive_keys_csv, max_length, "
                            + "mask_bearer_flag, updated_at, updated_by, update_reason, "
                            + "ROW_NUMBER() OVER(ORDER BY policy_version DESC) cpf_rn "
                            + "FROM cpf_masking_policy_version) cpf_page WHERE cpf_rn <= ? "
                            + "ORDER BY cpf_rn",
                    statement -> statement.setInt(1, limit), DataSourceAccess::mapSnapshot);
        }

        @Override
        public Optional<CommandRow> findCommand(String commandIdHash) {
            return queryOne("SELECT command_hash, result_version, result_sensitive_keys_csv, "
                            + "result_max_length, result_mask_bearer_flag, result_updated_at, "
                            + "result_updated_by, result_reason FROM cpf_masking_policy_command "
                            + "WHERE command_id_hash = ?",
                    statement -> statement.setString(1, commandIdHash),
                    resultSet -> new CommandRow(
                            resultSet.getString("command_hash"),
                            new CpfMaskingPolicySnapshot(
                                    resultSet.getLong("result_version"),
                                    decodeKeys(resultSet.getString("result_sensitive_keys_csv")),
                                    resultSet.getInt("result_max_length"),
                                    resultSet.getBoolean("result_mask_bearer_flag"),
                                    requiredTimestamp(resultSet, "result_updated_at").toInstant(),
                                    resultSet.getString("result_updated_by"),
                                    resultSet.getString("result_reason"))));
        }

        @Override
        public long countVersions() {
            return count("SELECT COUNT(*) FROM cpf_masking_policy_version");
        }

        @Override
        public long countCommands() {
            return count("SELECT COUNT(*) FROM cpf_masking_policy_command");
        }

        @Override
        public int deleteCommandsBefore(Instant cutoff) {
            return update("DELETE FROM cpf_masking_policy_command WHERE recorded_at < ?",
                    statement -> statement.setTimestamp(1, Timestamp.from(cutoff)));
        }

        @Override
        public void insertVersion(CpfMaskingPolicySnapshot snapshot) {
            int inserted = update("INSERT INTO cpf_masking_policy_version("
                            + "policy_version, sensitive_keys_csv, max_length, mask_bearer_flag, "
                            + "updated_at, updated_by, update_reason) VALUES(?,?,?,?,?,?,?)",
                    statement -> bindSnapshot(statement, snapshot, 1));
            if (inserted != 1) throw new IllegalStateException("masking policy version insert failed");
        }

        @Override
        public void insertHead(long activeVersion) {
            int inserted = update("INSERT INTO cpf_masking_policy_head(singleton_id, active_version) "
                            + "VALUES(1, ?)", statement -> statement.setLong(1, activeVersion));
            if (inserted != 1) throw new IllegalStateException("masking policy head insert failed");
        }

        @Override
        public int compareAndSetHead(long expectedVersion, long nextVersion) {
            return update("UPDATE cpf_masking_policy_head SET active_version = ? "
                            + "WHERE singleton_id = 1 AND active_version = ?",
                    statement -> {
                        statement.setLong(1, nextVersion);
                        statement.setLong(2, expectedVersion);
                    });
        }

        @Override
        public void insertCommand(
                String commandIdHash,
                String commandHash,
                CpfMaskingPolicySnapshot snapshot,
                Instant recordedAt) {
            int inserted = update("INSERT INTO cpf_masking_policy_command("
                            + "command_id_hash, command_hash, result_version, "
                            + "result_sensitive_keys_csv, result_max_length, "
                            + "result_mask_bearer_flag, result_updated_at, result_updated_by, "
                            + "result_reason, recorded_at) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    statement -> {
                        statement.setString(1, commandIdHash);
                        statement.setString(2, commandHash);
                        statement.setLong(3, snapshot.version());
                        statement.setString(4, encodeKeys(snapshot.sensitiveKeys()));
                        statement.setInt(5, snapshot.maxLength());
                        statement.setBoolean(6, snapshot.maskBearerToken());
                        statement.setTimestamp(7, Timestamp.from(snapshot.updatedAt()));
                        statement.setString(8, snapshot.updatedBy());
                        statement.setString(9, snapshot.reason());
                        statement.setTimestamp(10, Timestamp.from(recordedAt));
                    });
            if (inserted != 1) throw new IllegalStateException("masking policy command insert failed");
        }

        @Override
        public Optional<Long> oldestDeletableVersion(long activeVersion) {
            return queryOne(
                    "SELECT policy_version FROM cpf_masking_policy_version "
                            + "WHERE policy_version = (SELECT MIN(policy_version) "
                            + "FROM cpf_masking_policy_version WHERE policy_version <> ?)",
                    statement -> statement.setLong(1, activeVersion),
                    resultSet -> resultSet.getLong(1));
        }

        @Override
        public int deleteVersion(long version) {
            return update("DELETE FROM cpf_masking_policy_version WHERE policy_version = ?",
                    statement -> statement.setLong(1, version));
        }

        private long count(String sql) {
            return queryOne(sql, statement -> { }, resultSet -> resultSet.getLong(1))
                    .filter(value -> value >= 0L)
                    .orElseThrow(() -> new IllegalStateException("invalid masking policy count"));
        }

        private <T> Optional<T> queryOne(
                String sql, Binder binder, RowMapper<T> mapper) {
            List<T> rows = queryMany(sql, binder, mapper);
            if (rows.size() > 1) throw new IllegalStateException("duplicate masking policy rows");
            return rows.stream().findFirst();
        }

        private <T> List<T> queryMany(
                String sql, Binder binder, RowMapper<T> mapper) {
            return withConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    binder.bind(statement);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        List<T> rows = new ArrayList<>();
                        while (resultSet.next()) rows.add(mapper.map(resultSet));
                        return List.copyOf(rows);
                    }
                }
            });
        }

        private int update(String sql, Binder binder) {
            return withConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    binder.bind(statement);
                    return statement.executeUpdate();
                }
            });
        }

        private <T> T withConnection(SqlFunction<T> callback) {
            Connection existing = transactionConnection.get();
            if (existing != null) {
                try {
                    return callback.apply(existing);
                } catch (SQLException failure) {
                    throw new IllegalStateException("masking policy SQL failed", failure);
                }
            }
            try (Connection connection = dataSource.getConnection()) {
                return callback.apply(connection);
            } catch (SQLException failure) {
                throw new IllegalStateException("masking policy SQL failed", failure);
            }
        }

        private static void rollback(Connection connection, RuntimeException original) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                original.addSuppressed(rollbackFailure);
            }
        }

        private static void closeQuietly(Connection connection) {
            if (connection == null) return;
            try {
                connection.close();
            } catch (SQLException ignored) {
                // A close failure after a known commit must not turn success into UNKNOWN.
            }
        }

        private static CpfMaskingPolicySnapshot mapSnapshot(ResultSet resultSet)
                throws SQLException {
            return new CpfMaskingPolicySnapshot(
                    resultSet.getLong("policy_version"),
                    decodeKeys(resultSet.getString("sensitive_keys_csv")),
                    resultSet.getInt("max_length"),
                    resultSet.getBoolean("mask_bearer_flag"),
                    requiredTimestamp(resultSet, "updated_at").toInstant(),
                    resultSet.getString("updated_by"),
                    resultSet.getString("update_reason"));
        }

        private static void bindSnapshot(
                PreparedStatement statement, CpfMaskingPolicySnapshot snapshot, int offset)
                throws SQLException {
            statement.setLong(offset, snapshot.version());
            statement.setString(offset + 1, encodeKeys(snapshot.sensitiveKeys()));
            statement.setInt(offset + 2, snapshot.maxLength());
            statement.setBoolean(offset + 3, snapshot.maskBearerToken());
            statement.setTimestamp(offset + 4, Timestamp.from(snapshot.updatedAt()));
            statement.setString(offset + 5, snapshot.updatedBy());
            statement.setString(offset + 6, snapshot.reason());
        }

        private static Timestamp requiredTimestamp(ResultSet resultSet, String column)
                throws SQLException {
            Timestamp timestamp = resultSet.getTimestamp(column);
            if (timestamp == null) throw new SQLException(column + " is null");
            return timestamp;
        }

        private static String encodeKeys(Set<String> keys) {
            return String.join(",", keys.stream().sorted().toList());
        }

        private static Set<String> decodeKeys(String value) {
            if (value == null || value.isBlank()) return Set.of();
            LinkedHashSet<String> keys = new LinkedHashSet<>();
            for (String key : value.split(",", -1)) {
                if (!keys.add(key)) throw new IllegalStateException("duplicate stored sensitive key");
            }
            return Set.copyOf(keys);
        }

        @FunctionalInterface
        private interface Binder {
            void bind(PreparedStatement statement) throws SQLException;
        }

        @FunctionalInterface
        private interface RowMapper<T> {
            T map(ResultSet resultSet) throws SQLException;
        }

        @FunctionalInterface
        private interface SqlFunction<T> {
            T apply(Connection connection) throws SQLException;
        }
    }
}
