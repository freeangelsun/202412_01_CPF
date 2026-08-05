package com.cpf.starter.security.secret.internal;

import com.cpf.core.api.security.CpfMaskingPolicyRuntimeStatus;
import com.cpf.core.api.security.CpfMaskingPolicySnapshot;
import com.cpf.core.spi.security.CpfMaskingPolicyStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Executable multi-instance contract for the JDBC masking-policy provider. */
public final class JdbcCpfMaskingPolicyStoreHarness {
    private JdbcCpfMaskingPolicyStoreHarness() { }

    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        FakeAccess access = new FakeAccess();
        CpfMaskingPolicySnapshot initial = snapshot(clock, 1L, Set.of("password", "token"), "system");
        JdbcCpfMaskingPolicyStore first = new JdbcCpfMaskingPolicyStore(
                access, initial, 3, 16, Duration.ofHours(1), clock);
        JdbcCpfMaskingPolicyStore second = new JdbcCpfMaskingPolicyStore(
                access, initial, 3, 16, Duration.ofHours(1), clock);

        require(access.initialVersionInsertCount == 1, "shared provider must initialize exactly once");
        require(access.lockedShards.stream().allMatch(value -> value == 0),
                "all mutations must lock the single control shard");

        CpfMaskingPolicySnapshot v2 = snapshot(clock, 2L, Set.of("password", "token", "secret"), "admin-a");
        CpfMaskingPolicyStore.WriteResult applied = first.compareAndSet(
                1L, "mask-command-0001", hash('a'), v2);
        require(applied.status() == CpfMaskingPolicyStore.Status.APPLIED,
                "first command must apply");
        require(access.lastCommandIdHash != null
                        && access.lastCommandIdHash.matches("[0-9a-f]{64}")
                        && !access.lastCommandIdHash.contains("mask-command"),
                "raw command id must never reach durable storage");

        CpfMaskingPolicyStore.WriteResult replay = second.compareAndSet(
                1L, "mask-command-0001", hash('a'), v2);
        require(replay.status() == CpfMaskingPolicyStore.Status.IDEMPOTENT_REPLAY
                        && replay.snapshot().version() == 2L,
                "same command must replay across provider instances");
        require(second.compareAndSet(1L, "mask-command-0001", hash('b'), v2).status()
                        == CpfMaskingPolicyStore.Status.COMMAND_CONFLICT,
                "command id reuse with another hash must fail");

        concurrency(first, second, clock);
        require(first.current().orElseThrow().version() == 3L,
                "one concurrent writer must advance the shared version");

        long version = 3L;
        for (int i = 0; i < 14; i++) {
            CpfMaskingPolicySnapshot next = snapshot(
                    clock, version + 1L, Set.of("password", "token", "field" + i), "admin-c");
            CpfMaskingPolicyStore.WriteResult result = first.compareAndSet(
                    version, "mask-fill-" + String.format("%04d", i), hash((char) ('d' + i)), next);
            require(result.status() == CpfMaskingPolicyStore.Status.APPLIED,
                    "bounded command fill must apply");
            version++;
        }
        CpfMaskingPolicySnapshot over = snapshot(
                clock, version + 1L, Set.of("password", "overflow"), "admin-d");
        require(first.compareAndSet(version, "mask-fill-9999", hash('z'), over).status()
                        == CpfMaskingPolicyStore.Status.RESOURCE_EXHAUSTED,
                "command capacity must fail closed");
        require(first.history(100).size() == 3,
                "durable history must remain bounded");

        clock.advance(Duration.ofHours(2));
        require(first.compareAndSet(version, "mask-after-ttl", hash('y'), over).status()
                        == CpfMaskingPolicyStore.Status.APPLIED,
                "expired command records must be pruned before capacity evaluation");
        require(access.commands.size() == 1,
                "expired durable command records must be physically removed");

        access.commitUnknown = true;
        CpfMaskingPolicySnapshot uncertain = snapshot(
                clock, version + 2L, Set.of("password", "uncertain"), "admin-e");
        CpfMaskingPolicyStore.WriteResult unknown = second.compareAndSet(
                version + 1L, "mask-unknown-0001", hash('x'), uncertain);
        require(unknown.status() == CpfMaskingPolicyStore.Status.UNKNOWN,
                "commit uncertainty must preserve typed UNKNOWN");
        access.commitUnknown = false;

        access.readFailure = true;
        CpfMaskingPolicyRuntimeStatus down = first.runtimeStatus();
        require(down.health() == CpfMaskingPolicyRuntimeStatus.Health.DOWN,
                "provider read failure must report DOWN without exposing policy data");
        access.readFailure = false;

        int calls = access.calls;
        try {
            first.compareAndSet(1L, "bad", hash('q'), v2);
            throw new AssertionError("invalid command id must fail before storage");
        } catch (IllegalArgumentException expected) {
            require(access.calls == calls, "invalid command must not reach database access");
        }

