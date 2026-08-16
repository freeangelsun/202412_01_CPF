package com.cpf.integration.resilience.internal;

import com.cpf.platform.operations.api.state.CpfOperationState;
import com.cpf.platform.operations.api.state.CpfStateSearchRequest;
import com.cpf.platform.operations.api.state.CpfStateSnapshot;
import com.cpf.platform.operations.spi.state.CpfStateStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/** Multi-instance CAS and response-loss recovery gate without an external database. */
public final class JdbcCpfStateConcurrencyHarness {
    private JdbcCpfStateConcurrencyHarness() {}

    public static void main(String[] args) throws Exception {
        SharedAccess access = new SharedAccess();
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        JdbcCpfStateStore first = new JdbcCpfStateStore(
                access, 100, 1_000, Duration.ofDays(1), clock);
        JdbcCpfStateStore second = new JdbcCpfStateStore(
                access, 100, 1_000, Duration.ofDays(1), clock);
        CpfStateSnapshot start = snapshot("job:race", CpfOperationState.RUNNING, 0L, "op-start");
        require(first.compareAndSet("job:race", -1L, "op-start", hash('a'), start).status()
                == CpfStateStore.Status.APPLIED, "initial state must apply");

        access.gateNextTransactions(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<CpfStateStore.WriteResult> success = executor.submit(() -> {
                ready.countDown();
                go.await();
                return first.compareAndSet(
                        "job:race", 0L, "op-success", hash('b'),
                        snapshot("job:race", CpfOperationState.SUCCEEDED, 1L, "op-success"));
            });
            Future<CpfStateStore.WriteResult> failure = executor.submit(() -> {
                ready.countDown();
                go.await();
                return second.compareAndSet(
                        "job:race", 0L, "op-failure", hash('c'),
                        snapshot("job:race", CpfOperationState.FAILED, 1L, "op-failure"));
            });
            ready.await();
            go.countDown();
            List<CpfStateStore.Status> statuses = List.of(
                    success.get().status(), failure.get().status());
            require(statuses.stream().filter(status -> status == CpfStateStore.Status.APPLIED).count() == 1L,
                    "exactly one concurrent writer must apply");
            require(statuses.stream().filter(status -> status == CpfStateStore.Status.CONFLICT).count() == 1L,
                    "the stale concurrent writer must conflict");
        }
        CpfStateSnapshot current = first.find("job:race").orElseThrow();
        require(current.version() == 1L && current.state().terminal(),
                "concurrent writers must leave one terminal version");

        access.loseNextResponseAfterCommit();
        String recoveryOperation = "op-reconcile";
        CpfStateSnapshot reconciled = snapshot(
                "job:race", CpfOperationState.UNKNOWN, 2L, recoveryOperation);
        try {
            first.compareAndSet("job:race", 1L, recoveryOperation, hash('d'), reconciled);
            throw new AssertionError("simulated response loss must surface provider uncertainty");
        } catch (IllegalStateException expected) {
            // The transaction was committed but its response was lost.
        }
        CpfStateStore.WriteResult replay = second.compareAndSet(
                "job:race", 1L, recoveryOperation, hash('d'), reconciled);
        require(replay.status() == CpfStateStore.Status.IDEMPOTENT_REPLAY,
                "same operation must converge after commit response loss");
        require(replay.snapshot() != null && replay.snapshot().version() == 2L,
                "replay must return the committed snapshot");
        CpfStateStore.WriteResult hashConflict = second.compareAndSet(
                "job:race", 1L, recoveryOperation, hash('e'), reconciled);
        require(hashConflict.status() == CpfStateStore.Status.OPERATION_CONFLICT,
                "operation id reuse with a different command hash must fail closed");
        require(access.maximumSimultaneousTransactions() >= 2,
                "two provider instances must enter the shared access concurrently");
        require(access.heldLockCount() == 0, "all transaction locks must be released");

        System.out.println("CPF_JDBC_STATE_CONCURRENCY_HARNESS_PASS");
    }

    private static CpfStateSnapshot snapshot(
            String key, CpfOperationState state, long version, String operation) {
        return new CpfStateSnapshot(
                key, state, version, operation, "worker-a", "confirmed",
                Instant.parse("2026-08-05T00:00:00Z").plusSeconds(version));
    }

