package com.cpf.integration.resilience.internal;

import com.cpf.platform.operations.api.state.CpfOperationState;
import com.cpf.platform.operations.api.state.CpfStateRuntimeStatus;
import com.cpf.platform.operations.api.state.CpfStateSearchRequest;
import com.cpf.platform.operations.api.state.CpfStateSnapshot;
import com.cpf.platform.operations.spi.state.CpfStateStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class JdbcCpfStateStoreHarness {
    private JdbcCpfStateStoreHarness() {}

    public static void main(String[] args) {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        FakeAccess access = new FakeAccess();
        JdbcCpfStateStore store = new JdbcCpfStateStore(
                access, 2, 3, Duration.ofHours(1), clock);
        CpfStateSnapshot start = snapshot(clock, "job:1", CpfOperationState.RUNNING, 0L, "op-start");

        CpfStateStore.WriteResult applied = store.compareAndSet(
                "job:1", -1L, "op-start", hash('a'), start);
        require(applied.status() == CpfStateStore.Status.APPLIED, "initial state must apply");
        require(access.transactions.get() == 1, "state mutation must use one transaction");
        require(access.lockedShards.equals(List.of(256, 257, shard("job:1"))),
                "state mutation must use fixed capacity-command-key lock order");

        CpfStateStore.WriteResult replay = store.compareAndSet(
                "job:1", -1L, "op-start", hash('a'), start);
        require(replay.status() == CpfStateStore.Status.IDEMPOTENT_REPLAY,
                "same command must replay");
        CpfStateStore.WriteResult operationConflict = store.compareAndSet(
                "job:1", -1L, "op-start", hash('b'), start);
        require(operationConflict.status() == CpfStateStore.Status.OPERATION_CONFLICT,
                "operation id reuse with another hash must fail");

        CpfStateSnapshot second = snapshot(clock, "job:2", CpfOperationState.RUNNING, 0L, "op-2");
        require(store.compareAndSet("job:2", -1L, "op-2", hash('c'), second).status()
                == CpfStateStore.Status.APPLIED, "second state must apply");
        CpfStateSnapshot overState = snapshot(clock, "job:3", CpfOperationState.RUNNING, 0L, "op-3");
        require(store.compareAndSet("job:3", -1L, "op-3", hash('d'), overState).status()
                == CpfStateStore.Status.RESOURCE_EXHAUSTED,
                "new state must fail closed at durable state capacity");

        CpfStateSnapshot success = snapshot(clock, "job:1", CpfOperationState.SUCCEEDED, 1L, "op-success");
        require(store.compareAndSet("job:1", 0L, "op-success", hash('e'), success).status()
                == CpfStateStore.Status.APPLIED, "existing state update must apply at state capacity");
        CpfStateSnapshot overCommand = snapshot(clock, "job:2", CpfOperationState.FAILED, 1L, "op-full");
        require(store.compareAndSet("job:2", 0L, "op-full", hash('f'), overCommand).status()
                == CpfStateStore.Status.RESOURCE_EXHAUSTED,
                "mutation must fail closed at command-ledger capacity");

        clock.advance(Duration.ofHours(2));
        require(store.compareAndSet("job:2", 0L, "op-full", hash('f'), overCommand).status()
                == CpfStateStore.Status.APPLIED,
                "expired command rows must be pruned before capacity evaluation");
        require(access.commands.size() == 1, "expired command rows must be physically removed");

        CpfStateStore.SearchResult page = store.search(new CpfStateSearchRequest(
                "job:", Set.of(CpfOperationState.SUCCEEDED, CpfOperationState.FAILED), null, 1));
        require(page.status() == CpfStateStore.SearchStatus.SUCCESS
                        && page.items().size() == 1 && page.nextCursor() != null,
                "search must be bounded and cursor based");

        int calls = access.calls.get();
        try {
            store.find("../invalid");
            throw new AssertionError("invalid state key must be rejected");
        } catch (IllegalArgumentException expected) {
            require(access.calls.get() == calls, "invalid key must not reach database access");
        }

        access.searchFailure = true;
        require(store.search(CpfStateSearchRequest.firstPage(10)).status()
                == CpfStateStore.SearchStatus.UNKNOWN,
                "search provider failure must preserve UNKNOWN");
        access.searchFailure = false;

        CpfStateRuntimeStatus.RuntimeSnapshot runtime = store.stateRuntimeSnapshot();
        require(runtime.stateCount() == 2 && runtime.maximumStates() == 2,
                "runtime status must expose durable capacity");
        require(runtime.appliedCount() == 4L
                        && runtime.replayCount() == 1L
                        && runtime.operationConflictCount() == 1L
                        && runtime.resourceExhaustedCount() == 2L,
                "runtime counters must preserve result classes");
        require(runtime.providerFailureCount() >= 1L
                        && runtime.health() == CpfStateRuntimeStatus.Health.DEGRADED,
                "provider failure must degrade runtime health");

        System.out.println("CPF_JDBC_STATE_STORE_HARNESS_PASS");
    }

    private static int shard(String key) {
        return Math.floorMod(key.hashCode(), JdbcCpfStateStore.KEY_SHARD_COUNT);
    }

    private static CpfStateSnapshot snapshot(
            Clock clock, String key, CpfOperationState state, long version, String operationId) {
        return new CpfStateSnapshot(
                key, state, version, operationId, "worker-a", "confirmed", clock.instant());
    }

    private static String hash(char value) {
        return String.valueOf(value).repeat(64);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FakeAccess implements JdbcCpfStateStore.Access {
        private final Map<String, CpfStateSnapshot> states = new LinkedHashMap<>();
        private final Map<String, Command> commands = new LinkedHashMap<>();
        private final AtomicInteger transactions = new AtomicInteger();
        private final AtomicInteger calls = new AtomicInteger();
        private final List<Integer> lockedShards = new ArrayList<>();
        private boolean searchFailure;

        @Override
        public <T> T transaction(Supplier<T> callback) {
            transactions.incrementAndGet();
            lockedShards.clear();
            return callback.get();
        }

        @Override
        public void lockShard(int shard) {
            require(shard >= 0 && shard < JdbcCpfStateStore.REQUIRED_SHARD_ROWS,
                    "invalid shard");
            lockedShards.add(shard);
        }

        @Override
        public Optional<CpfStateSnapshot> findState(String stateKey) {
            calls.incrementAndGet();
            return Optional.ofNullable(states.get(stateKey));
        }

        @Override
        public Optional<JdbcCpfStateStore.CommandRow> findCommand(
                String stateKey, String operationId) {
            calls.incrementAndGet();
            Command command = commands.get(stateKey + '\n' + operationId);
            return command == null
                    ? Optional.empty()
                    : Optional.of(new JdbcCpfStateStore.CommandRow(
                            command.commandHash(), command.snapshot()));
        }

        @Override
        public long countStates() {
            calls.incrementAndGet();
            return states.size();
        }

        @Override
        public long countCommands() {
            calls.incrementAndGet();
            return commands.size();
        }

        @Override
        public int deleteCommandsBefore(Instant cutoff) {
            calls.incrementAndGet();
            int before = commands.size();
            commands.entrySet().removeIf(entry -> entry.getValue().recordedAt().isBefore(cutoff));
            return before - commands.size();
        }

        @Override
        public void insertState(CpfStateSnapshot snapshot) {
            calls.incrementAndGet();
            if (states.putIfAbsent(snapshot.stateKey(), snapshot) != null) {
                throw new IllegalStateException("duplicate state");
            }
        }

        @Override
        public void updateState(CpfStateSnapshot snapshot, long expectedVersion) {
            calls.incrementAndGet();
            CpfStateSnapshot current = states.get(snapshot.stateKey());
            if (current == null || current.version() != expectedVersion) {
                throw new IllegalStateException("stale state writer");
            }
            states.put(snapshot.stateKey(), snapshot);
        }

        @Override
        public void insertCommand(
                String stateKey,
                String operationId,
                String commandHash,
                CpfStateSnapshot snapshot,
                Instant recordedAt) {
            calls.incrementAndGet();
            String key = stateKey + '\n' + operationId;
            if (commands.putIfAbsent(key,
                    new Command(commandHash, snapshot, recordedAt)) != null) {
                throw new IllegalStateException("duplicate command");
            }
        }

        @Override
        public List<CpfStateSnapshot> search(CpfStateSearchRequest request, int maximumRows) {
            calls.incrementAndGet();
            if (searchFailure) throw new IllegalStateException("database unavailable");
            List<CpfStateSnapshot> rows = new ArrayList<>();
            for (CpfStateSnapshot snapshot : states.values()) {
                if (!snapshot.stateKey().startsWith(request.stateKeyPrefix())) continue;
                if (!request.states().contains(snapshot.state())) continue;
                if (request.afterStateKey() != null
                        && snapshot.stateKey().compareTo(request.afterStateKey()) <= 0) continue;
                rows.add(snapshot);
            }
            rows.sort(Comparator.comparing(value -> value.stateKey()));
            return List.copyOf(rows.subList(0, Math.min(rows.size(), maximumRows)));
        }

        private record Command(
                String commandHash, CpfStateSnapshot snapshot, Instant recordedAt) {}
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
