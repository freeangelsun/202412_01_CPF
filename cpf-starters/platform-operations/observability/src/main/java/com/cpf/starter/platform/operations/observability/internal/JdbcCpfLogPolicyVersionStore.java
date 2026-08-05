package com.cpf.starter.platform.operations.observability.internal;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionRuntimeStatus;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.spi.logging.CpfLogPolicyVersionStore;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.sql.DataSource;

/**
 * Shared JDBC implementation for versioned log policies.
 *
 * <p>The store serializes one target and the global command ledger in a fixed lock order,
 * persists hashes instead of raw target/command/actor identifiers, and reports an explicit
 * {@link Status#UNKNOWN} when a JDBC commit response is lost.</p>
 */
public final class JdbcCpfLogPolicyVersionStore implements CpfLogPolicyVersionStore {
    public static final int TARGET_SHARD_COUNT = 256;
    public static final int COMMAND_LEDGER_SHARD = 256;
    public static final int TARGET_CAPACITY_SHARD = 257;
    public static final int REQUIRED_SHARD_ROWS = 258;
    public static final int DEFAULT_MAXIMUM_TARGETS = 100_000;
    public static final int DEFAULT_MAXIMUM_HISTORY_PER_TARGET = 128;
    public static final int DEFAULT_MAXIMUM_COMMAND_RECORDS = 1_000_000;
    public static final Duration DEFAULT_COMMAND_TTL = Duration.ofDays(7);
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_.:-]{8,128}");

    private final Access access;
    private final int maximumTargets;
    private final int maximumHistoryPerTarget;
    private final int maximumCommandRecords;
    private final Duration commandTtl;
    private final Clock clock;
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong unknown = new AtomicLong();
    private final AtomicLong providerFailures = new AtomicLong();

    public JdbcCpfLogPolicyVersionStore(DataSource dataSource, int maximumTargets,
            int maximumHistoryPerTarget, int maximumCommandRecords, Duration commandTtl, Clock clock) {
        this(new DataSourceAccess(dataSource), maximumTargets, maximumHistoryPerTarget,
                maximumCommandRecords, commandTtl, clock);
    }

    JdbcCpfLogPolicyVersionStore(Access access, int maximumTargets,
            int maximumHistoryPerTarget, int maximumCommandRecords, Duration commandTtl, Clock clock) {
        this.access = Objects.requireNonNull(access, "access");
        if (maximumTargets < 1 || maximumTargets > 1_000_000) {
            throw new IllegalArgumentException("maximumTargets must be between 1 and 1000000");
        }
        if (maximumHistoryPerTarget < 2 || maximumHistoryPerTarget > 4_096) {
            throw new IllegalArgumentException("maximumHistoryPerTarget must be between 2 and 4096");
        }
        if (maximumCommandRecords < 16 || maximumCommandRecords > 10_000_000) {
            throw new IllegalArgumentException("maximumCommandRecords must be between 16 and 10000000");
        }
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("commandTtl must be positive and <= 365 days");
        }
        this.maximumTargets = maximumTargets;
        this.maximumHistoryPerTarget = maximumHistoryPerTarget;
        this.maximumCommandRecords = maximumCommandRecords;
        this.commandTtl = commandTtl;
        this.clock = Objects.requireNonNull(clock, "clock");
    }


    /** Verifies required shared tables and the complete shard seed before exposing the provider. */
    public void verifySchema() {
        try {
            access.transaction(() -> {
                access.verifySchema();
                return null;
            });
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw new IllegalStateException("log policy JDBC schema is unavailable or not seeded", failure);
        }
    }

    @Override
    public CpfLogPolicyVersionSnapshot ensureBaseline(CpfLogPolicyVersionSnapshot baseline) {
        Objects.requireNonNull(baseline, "baseline");
        if (baseline.version() != 1L || baseline.status() != CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            throw new IllegalArgumentException("baseline must be ACTIVE version 1");
        }
        String targetHash = targetHash(baseline.targetType(), baseline.targetId());
        try {
            return access.transaction(() -> {
                lock(targetHash);
                Optional<CpfLogPolicyVersionSnapshot> existing = access.current(
                        baseline.targetType(), targetHash, baseline.targetId());
                if (existing.isPresent()) return existing.get();
                if (access.countTargets() >= maximumTargets) {
                    throw new IllegalStateException("log policy target capacity exhausted");
                }
                access.insertVersion(targetHash, baseline);
                access.insertHead(baseline.targetType(), targetHash, baseline.version(), clock.instant());
                return baseline;
            });
        } catch (CommitOutcomeUnknownException uncertain) {
            unknown.incrementAndGet();
            return access.transaction(() -> access.current(
                    baseline.targetType(), targetHash, baseline.targetId()))
                    .orElseThrow(() -> new IllegalStateException(
                            "baseline commit outcome is unknown", uncertain));
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public Optional<CpfLogPolicyVersionSnapshot> current(LogPolicyTargetType type, String targetId) {
        Objects.requireNonNull(type, "type");
        String normalized = LogPolicyDecision.normalizeTargetId(targetId);
        try {
            return access.transaction(() -> access.current(type, targetHash(type, normalized), normalized));
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public Optional<CpfLogPolicyVersionSnapshot> findVersion(
            LogPolicyTargetType type, String targetId, long version) {
        if (version < 1L) return Optional.empty();
        Objects.requireNonNull(type, "type");
        String normalized = LogPolicyDecision.normalizeTargetId(targetId);
        try {
            return access.transaction(() -> access.findVersion(type, targetHash(type, normalized), normalized, version));
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public List<CpfLogPolicyVersionSnapshot> history(
            LogPolicyTargetType type, String targetId, int limit) {
        Objects.requireNonNull(type, "type");
        String normalized = LogPolicyDecision.normalizeTargetId(targetId);
        int bounded = Math.max(1, Math.min(limit, maximumHistoryPerTarget));
        try {
            return access.transaction(() -> List.copyOf(access.history(type, targetHash(type, normalized), normalized, bounded)));
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public WriteResult compareAndSet(long expectedVersion, String commandId,
            String commandHash, CpfLogPolicyVersionSnapshot next) {
        Objects.requireNonNull(next, "next");
        commandId = identifier(commandId, "commandId");
        commandHash = lowerHash(commandHash, "commandHash");
        if (expectedVersion < 1L || next.version() != expectedVersion + 1L
                || next.status() != CpfLogPolicyVersionSnapshot.Status.DRAFT) {
            throw new IllegalArgumentException("invalid versioned log policy mutation");
        }
        String commandIdHash = sha256(commandId);
        String targetHash = targetHash(next.targetType(), next.targetId());
        Instant now = clock.instant();
        final String finalCommandHash = commandHash;
        try {
            WriteResult result = access.transaction(() -> {
                access.lockShard(COMMAND_LEDGER_SHARD);
                access.lockShard(TARGET_CAPACITY_SHARD);
                access.lockShard(targetShard(targetHash));
                access.deleteCommandsBefore(now);

                Optional<CommandRow> previous = access.findCommand(commandIdHash);
                if (previous.isPresent()) {
                    CommandRow row = previous.get();
                    if (row.targetType() == next.targetType()
                            && row.targetHash().equals(targetHash)
                            && row.commandHash().equals(finalCommandHash)) {
                        CpfLogPolicyVersionSnapshot replay = access.findVersion(next.targetType(), targetHash,
                                next.targetId(), row.version()).orElse(next);
                        return new WriteResult(Status.IDEMPOTENT_REPLAY, replay);
                    }
                    return new WriteResult(Status.COMMAND_CONFLICT,
                            access.current(next.targetType(), targetHash, next.targetId()).orElse(null));
                }
                if (access.countCommands() >= maximumCommandRecords) {
                    return new WriteResult(Status.RESOURCE_EXHAUSTED,
                            access.current(next.targetType(), targetHash, next.targetId()).orElse(null));
                }
                Optional<CpfLogPolicyVersionSnapshot> found = access.current(
                        next.targetType(), targetHash, next.targetId());
                if (found.isEmpty()) return new WriteResult(Status.VERSION_CONFLICT, null);
                CpfLogPolicyVersionSnapshot current = found.get();
                if (current.version() != expectedVersion
                        || current.status() != CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
                    return new WriteResult(Status.VERSION_CONFLICT, current);
                }
                access.insertVersion(targetHash, next);
                if (access.compareAndSetHead(next.targetType(), targetHash,
                        expectedVersion, next.version(), now) != 1) {
                    throw new IllegalStateException("log policy head CAS failed after target lock");
                }
                access.insertCommand(commandIdHash, finalCommandHash, next.targetType(), targetHash,
                        next.version(), now, safePlus(now, commandTtl));
                trimHistory(next.targetType(), targetHash, next.targetId(), next.version());
                return new WriteResult(Status.APPLIED, next);
            });
            if (result.status() != Status.APPLIED && result.status() != Status.IDEMPOTENT_REPLAY) {
                rejected.incrementAndGet();
            }
            return result;
        } catch (CommitOutcomeUnknownException uncertain) {
            unknown.incrementAndGet();
            return new WriteResult(Status.UNKNOWN, next);
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public StatusResult updateStatus(LogPolicyTargetType type, String targetId, long expectedVersion,
            CpfLogPolicyVersionSnapshot.Status expectedStatus,
            CpfLogPolicyVersionSnapshot.Status nextStatus, String actor, String reason) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(expectedStatus, "expectedStatus");
        Objects.requireNonNull(nextStatus, "nextStatus");
        String normalized = LogPolicyDecision.normalizeTargetId(targetId);
        String targetHash = targetHash(type, normalized);
        String actorHash = sha256(identifier(actor, "actor"));
        try {
            return access.transaction(() -> {
                access.lockShard(targetShard(targetHash));
                Optional<CpfLogPolicyVersionSnapshot> found = access.current(type, targetHash, normalized);
                if (found.isEmpty()) return new StatusResult(false, null);
                CpfLogPolicyVersionSnapshot current = found.get();
                if (current.version() != expectedVersion || current.status() != expectedStatus) {
                    return new StatusResult(false, current);
                }
                CpfLogPolicyVersionSnapshot changed = new CpfLogPolicyVersionSnapshot(type, normalized,
                        expectedVersion, nextStatus, current.decision(), clock.instant(), actorHash, reason);
                int updated = access.updateStatus(type, targetHash, expectedVersion,
                        expectedStatus, changed, clock.instant());
                if (updated != 1) return new StatusResult(false,
                        access.current(type, targetHash, normalized).orElse(current));
                return new StatusResult(true, changed);
            });
        } catch (CommitOutcomeUnknownException uncertain) {
            unknown.incrementAndGet();
            return new StatusResult(false, access.transaction(
                    () -> access.current(type, targetHash, normalized)).orElse(null));
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public CpfLogPolicyVersionRuntimeStatus runtimeStatus() {
        try {
            long[] counts = access.transaction(() -> new long[] {
                    access.countTargets(), access.countVersions(), access.countCommands()});
            long targets = counts[0];
            long versions = counts[1];
            long commands = counts[2];
            boolean invalid = targets > maximumTargets || commands > maximumCommandRecords
                    || versions > targets * (long) maximumHistoryPerTarget;
            CpfLogPolicyVersionRuntimeStatus.Health health = invalid
                    ? CpfLogPolicyVersionRuntimeStatus.Health.DOWN
                    : providerFailures.get() > 0L || rejected.get() > 0L || unknown.get() > 0L
                            ? CpfLogPolicyVersionRuntimeStatus.Health.DEGRADED
                            : CpfLogPolicyVersionRuntimeStatus.Health.UP;
            return new CpfLogPolicyVersionRuntimeStatus(health,
                    boundedInt(targets, maximumTargets),
                    boundedInt(versions, (long) maximumTargets * maximumHistoryPerTarget),
                    boundedInt(commands, maximumCommandRecords), maximumTargets,
                    maximumHistoryPerTarget, maximumCommandRecords, rejected.get(), unknown.get(),
                    0L, 0L, clock.instant());
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            return new CpfLogPolicyVersionRuntimeStatus(CpfLogPolicyVersionRuntimeStatus.Health.DOWN,
                    0, 0, 0, maximumTargets, maximumHistoryPerTarget, maximumCommandRecords,
                    rejected.get(), unknown.get(), 0L, 0L, clock.instant());
        }
    }

    private void lock(String targetHash) {
        access.lockShard(TARGET_CAPACITY_SHARD);
        access.lockShard(targetShard(targetHash));
    }

    private void trimHistory(LogPolicyTargetType type, String targetHash, String targetId, long currentVersion) {
        long count = access.countTargetVersions(type, targetHash);
        int guard = maximumHistoryPerTarget + 1;
        while (count > maximumHistoryPerTarget && guard-- > 0) {
            Optional<Long> oldest = access.oldestDeletableVersion(type, targetHash, currentVersion);
            if (oldest.isEmpty() || access.deleteVersion(type, targetHash, oldest.get()) != 1) {
                throw new IllegalStateException("log policy version history cannot be bounded");
            }
            count--;
        }
        if (count > maximumHistoryPerTarget) {
            throw new IllegalStateException("log policy version history capacity remains exceeded");
        }
    }

    private static int targetShard(String targetHash) {
        return Integer.parseInt(targetHash.substring(0, 2), 16) % TARGET_SHARD_COUNT;
    }
    private static String targetHash(LogPolicyTargetType type, String targetId) {
        return sha256(type.code() + ':' + LogPolicyDecision.normalizeTargetId(targetId));
    }
    private static int boundedInt(long value, long maximum) {
        if (value < 0L || maximum < 0L) throw new IllegalStateException("negative store count");
        return (int) Math.min(Math.min(value, maximum), Integer.MAX_VALUE);
    }
    private static Instant safePlus(Instant instant, Duration duration) {
        try { return instant.plus(duration); } catch (RuntimeException overflow) { return Instant.MAX; }
    }
    private static Instant safeMinus(Instant instant, Duration duration) {
        try { return instant.minus(duration); } catch (RuntimeException overflow) { return Instant.MIN; }
    }
    private static String identifier(String value, String field) {
        if (value == null || !IDENTIFIER.matcher(value.trim()).matches()
                || value.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return value.trim();
    }
    private static String lowerHash(String value, String field) {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(field + " must be lowercase SHA-256");
        }
        return value;
    }
    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 unavailable", unavailable);
        }
    }

    static final class CommitOutcomeUnknownException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        CommitOutcomeUnknownException(String message, Throwable cause) { super(message, cause); }
    }

    interface Access {
        <T> T transaction(Supplier<T> callback);
        void verifySchema();
        void lockShard(int shardId);
        Optional<CpfLogPolicyVersionSnapshot> current(
                LogPolicyTargetType type, String targetHash, String targetId);
        Optional<CpfLogPolicyVersionSnapshot> findVersion(
                LogPolicyTargetType type, String targetHash, String targetId, long version);
        List<CpfLogPolicyVersionSnapshot> history(
                LogPolicyTargetType type, String targetHash, String targetId, int limit);
        Optional<CommandRow> findCommand(String commandIdHash);
        long countTargets();
        long countVersions();
        long countTargetVersions(LogPolicyTargetType type, String targetHash);
        long countCommands();
        int deleteCommandsBefore(Instant cutoff);
        void insertVersion(String targetHash, CpfLogPolicyVersionSnapshot snapshot);
        void insertHead(LogPolicyTargetType type, String targetHash, long version, Instant updatedAt);
        int compareAndSetHead(LogPolicyTargetType type, String targetHash,
                long expectedVersion, long nextVersion, Instant updatedAt);
        void insertCommand(String commandIdHash, String commandHash, LogPolicyTargetType type,
                String targetHash, long version, Instant recordedAt, Instant expiresAt);
        int updateStatus(LogPolicyTargetType type, String targetHash, long version,
                CpfLogPolicyVersionSnapshot.Status expectedStatus,
                CpfLogPolicyVersionSnapshot changed, Instant updatedAt);
        Optional<Long> oldestDeletableVersion(
                LogPolicyTargetType type, String targetHash, long currentVersion);
        int deleteVersion(LogPolicyTargetType type, String targetHash, long version);
    }

    record CommandRow(LogPolicyTargetType targetType, String targetHash,
            String commandHash, long version) {
        CommandRow {
            Objects.requireNonNull(targetType, "targetType");
            lowerHash(targetHash, "targetHash");
            lowerHash(commandHash, "commandHash");
            if (version < 1L) throw new IllegalArgumentException("version is invalid");
        }
    }

    private static final class DataSourceAccess implements Access {
        private final DataSource dataSource;
        private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

        private DataSourceAccess(DataSource dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        }

        @Override public <T> T transaction(Supplier<T> callback) {
            if (transactionConnection.get() != null) {
                throw new IllegalStateException("nested log policy transaction is forbidden");
            }
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                transactionConnection.set(connection);
                final T result;
                try { result = callback.get(); }
                catch (RuntimeException failure) {
                    rollback(connection, failure);
                    throw failure;
                }
                try { connection.commit(); }
                catch (SQLException uncertain) {
                    throw new CommitOutcomeUnknownException("log policy commit outcome is unknown", uncertain);
                }
                return result;
            } catch (SQLException failure) {
                throw new IllegalStateException("log policy transaction failed", failure);
            } finally {
                transactionConnection.remove();
                closeQuietly(connection);
            }
        }

        @Override public void verifySchema() {
            long shardCount = count("SELECT COUNT(*) FROM cpf_log_policy_version_shard WHERE shard_id BETWEEN 0 AND 257");
            if (shardCount != REQUIRED_SHARD_ROWS) {
                throw new IllegalStateException("cpf_log_policy_version_shard requires 258 seed rows");
            }
            count("SELECT COUNT(*) FROM cpf_log_policy_version_head WHERE 1=0");
            count("SELECT COUNT(*) FROM cpf_log_policy_version WHERE 1=0");
            count("SELECT COUNT(*) FROM cpf_log_policy_version_command WHERE 1=0");
        }

        @Override public void lockShard(int shardId) {
            queryOne("SELECT shard_id FROM cpf_log_policy_version_shard WHERE shard_id = ? FOR UPDATE",
                    statement -> statement.setInt(1, shardId), rs -> rs.getInt(1))
                    .orElseThrow(() -> new IllegalStateException(
                            "cpf_log_policy_version_shard seed row is missing: " + shardId));
        }

        @Override public Optional<CpfLogPolicyVersionSnapshot> current(
                LogPolicyTargetType type, String targetHash, String targetId) {
            return queryOne("SELECT v.policy_version,v.policy_status,v.schema_version,v.file_log_level,"
                            + "v.db_log_enabled_flag,v.db_log_level,v.query_capture_mode,"
                            + "v.request_header_capture_mode,v.response_header_capture_mode,"
                            + "v.request_body_capture_mode,v.response_body_capture_mode,"
                            + "v.error_stack_capture_mode,v.query_allowlist,v.header_allowlist,"
                            + "v.field_allowlist,v.max_query_bytes,v.max_header_bytes,"
                            + "v.max_request_body_bytes,v.max_response_body_bytes,v.max_stack_bytes,"
                            + "v.masking_policy_key,v.policy_checksum,v.resolved_source,v.override_id,"
                            + "v.policy_id,v.updated_at,v.updated_by_hash,v.update_reason "
                            + "FROM cpf_log_policy_version_head h JOIN cpf_log_policy_version v "
                            + "ON v.target_type=h.target_type AND v.target_hash=h.target_hash "
                            + "AND v.policy_version=h.current_version "
                            + "WHERE h.target_type=? AND h.target_hash=?",
                    statement -> { statement.setString(1, type.code()); statement.setString(2, targetHash); },
                    rs -> mapSnapshot(type, targetId, rs));
        }

        @Override public Optional<CpfLogPolicyVersionSnapshot> findVersion(
                LogPolicyTargetType type, String targetHash, String targetId, long version) {
            return queryOne(versionSelect() + " WHERE target_type=? AND target_hash=? AND policy_version=?",
                    statement -> { statement.setString(1, type.code()); statement.setString(2, targetHash);
                        statement.setLong(3, version); }, rs -> mapSnapshot(type, targetId, rs));
        }

        @Override public List<CpfLogPolicyVersionSnapshot> history(
                LogPolicyTargetType type, String targetHash, String targetId, int limit) {
            return queryMany(versionSelect() + " WHERE target_type=? AND target_hash=? ORDER BY policy_version DESC",
                    statement -> { statement.setString(1, type.code()); statement.setString(2, targetHash);
                        statement.setMaxRows(limit); }, rs -> mapSnapshot(type, targetId, rs));
        }

        @Override public Optional<CommandRow> findCommand(String commandIdHash) {
            return queryOne("SELECT target_type,target_hash,command_hash,policy_version "
                            + "FROM cpf_log_policy_version_command WHERE command_id_hash=?",
                    statement -> statement.setString(1, commandIdHash), rs -> new CommandRow(
                            LogPolicyTargetType.fromCode(rs.getString(1)), rs.getString(2),
                            rs.getString(3), rs.getLong(4)));
        }
        @Override public long countTargets() { return count("SELECT COUNT(*) FROM cpf_log_policy_version_head"); }
        @Override public long countVersions() { return count("SELECT COUNT(*) FROM cpf_log_policy_version"); }
        @Override public long countTargetVersions(LogPolicyTargetType type, String targetHash) {
            return queryOne("SELECT COUNT(*) FROM cpf_log_policy_version WHERE target_type=? AND target_hash=?",
                    statement -> { statement.setString(1, type.code()); statement.setString(2, targetHash); },
                    rs -> rs.getLong(1)).orElse(0L);
        }
        @Override public long countCommands() { return count("SELECT COUNT(*) FROM cpf_log_policy_version_command"); }
        @Override public int deleteCommandsBefore(Instant cutoff) {
            return update("DELETE FROM cpf_log_policy_version_command WHERE expires_at<=?",
                    statement -> statement.setTimestamp(1, Timestamp.from(cutoff)));
        }

        @Override public void insertVersion(String targetHash, CpfLogPolicyVersionSnapshot snapshot) {
            LogPolicyDecision d = snapshot.decision();
            int updated = update("INSERT INTO cpf_log_policy_version(target_type,target_hash,policy_version,"
                            + "policy_status,schema_version,file_log_level,db_log_enabled_flag,db_log_level,"
                            + "query_capture_mode,request_header_capture_mode,response_header_capture_mode,"
                            + "request_body_capture_mode,response_body_capture_mode,error_stack_capture_mode,"
                            + "query_allowlist,header_allowlist,field_allowlist,max_query_bytes,max_header_bytes,"
                            + "max_request_body_bytes,max_response_body_bytes,max_stack_bytes,masking_policy_key,"
                            + "policy_checksum,resolved_source,override_id,policy_id,updated_at,updated_by_hash,"
                            + "update_reason) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    ps -> bindVersion(ps, targetHash, snapshot, d));
            if (updated != 1) throw new IllegalStateException("log policy version insert affected " + updated + " rows");
        }

        @Override public void insertHead(LogPolicyTargetType type, String targetHash, long version, Instant updatedAt) {
            int rows = update("INSERT INTO cpf_log_policy_version_head(target_type,target_hash,current_version,updated_at) "
                            + "VALUES(?,?,?,?)", ps -> { ps.setString(1, type.code()); ps.setString(2, targetHash);
                                ps.setLong(3, version); ps.setTimestamp(4, Timestamp.from(updatedAt)); });
            if (rows != 1) throw new IllegalStateException("log policy head insert affected " + rows + " rows");
        }

        @Override public int compareAndSetHead(LogPolicyTargetType type, String targetHash,
                long expectedVersion, long nextVersion, Instant updatedAt) {
            return update("UPDATE cpf_log_policy_version_head SET current_version=?,updated_at=? "
                            + "WHERE target_type=? AND target_hash=? AND current_version=?",
                    ps -> { ps.setLong(1, nextVersion); ps.setTimestamp(2, Timestamp.from(updatedAt));
                        ps.setString(3, type.code()); ps.setString(4, targetHash); ps.setLong(5, expectedVersion); });
        }

        @Override public void insertCommand(String commandIdHash, String commandHash,
                LogPolicyTargetType type, String targetHash, long version,
                Instant recordedAt, Instant expiresAt) {
            int rows = update("INSERT INTO cpf_log_policy_version_command(command_id_hash,command_hash,"
                            + "target_type,target_hash,policy_version,recorded_at,expires_at) VALUES(?,?,?,?,?,?,?)",
                    ps -> { ps.setString(1, commandIdHash); ps.setString(2, commandHash);
                        ps.setString(3, type.code()); ps.setString(4, targetHash); ps.setLong(5, version);
                        ps.setTimestamp(6, Timestamp.from(recordedAt)); ps.setTimestamp(7, Timestamp.from(expiresAt)); });
            if (rows != 1) throw new IllegalStateException("log policy command insert affected " + rows + " rows");
        }

        @Override public int updateStatus(LogPolicyTargetType type, String targetHash, long version,
                CpfLogPolicyVersionSnapshot.Status expectedStatus,
                CpfLogPolicyVersionSnapshot changed, Instant updatedAt) {
            return update("UPDATE cpf_log_policy_version SET policy_status=?,updated_at=?,updated_by_hash=?,"
                            + "update_reason=? WHERE target_type=? AND target_hash=? AND policy_version=? "
                            + "AND policy_status=?",
                    ps -> { ps.setString(1, changed.status().name()); ps.setTimestamp(2, Timestamp.from(updatedAt));
                        ps.setString(3, changed.updatedBy()); ps.setString(4, changed.reason());
                        ps.setString(5, type.code()); ps.setString(6, targetHash); ps.setLong(7, version);
                        ps.setString(8, expectedStatus.name()); });
        }

        @Override public Optional<Long> oldestDeletableVersion(
                LogPolicyTargetType type, String targetHash, long currentVersion) {
            return queryOne("SELECT MIN(policy_version) FROM cpf_log_policy_version "
                            + "WHERE target_type=? AND target_hash=? AND policy_version<>?",
                    ps -> { ps.setString(1, type.code()); ps.setString(2, targetHash); ps.setLong(3, currentVersion); },
                    rs -> { long value = rs.getLong(1); return rs.wasNull() ? null : value; });
        }
        @Override public int deleteVersion(LogPolicyTargetType type, String targetHash, long version) {
            return update("DELETE FROM cpf_log_policy_version WHERE target_type=? AND target_hash=? AND policy_version=?",
                    ps -> { ps.setString(1, type.code()); ps.setString(2, targetHash); ps.setLong(3, version); });
        }

        private long count(String sql) {
            return queryOne(sql, ps -> { }, rs -> rs.getLong(1)).orElse(0L);
        }
        private <T> Optional<T> queryOne(String sql, Binder binder, Mapper<T> mapper) {
            List<T> rows = queryMany(sql, binder, mapper);
            return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
        }
        private <T> List<T> queryMany(String sql, Binder binder, Mapper<T> mapper) {
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                binder.bind(statement);
                try (ResultSet rs = statement.executeQuery()) {
                    List<T> rows = new ArrayList<>();
                    while (rs.next()) rows.add(mapper.map(rs));
                    return rows;
                }
            } catch (SQLException failure) {
                throw new IllegalStateException("log policy query failed", failure);
            }
        }
        private int update(String sql, Binder binder) {
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                binder.bind(statement);
                return statement.executeUpdate();
            } catch (SQLException failure) {
                throw new IllegalStateException("log policy update failed", failure);
            }
        }
        private Connection connection() {
            Connection connection = transactionConnection.get();
            if (connection == null) throw new IllegalStateException("log policy JDBC access requires a transaction");
            return connection;
        }
        private static String versionSelect() {
            return "SELECT policy_version,policy_status,schema_version,file_log_level,db_log_enabled_flag,"
                    + "db_log_level,query_capture_mode,request_header_capture_mode,response_header_capture_mode,"
                    + "request_body_capture_mode,response_body_capture_mode,error_stack_capture_mode,"
                    + "query_allowlist,header_allowlist,field_allowlist,max_query_bytes,max_header_bytes,"
                    + "max_request_body_bytes,max_response_body_bytes,max_stack_bytes,masking_policy_key,"
                    + "policy_checksum,resolved_source,override_id,policy_id,updated_at,updated_by_hash,update_reason "
                    + "FROM cpf_log_policy_version";
        }
        private static CpfLogPolicyVersionSnapshot mapSnapshot(
                LogPolicyTargetType type, String targetId, ResultSet rs) throws SQLException {
            LogPolicyDecision decision = new LogPolicyDecision(rs.getInt(3), type.code(), targetId,
                    rs.getString(4), "Y".equalsIgnoreCase(rs.getString(5)), rs.getString(6),
                    LogCaptureMode.valueOf(rs.getString(7)), LogCaptureMode.valueOf(rs.getString(8)),
                    LogCaptureMode.valueOf(rs.getString(9)), LogCaptureMode.valueOf(rs.getString(10)),
                    LogCaptureMode.valueOf(rs.getString(11)), LogCaptureMode.valueOf(rs.getString(12)),
                    LogPolicyDecision.parseCsv(rs.getString(13)), LogPolicyDecision.parseCsv(rs.getString(14)),
                    LogPolicyDecision.parseCsv(rs.getString(15)), rs.getInt(16), rs.getInt(17), rs.getInt(18),
                    rs.getInt(19), rs.getInt(20), rs.getString(21), rs.getString(22), rs.getString(23),
                    nullableLong(rs, 24), nullableLong(rs, 25));
            return new CpfLogPolicyVersionSnapshot(type, targetId, rs.getLong(1),
                    CpfLogPolicyVersionSnapshot.Status.valueOf(rs.getString(2)), decision,
                    rs.getTimestamp(26).toInstant(), rs.getString(27), rs.getString(28));
        }
        private static void bindVersion(PreparedStatement ps, String targetHash,
                CpfLogPolicyVersionSnapshot snapshot, LogPolicyDecision d) throws SQLException {
            int i = 1;
            ps.setString(i++, snapshot.targetType().code()); ps.setString(i++, targetHash);
            ps.setLong(i++, snapshot.version()); ps.setString(i++, snapshot.status().name());
            ps.setInt(i++, d.schemaVersion()); ps.setString(i++, d.fileLogLevel());
            ps.setString(i++, d.dbLogEnabled() ? "Y" : "N"); ps.setString(i++, d.dbLogLevel());
            ps.setString(i++, d.queryCaptureMode().name()); ps.setString(i++, d.requestHeaderCaptureMode().name());
            ps.setString(i++, d.responseHeaderCaptureMode().name()); ps.setString(i++, d.requestBodyCaptureMode().name());
            ps.setString(i++, d.responseBodyCaptureMode().name()); ps.setString(i++, d.errorStackCaptureMode().name());
            ps.setString(i++, LogPolicyDecision.toCsv(d.queryAllowlist()));
            ps.setString(i++, LogPolicyDecision.toCsv(d.headerAllowlist()));
            ps.setString(i++, LogPolicyDecision.toCsv(d.fieldAllowlist()));
            ps.setInt(i++, d.maxQueryBytes()); ps.setInt(i++, d.maxHeaderBytes());
            ps.setInt(i++, d.maxRequestBodyBytes()); ps.setInt(i++, d.maxResponseBodyBytes());
            ps.setInt(i++, d.maxStackBytes()); ps.setString(i++, d.maskingPolicyKey());
            ps.setString(i++, d.policyChecksum()); ps.setString(i++, d.resolvedSource());
            if (d.overrideId() == null) ps.setNull(i++, java.sql.Types.BIGINT); else ps.setLong(i++, d.overrideId());
            if (d.policyId() == null) ps.setNull(i++, java.sql.Types.BIGINT); else ps.setLong(i++, d.policyId());
            ps.setTimestamp(i++, Timestamp.from(snapshot.updatedAt()));
            ps.setString(i++, sha256(snapshot.updatedBy())); ps.setString(i, snapshot.reason());
        }
        private static Long nullableLong(ResultSet rs, int column) throws SQLException {
            long value = rs.getLong(column); return rs.wasNull() ? null : value;
        }
        private static void rollback(Connection connection, RuntimeException failure) {
            try { connection.rollback(); } catch (SQLException rollbackFailure) { failure.addSuppressed(rollbackFailure); }
        }
        private static void closeQuietly(Connection connection) {
            if (connection == null) return;
            try { connection.close(); } catch (SQLException ignored) { }
        }
        @FunctionalInterface private interface Binder { void bind(PreparedStatement statement) throws SQLException; }
        @FunctionalInterface private interface Mapper<T> { T map(ResultSet resultSet) throws SQLException; }
    }
}