    private static String hash(char value) {
        return String.valueOf(value).repeat(64);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class SharedAccess implements JdbcCpfStateStore.Access {
        private final Map<String, CpfStateSnapshot> states = new LinkedHashMap<>();
        private final Map<String, Command> commands = new LinkedHashMap<>();
        private final ReentrantLock[] shardLocks = new ReentrantLock[JdbcCpfStateStore.REQUIRED_SHARD_ROWS];
        private final ThreadLocal<List<ReentrantLock>> held = ThreadLocal.withInitial(ArrayList::new);
        private final AtomicBoolean loseResponse = new AtomicBoolean();
        private volatile CountDownLatch entryReady;
        private volatile CountDownLatch entryGo;
        private int activeTransactions;
        private int maxActiveTransactions;

        private SharedAccess() {
            for (int index = 0; index < shardLocks.length; index++) {
                shardLocks[index] = new ReentrantLock();
            }
        }

        @Override
        public <T> T transaction(Supplier<T> callback) {
            synchronized (this) {
                activeTransactions++;
                maxActiveTransactions = Math.max(maxActiveTransactions, activeTransactions);
            }
            try {
                CountDownLatch ready = entryReady;
                CountDownLatch go = entryGo;
                if (ready != null && go != null) {
                    ready.countDown();
                    if (ready.getCount() == 0L) go.countDown();
                    try {
                        go.await();
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("transaction entry gate interrupted", interrupted);
                    }
                }
                T result = callback.get();
                if (loseResponse.compareAndSet(true, false)) {
                    throw new IllegalStateException("simulated commit response loss");
                }
                return result;
            } finally {
                List<ReentrantLock> acquired = held.get();
                for (int index = acquired.size() - 1; index >= 0; index--) acquired.get(index).unlock();
                acquired.clear();
                synchronized (this) {
                    activeTransactions--;
                }
            }
        }

        @Override
        public void lockShard(int shard) {
            ReentrantLock lock = shardLocks[shard];
            lock.lock();
            held.get().add(lock);
        }

        @Override
        public synchronized Optional<CpfStateSnapshot> findState(String stateKey) {
            return Optional.ofNullable(states.get(stateKey));
        }

        @Override
        public synchronized Optional<JdbcCpfStateStore.CommandRow> findCommand(
                String stateKey, String operationId) {
            Command command = commands.get(stateKey + '\n' + operationId);
            return command == null
                    ? Optional.empty()
                    : Optional.of(new JdbcCpfStateStore.CommandRow(
                            command.commandHash(), command.snapshot()));
        }

        @Override
        public synchronized long countStates() {
            return states.size();
        }

        @Override
        public synchronized long countCommands() {
            return commands.size();
        }

        @Override
        public synchronized int deleteCommandsBefore(Instant cutoff) {
            int before = commands.size();
            commands.entrySet().removeIf(entry -> entry.getValue().recordedAt().isBefore(cutoff));
            return before - commands.size();
        }

        @Override
        public synchronized void insertState(CpfStateSnapshot snapshot) {
            if (states.putIfAbsent(snapshot.stateKey(), snapshot) != null) {
                throw new IllegalStateException("duplicate state");
            }
        }

        @Override
        public synchronized void updateState(CpfStateSnapshot snapshot, long expectedVersion) {
            CpfStateSnapshot current = states.get(snapshot.stateKey());
            if (current == null || current.version() != expectedVersion) {
                throw new IllegalStateException("stale state writer");
            }
            states.put(snapshot.stateKey(), snapshot);
        }

        @Override
        public synchronized void insertCommand(
                String stateKey,
                String operationId,
                String commandHash,
                CpfStateSnapshot snapshot,
                Instant recordedAt) {
            String key = stateKey + '\n' + operationId;
            if (commands.putIfAbsent(key,
                    new Command(commandHash, snapshot, recordedAt)) != null) {
                throw new IllegalStateException("duplicate command");
            }
        }

        @Override
        public synchronized List<CpfStateSnapshot> search(
                CpfStateSearchRequest request, int maximumRows) {
            List<CpfStateSnapshot> rows = states.values().stream()
                    .filter(snapshot -> snapshot.stateKey().startsWith(request.stateKeyPrefix()))
                    .filter(snapshot -> request.states().contains(snapshot.state()))
                    .filter(snapshot -> request.afterStateKey() == null
                            || snapshot.stateKey().compareTo(request.afterStateKey()) > 0)
                    .sorted(Comparator.comparing(CpfStateSnapshot::stateKey))
                    .limit(maximumRows)
                    .toList();
            return List.copyOf(rows);
        }

        void gateNextTransactions(int count) {
            entryReady = new CountDownLatch(count);
            entryGo = new CountDownLatch(1);
        }

        void loseNextResponseAfterCommit() {
            entryReady = null;
            entryGo = null;
            loseResponse.set(true);
        }

        synchronized int maximumSimultaneousTransactions() {
            return maxActiveTransactions;
        }

        int heldLockCount() {
            int count = 0;
            for (ReentrantLock lock : shardLocks) if (lock.isLocked()) count++;
            return count;
        }

        private record Command(
                String commandHash, CpfStateSnapshot snapshot, Instant recordedAt) {}
    }
}
