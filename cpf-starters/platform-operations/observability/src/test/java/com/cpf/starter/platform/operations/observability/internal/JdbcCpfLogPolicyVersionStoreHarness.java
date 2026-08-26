package com.cpf.starter.platform.operations.observability.internal;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionRuntimeStatus;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import com.cpf.platform.operations.observability.spi.logging.CpfLogPolicyVersionStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Executable shared-JDBC contract for versioned log-policy state. */
public final class JdbcCpfLogPolicyVersionStoreHarness {
    private JdbcCpfLogPolicyVersionStoreHarness() { }

    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        FakeAccess access = new FakeAccess();
        JdbcCpfLogPolicyVersionStore first = new JdbcCpfLogPolicyVersionStore(
                access, 8, 3, 16, Duration.ofHours(1), clock);
        JdbcCpfLogPolicyVersionStore second = new JdbcCpfLogPolicyVersionStore(
                access, 8, 3, 16, Duration.ofHours(1), clock);

        CpfLogPolicyVersionSnapshot baseline = snapshot(clock, 1L,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE, "baseline");
        require(first.ensureBaseline(baseline).version() == 1L, "baseline must be created");
        require(second.ensureBaseline(baseline).version() == 1L,
                "shared baseline creation must be idempotent");
        require(access.versionInsertCount == 1, "baseline must be inserted once");

        CpfLogPolicyVersionSnapshot v2 = snapshot(clock, 2L,
                CpfLogPolicyVersionSnapshot.Status.DRAFT, "update-v2");
        CpfLogPolicyVersionStore.WriteResult applied = first.compareAndSet(
                1L, "log-policy-command-0001", hash('a'), v2);
        require(applied.status() == CpfLogPolicyVersionStore.Status.APPLIED,
                "first mutation must apply");
        require(access.lastCommandIdHash != null
                        && access.lastCommandIdHash.matches("[0-9a-f]{64}")
                        && !access.lastCommandIdHash.contains("log-policy-command"),
                "raw command id must not reach durable storage");
        require(access.lastDeleteCutoff.equals(clock.instant()),
                "expiry cleanup must compare expires_at with current clock");

        CpfLogPolicyVersionStore.WriteResult replay = second.compareAndSet(
                1L, "log-policy-command-0001", hash('a'), v2);
        require(replay.status() == CpfLogPolicyVersionStore.Status.IDEMPOTENT_REPLAY,
                "same command must replay across instances");
        require(second.compareAndSet(1L, "log-policy-command-0001", hash('b'), v2).status()
                        == CpfLogPolicyVersionStore.Status.COMMAND_CONFLICT,
                "same command id with another hash must fail closed");

        CpfLogPolicyVersionStore.StatusResult promoted = first.updateStatus(
                LogPolicyTargetType.MODULE, "module-a", 2L,
                CpfLogPolicyVersionSnapshot.Status.DRAFT,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                "operator-0001", "approved promotion");
        require(promoted.updated() && promoted.snapshot().status()
                        == CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                "draft must promote atomically");
        require(promoted.snapshot().updatedBy().matches("[0-9a-f]{64}"),
                "durable actor identity must be hashed");

        concurrency(first, second, clock);
        require(first.current(LogPolicyTargetType.MODULE, "module-a").orElseThrow().version() == 3L,
                "one concurrent writer must advance the shared head");
        require(first.updateStatus(LogPolicyTargetType.MODULE, "module-a", 3L,
                CpfLogPolicyVersionSnapshot.Status.DRAFT,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                "operator-0002", "activate concurrent winner").updated(),
                "concurrent winning draft must promote before the next command");

        clock.advance(Duration.ofHours(2));
        CpfLogPolicyVersionSnapshot v4 = snapshot(clock, 4L,
                CpfLogPolicyVersionSnapshot.Status.DRAFT, "after-ttl");
        require(first.compareAndSet(3L, "log-policy-after-ttl", hash('t'), v4).status()
                        == CpfLogPolicyVersionStore.Status.APPLIED,
                "expired command records must be pruned before capacity evaluation");
        require(access.commands.size() == 1,
                "expired command rows must be physically removed using expires_at");
        require(first.updateStatus(LogPolicyTargetType.MODULE, "module-a", 4L,
                CpfLogPolicyVersionSnapshot.Status.DRAFT,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                "operator-0003", "activate ttl version").updated(),
                "ttl test draft must promote before UNKNOWN simulation");