        System.out.println("CPF_JDBC_MASKING_POLICY_STORE_HARNESS_PASS");
    }

    private static void concurrency(
            JdbcCpfMaskingPolicyStore first,
            JdbcCpfMaskingPolicyStore second,
            Clock clock) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<CpfMaskingPolicyStore.WriteResult> left = pool.submit(() -> {
                start.await();
                return first.compareAndSet(2L, "mask-race-left", hash('l'),
                        snapshot(clock, 3L, Set.of("password", "left"), "admin-left"));
            });
            Future<CpfMaskingPolicyStore.WriteResult> right = pool.submit(() -> {
                start.await();
                return second.compareAndSet(2L, "mask-race-right", hash('r'),
                        snapshot(clock, 3L, Set.of("password", "right"), "admin-right"));
            });
            start.countDown();
            List<CpfMaskingPolicyStore.Status> statuses = List.of(
                    left.get(5, TimeUnit.SECONDS).status(),
                    right.get(5, TimeUnit.SECONDS).status());
            require(statuses.stream().filter(status -> status == CpfMaskingPolicyStore.Status.APPLIED).count() == 1L,
                    "exactly one concurrent provider must win");
            require(statuses.stream().filter(status -> status == CpfMaskingPolicyStore.Status.VERSION_CONFLICT).count() == 1L,
                    "losing concurrent provider must receive version conflict");
        }
    }

    private static CpfMaskingPolicySnapshot snapshot(
            Clock clock, long version, Set<String> keys, String actor) {
        return new CpfMaskingPolicySnapshot(
                version, keys, 4_096, true, clock.instant(), actor,
                "approved masking policy change");
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

    private static final class FakeAccess implements JdbcCpfMaskingPolicyStore.Access {
        private final Object monitor = new Object();
        private final TreeMap<Long, CpfMaskingPolicySnapshot> versions = new TreeMap<>();
        private final Map<String, Command> commands = new LinkedHashMap<>();
        private final List<Integer> lockedShards = new ArrayList<>();
        private Long head;
        private String lastCommandIdHash;
        private int initialVersionInsertCount;
        private int calls;
        private boolean readFailure;
        private boolean commitUnknown;

        @Override
        public <T> T transaction(Supplier<T> callback) {
            synchronized (monitor) {
                TreeMap<Long, CpfMaskingPolicySnapshot> versionBackup = new TreeMap<>(versions);
                Map<String, Command> commandBackup = new LinkedHashMap<>(commands);
                Long headBackup = head;
                String hashBackup = lastCommandIdHash;
                try {
                    T result = callback.get();
                    if (commitUnknown) {
                        throw new JdbcCpfMaskingPolicyStore.CommitOutcomeUnknownException(
                                "simulated uncertain commit", new IllegalStateException("connection lost"));
                    }
                    return result;
                } catch (JdbcCpfMaskingPolicyStore.CommitOutcomeUnknownException unknown) {
                    throw unknown;
                } catch (RuntimeException failure) {
                    versions.clear();
                    versions.putAll(versionBackup);
                    commands.clear();
                    commands.putAll(commandBackup);
                    head = headBackup;
                    lastCommandIdHash = hashBackup;
                    throw failure;
                }
            }
        }

        @Override public void lockControlShard(int shardId) {
            calls++;
            require(shardId == 0, "unexpected control shard");
            lockedShards.add(shardId);
        }

        @Override public Optional<CpfMaskingPolicySnapshot> current() {
            calls++;
            failRead();
            return head == null ? Optional.empty() : Optional.ofNullable(versions.get(head));
        }

        @Override public Optional<CpfMaskingPolicySnapshot> findVersion(long version) {
            calls++;
            failRead();
            return Optional.ofNullable(versions.get(version));
        }

        @Override public List<CpfMaskingPolicySnapshot> history(int limit) {
            calls++;
            failRead();
            return versions.values().stream()
                    .sorted(Comparator.comparingLong(CpfMaskingPolicySnapshot::version).reversed())
                    .limit(limit)
                    .toList();
        }

        @Override public Optional<JdbcCpfMaskingPolicyStore.CommandRow> findCommand(String commandIdHash) {
            calls++;
            Command command = commands.get(commandIdHash);
            return command == null ? Optional.empty() : Optional.of(
                    new JdbcCpfMaskingPolicyStore.CommandRow(command.commandHash(), command.snapshot()));
        }

        @Override public long countVersions() { calls++; failRead(); return versions.size(); }
        @Override public long countCommands() { calls++; failRead(); return commands.size(); }

        @Override public int deleteCommandsBefore(Instant cutoff) {
            calls++;
            int before = commands.size();
            commands.entrySet().removeIf(entry -> entry.getValue().recordedAt().isBefore(cutoff));
            return before - commands.size();
        }

        @Override public void insertVersion(CpfMaskingPolicySnapshot snapshot) {
            calls++;
            if (versions.putIfAbsent(snapshot.version(), snapshot) != null) {
                throw new IllegalStateException("duplicate version");
            }
            if (head == null) initialVersionInsertCount++;
        }

        @Override public void insertHead(long activeVersion) {
            calls++;
            if (head != null) throw new IllegalStateException("duplicate head");
            head = activeVersion;
        }

        @Override public int compareAndSetHead(long expectedVersion, long nextVersion) {
            calls++;
            if (head == null || head.longValue() != expectedVersion) return 0;
            head = nextVersion;
            return 1;
        }

        @Override public void insertCommand(
                String commandIdHash,
                String commandHash,
                CpfMaskingPolicySnapshot snapshot,
                Instant recordedAt) {
            calls++;
            require(commandIdHash.matches("[0-9a-f]{64}"), "command id must be hashed");
            lastCommandIdHash = commandIdHash;
            if (commands.putIfAbsent(commandIdHash,
                    new Command(commandHash, snapshot, recordedAt)) != null) {
                throw new IllegalStateException("duplicate command");
            }
        }

        @Override public Optional<Long> oldestDeletableVersion(long activeVersion) {
            calls++;
            return versions.keySet().stream().filter(value -> value.longValue() != activeVersion).findFirst();
        }

        @Override public int deleteVersion(long version) {
            calls++;
            return versions.remove(version) == null ? 0 : 1;
        }

        private void failRead() {
            if (readFailure) throw new IllegalStateException("database unavailable");
        }

        private record Command(
                String commandHash, CpfMaskingPolicySnapshot snapshot, Instant recordedAt) { }
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
