package com.cpf.core.internal.state;

import com.cpf.core.api.state.CpfStateIdentifiers;
import com.cpf.core.api.state.CpfStateRuntimeStatus;
import com.cpf.core.api.state.CpfStateSearchRequest;
import com.cpf.core.api.state.CpfStateSnapshot;
import com.cpf.core.spi.state.CpfStateStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded single-JVM provider. Distributed deployments must supply a shared durable store. */
public final class InMemoryCpfStateStore implements CpfStateStore, CpfStateRuntimeStatus {
    private static final int DEFAULT_MAXIMUM_STATES = 50_000;
    private static final int DEFAULT_MAXIMUM_OPERATIONS_PER_STATE = 64;
    private static final Duration DEFAULT_COMMAND_TTL = Duration.ofHours(24);

    private final Map<String, CpfStateSnapshot> states = new LinkedHashMap<>();
    private final Map<CommandKey, CommandRecord> commands = new LinkedHashMap<>();
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

    public InMemoryCpfStateStore() {
        this(DEFAULT_MAXIMUM_STATES, DEFAULT_MAXIMUM_OPERATIONS_PER_STATE,
                DEFAULT_COMMAND_TTL, Clock.systemUTC());
    }

    public InMemoryCpfStateStore(int maximumStates, Clock clock) {
        this(maximumStates, DEFAULT_MAXIMUM_OPERATIONS_PER_STATE, DEFAULT_COMMAND_TTL, clock);
    }

    public InMemoryCpfStateStore(
            int maximumStates,
            int maximumOperationsPerState,
            Duration commandTtl,
            Clock clock) {
        if (maximumStates < 1 || maximumStates > 1_000_000) {
            throw new IllegalArgumentException("maximumStates must be between 1 and 1000000");
        }
        if (maximumOperationsPerState < 1 || maximumOperationsPerState > 10_000) {
            throw new IllegalArgumentException(
                    "maximumOperationsPerState must be between 1 and 10000");
        }
        long commandsLimit = Math.multiplyExact((long) maximumStates, maximumOperationsPerState);
        if (commandsLimit > 10_000_000L) {
            throw new IllegalArgumentException("derived maximumCommands exceeds 10000000");
        }
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("commandTtl must be positive and <= 365 days");
        }
        this.maximumStates = maximumStates;
        this.maximumCommands = (int) commandsLimit;
        this.commandTtl = commandTtl;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public synchronized Optional<CpfStateSnapshot> find(String stateKey) {
        return Optional.ofNullable(states.get(CpfStateIdentifiers.stateKey(stateKey)));
    }

    @Override
    public synchronized WriteResult compareAndSet(
            String stateKey,
            long expectedVersion,
            String operationId,
            String commandHash,
            CpfStateSnapshot next) {
        try {
            String key = CpfStateIdentifiers.stateKey(stateKey);
            String operation = CpfStateIdentifiers.operationId(operationId);
            validateCommand(key, expectedVersion, operation, commandHash, next);
            pruneExpiredCommands(safeMinus(clock.instant(), commandTtl));

            CommandKey commandKey = new CommandKey(key, operation);
            CommandRecord previous = commands.get(commandKey);
            if (previous != null) {
                if (previous.commandHash().equals(commandHash)) {
                    replays.incrementAndGet();
                    return new WriteResult(Status.IDEMPOTENT_REPLAY, previous.snapshot());
                }
                operationConflicts.incrementAndGet();
                return new WriteResult(Status.OPERATION_CONFLICT, states.get(key));
            }

            CpfStateSnapshot current = states.get(key);
            boolean matches = expectedVersion < 0L
                    ? current == null
                    : current != null && current.version() == expectedVersion;
            if (!matches) {
                versionConflicts.incrementAndGet();
                return new WriteResult(Status.CONFLICT, current);
            }
            if (current == null && states.size() >= maximumStates) {
                resourceExhausted.incrementAndGet();
                return new WriteResult(Status.RESOURCE_EXHAUSTED, null);
            }
            if (commands.size() >= maximumCommands) {
                resourceExhausted.incrementAndGet();
                return new WriteResult(Status.RESOURCE_EXHAUSTED, current);
            }

            states.put(key, next);
            commands.put(commandKey, new CommandRecord(commandHash, next, clock.instant()));
            applied.incrementAndGet();
            return new WriteResult(Status.APPLIED, next);
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public synchronized SearchResult search(CpfStateSearchRequest request) {
        try {
            if (request == null) throw new IllegalArgumentException("request is required");
            List<CpfStateSnapshot> matching = new ArrayList<>();
            for (CpfStateSnapshot snapshot : states.values()) {
                if (!snapshot.stateKey().startsWith(request.stateKeyPrefix())) continue;
                if (!request.states().contains(snapshot.state())) continue;
                if (request.afterStateKey() != null
                        && snapshot.stateKey().compareTo(request.afterStateKey()) <= 0) continue;
                matching.add(snapshot);
            }
            matching.sort(Comparator.comparing(CpfStateSnapshot::stateKey));
            boolean more = matching.size() > request.pageSize();
            List<CpfStateSnapshot> page = List.copyOf(
                    matching.subList(0, Math.min(matching.size(), request.pageSize())));
            String nextCursor = more && !page.isEmpty() ? page.getLast().stateKey() : null;
            return new SearchResult(SearchStatus.SUCCESS, page, nextCursor);
        } catch (RuntimeException failure) {
            providerFailures.incrementAndGet();
            throw failure;
        }
    }

    @Override
    public synchronized RuntimeSnapshot stateRuntimeSnapshot() {
        Health health = providerFailures.get() > 0L || resourceExhausted.get() > 0L
                ? Health.DEGRADED : Health.UP;
        return new RuntimeSnapshot(
                health,
                states.size(),
                maximumStates,
                applied.get(),
                replays.get(),
                versionConflicts.get(),
                operationConflicts.get(),
                resourceExhausted.get(),
                providerFailures.get(),
                clock.instant());
    }

    private void pruneExpiredCommands(Instant cutoff) {
        Iterator<Map.Entry<CommandKey, CommandRecord>> iterator = commands.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().recordedAt().isBefore(cutoff)) iterator.remove();
        }
    }

    private static Instant safeMinus(Instant instant, Duration duration) {
        try {
            return instant.minus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MIN;
        }
    }

    private static void validateCommand(
            String stateKey,
            long expectedVersion,
            String operationId,
            String commandHash,
            CpfStateSnapshot next) {
        if (!stateKey.equals(next.stateKey())) {
            throw new IllegalArgumentException("stateKey does not match snapshot");
        }
        if (!operationId.equals(next.lastOperationId())) {
            throw new IllegalArgumentException("operationId does not match snapshot");
        }
        if (commandHash == null || !commandHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("commandHash must be lowercase SHA-256");
        }
        long requiredNextVersion = expectedVersion < 0L ? 0L : Math.addExact(expectedVersion, 1L);
        if (next.version() != requiredNextVersion) {
            throw new IllegalArgumentException("snapshot version does not follow expectedVersion");
        }
    }

    private record CommandKey(String stateKey, String operationId) {}
    private record CommandRecord(
            String commandHash, CpfStateSnapshot snapshot, Instant recordedAt) {}
}