        access.commitUnknownOnce = true;
        CpfLogPolicyVersionSnapshot v5 = snapshot(clock, 5L,
                CpfLogPolicyVersionSnapshot.Status.DRAFT, "uncertain");
        require(second.compareAndSet(4L, "log-policy-unknown-01", hash('u'), v5).status()
                        == CpfLogPolicyVersionStore.Status.UNKNOWN,
                "lost commit response must preserve typed UNKNOWN");
        require(second.current(LogPolicyTargetType.MODULE, "module-a").orElseThrow().version() == 5L,
                "UNKNOWN result must remain reconcilable from durable state");

        require(first.history(LogPolicyTargetType.MODULE, "module-a", 100).size() <= 3,
                "history must remain bounded");
        CpfLogPolicyVersionRuntimeStatus degraded = second.runtimeStatus();
        require(degraded.health() == CpfLogPolicyVersionRuntimeStatus.Health.DEGRADED
                        && degraded.unknownResultCount() >= 1L,
                "UNKNOWN outcome must be visible in runtime status");
        require(access.outsideTransactionReadCount == 0,
                "all JDBC reads and runtime queries must execute inside a transaction");

        access.readFailure = true;
        require(first.runtimeStatus().health() == CpfLogPolicyVersionRuntimeStatus.Health.DOWN,
                "provider read failure must report DOWN without leaking policy data");
        access.readFailure = false;

        int calls = access.calls;
        try {
            first.compareAndSet(1L, "bad", hash('x'), v2);
            throw new AssertionError("invalid command id must fail before database access");
        } catch (IllegalArgumentException expected) {
            require(access.calls == calls, "invalid command must not reach database access");
        }

        System.out.println("CPF_JDBC_LOG_POLICY_VERSION_STORE_HARNESS_PASS");
    }

    private static void concurrency(JdbcCpfLogPolicyVersionStore first,
            JdbcCpfLogPolicyVersionStore second, Clock clock) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<CpfLogPolicyVersionStore.WriteResult> left = pool.submit(() -> {
                start.await();
                return first.compareAndSet(2L, "log-policy-race-left", hash('l'),
                        snapshot(clock, 3L, CpfLogPolicyVersionSnapshot.Status.DRAFT, "left"));
            });
            Future<CpfLogPolicyVersionStore.WriteResult> right = pool.submit(() -> {
                start.await();
                return second.compareAndSet(2L, "log-policy-race-right", hash('r'),
                        snapshot(clock, 3L, CpfLogPolicyVersionSnapshot.Status.DRAFT, "right"));
            });
            start.countDown();
            List<CpfLogPolicyVersionStore.Status> statuses = List.of(
                    left.get(5, TimeUnit.SECONDS).status(), right.get(5, TimeUnit.SECONDS).status());
            require(statuses.stream().filter(s -> s == CpfLogPolicyVersionStore.Status.APPLIED).count() == 1L,
                    "exactly one concurrent provider must win");
            require(statuses.stream().filter(s -> s == CpfLogPolicyVersionStore.Status.VERSION_CONFLICT).count() == 1L,
                    "losing provider must observe version conflict");
        }
    }

    private static CpfLogPolicyVersionSnapshot snapshot(Clock clock, long version,
            CpfLogPolicyVersionSnapshot.Status status, String source) {
        LogPolicyDecision decision = LogPolicyDecision.cpfDefault(LogPolicyTargetType.MODULE, "module-a")
                .withSource(source);
        return new CpfLogPolicyVersionSnapshot(LogPolicyTargetType.MODULE, "module-a", version,
                status, decision, clock.instant(), "operator-0001", "approved log policy change");
    }

    private static String hash(char value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 unavailable", unavailable);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FakeAccess implements JdbcCpfLogPolicyVersionStore.Access {
        private final Object monitor = new Object();
        private final Map<String, TreeMap<Long, CpfLogPolicyVersionSnapshot>> versions = new HashMap<>();
        private final Map<String, Long> heads = new HashMap<>();
        private final Map<String, Command> commands = new LinkedHashMap<>();
        private final ThreadLocal<Boolean> transaction = ThreadLocal.withInitial(() -> false);
        private int versionInsertCount;
        private int calls;
        private int outsideTransactionReadCount;
        private String lastCommandIdHash;
        private Instant lastDeleteCutoff;
        private boolean commitUnknownOnce;
        private boolean readFailure;

        @Override public <T> T transaction(Supplier<T> callback) {
            synchronized (monitor) {
                require(!transaction.get(), "nested fake transaction");
                Map<String, TreeMap<Long, CpfLogPolicyVersionSnapshot>> versionBackup = copyVersions();
                Map<String, Long> headBackup = new HashMap<>(heads);
                Map<String, Command> commandBackup = new LinkedHashMap<>(commands);
                transaction.set(true);
                try {
                    T result = callback.get();
                    if (commitUnknownOnce) {
                        commitUnknownOnce = false;
                        throw new JdbcCpfLogPolicyVersionStore.CommitOutcomeUnknownException(
                                "simulated uncertain commit", new IllegalStateException("connection lost"));
                    }
                    return result;
                } catch (JdbcCpfLogPolicyVersionStore.CommitOutcomeUnknownException unknown) {
                    throw unknown;
                } catch (RuntimeException failure) {
                    versions.clear(); versions.putAll(versionBackup);
                    heads.clear(); heads.putAll(headBackup);
                    commands.clear(); commands.putAll(commandBackup);
                    throw failure;
                } finally {
                    transaction.remove();
                }
            }
        }

        @Override public void verifySchema() {
            write();
        }

        @Override public void lockShard(int shardId) {
            calls++;
            require(transaction.get(), "lock requires transaction");
            require(shardId >= 0 && shardId < JdbcCpfLogPolicyVersionStore.REQUIRED_SHARD_ROWS,
                    "unexpected shard id");
        }

        @Override public Optional<CpfLogPolicyVersionSnapshot> current(
                LogPolicyTargetType type, String targetHash, String targetId) {
            read();
            Long head = heads.get(key(type, targetHash));
            return head == null ? Optional.empty()
                    : Optional.ofNullable(versions.getOrDefault(key(type, targetHash), new TreeMap<>()).get(head));
        }

        @Override public Optional<CpfLogPolicyVersionSnapshot> findVersion(
                LogPolicyTargetType type, String targetHash, String targetId, long version) {
            read();
            return Optional.ofNullable(versions.getOrDefault(key(type, targetHash), new TreeMap<>()).get(version));
        }

        @Override public List<CpfLogPolicyVersionSnapshot> history(
                LogPolicyTargetType type, String targetHash, String targetId, int limit) {
            read();
            return versions.getOrDefault(key(type, targetHash), new TreeMap<>()).values().stream()
                    .sorted(Comparator.comparingLong(value -> value.version()).reversed())
                    .limit(limit).toList();
        }

        @Override public Optional<JdbcCpfLogPolicyVersionStore.CommandRow> findCommand(String commandIdHash) {
            read();
            Command command = commands.get(commandIdHash);
            return command == null ? Optional.empty() : Optional.of(new JdbcCpfLogPolicyVersionStore.CommandRow(
                    command.type(), command.targetHash(), command.commandHash(), command.version()));
        }

        @Override public long countTargets() { read(); return heads.size(); }
        @Override public long countVersions() { read(); return versions.values().stream().mapToLong(value -> value.size()).sum(); }
        @Override public long countTargetVersions(LogPolicyTargetType type, String targetHash) {
            read(); return versions.getOrDefault(key(type, targetHash), new TreeMap<>()).size();
        }
        @Override public long countCommands() { read(); return commands.size(); }

        @Override public int deleteCommandsBefore(Instant cutoff) {
            write();
            lastDeleteCutoff = cutoff;
            int before = commands.size();
            commands.entrySet().removeIf(e -> !e.getValue().expiresAt().isAfter(cutoff));
            return before - commands.size();
        }

        @Override public void insertVersion(String targetHash, CpfLogPolicyVersionSnapshot snapshot) {
            write();
            TreeMap<Long, CpfLogPolicyVersionSnapshot> values = versions.computeIfAbsent(
                    key(snapshot.targetType(), targetHash), ignored -> new TreeMap<>());
            if (values.putIfAbsent(snapshot.version(), snapshot) != null) {
                throw new IllegalStateException("duplicate version");
            }
            versionInsertCount++;
        }

        @Override public void insertHead(LogPolicyTargetType type, String targetHash,
                long version, Instant updatedAt) {
            write();
            if (heads.putIfAbsent(key(type, targetHash), version) != null) {
                throw new IllegalStateException("duplicate head");
            }
        }

        @Override public int compareAndSetHead(LogPolicyTargetType type, String targetHash,
                long expectedVersion, long nextVersion, Instant updatedAt) {
            write();
            String key = key(type, targetHash);
            Long current = heads.get(key);
            if (current == null || current.longValue() != expectedVersion) return 0;
            heads.put(key, nextVersion);
            return 1;
        }

        @Override public void insertCommand(String commandIdHash, String commandHash,
                LogPolicyTargetType type, String targetHash, long version,
                Instant recordedAt, Instant expiresAt) {
            write();
            require(commandIdHash.matches("[0-9a-f]{64}"), "command id must be hashed");
            lastCommandIdHash = commandIdHash;
            if (commands.putIfAbsent(commandIdHash,
                    new Command(commandHash, type, targetHash, version, expiresAt)) != null) {
                throw new IllegalStateException("duplicate command");
            }
        }

        @Override public int updateStatus(LogPolicyTargetType type, String targetHash, long version,
                CpfLogPolicyVersionSnapshot.Status expectedStatus,
                CpfLogPolicyVersionSnapshot changed, Instant updatedAt) {
            write();
            TreeMap<Long, CpfLogPolicyVersionSnapshot> values = versions.get(key(type, targetHash));
            if (values == null) return 0;
            TreeMap<Long, CpfLogPolicyVersionSnapshot> requiredValues = Objects.requireNonNull(values);
            CpfLogPolicyVersionSnapshot current = requiredValues.get(version);
            if (current == null || current.status() != expectedStatus) return 0;
            requiredValues.put(version, changed);
            return 1;
        }

        @Override public Optional<Long> oldestDeletableVersion(
                LogPolicyTargetType type, String targetHash, long currentVersion) {
            read();
            return versions.getOrDefault(key(type, targetHash), new TreeMap<>()).keySet().stream()
                    .filter(value -> value.longValue() != currentVersion).findFirst();
        }

        @Override public int deleteVersion(LogPolicyTargetType type, String targetHash, long version) {
            write();
            TreeMap<Long, CpfLogPolicyVersionSnapshot> values = versions.get(key(type, targetHash));
            return values == null || values.remove(version) == null ? 0 : 1;
        }

        private void read() {
            calls++;
            if (!transaction.get()) outsideTransactionReadCount++;
            require(transaction.get(), "JDBC read requires transaction");
            if (readFailure) throw new IllegalStateException("database unavailable");
        }
        private void write() {
            calls++;
            require(transaction.get(), "JDBC write requires transaction");
        }
        private Map<String, TreeMap<Long, CpfLogPolicyVersionSnapshot>> copyVersions() {
            Map<String, TreeMap<Long, CpfLogPolicyVersionSnapshot>> copy = new HashMap<>();
            versions.forEach((key, value) -> copy.put(key, new TreeMap<>(value)));
            return copy;
        }
        private static String key(LogPolicyTargetType type, String hash) { return type.code() + ':' + hash; }
        private record Command(String commandHash, LogPolicyTargetType type,
                String targetHash, long version, Instant expiresAt) { }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private MutableClock(Instant instant) { this.instant = instant; }
        void advance(Duration duration) { instant = instant.plus(duration); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
