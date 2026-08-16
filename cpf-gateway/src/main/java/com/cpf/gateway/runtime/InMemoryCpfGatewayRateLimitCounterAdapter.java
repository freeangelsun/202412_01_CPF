package com.cpf.gateway.runtime;

import com.cpf.gateway.api.CpfGatewayRateLimitCounterPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 단일 JVM용 bounded Counter Adapter입니다.
 * 비로컬/다중 인스턴스 운영에서는 distributed Provider로 교체해야 합니다.
 */
public final class InMemoryCpfGatewayRateLimitCounterAdapter implements CpfGatewayRateLimitCounterPort {
    private static final int MAX_ATOMIC_SCOPES = 16;
    private static final int MAX_BATCH_DEDUPE = 100_000;

    private final ConcurrentHashMap<String, WindowState> states = new ConcurrentHashMap<>();
    private final LinkedHashMap<String, BatchMemo> batchRequests = new LinkedHashMap<>();
    private final AtomicLong operations = new AtomicLong();
    private final Object mutationLock = new Object();
    private final int maxCounters;
    private final int maxBatchDedupe;
    private final Clock clock;

    public InMemoryCpfGatewayRateLimitCounterAdapter(int maxCounters) {
        this(maxCounters, Clock.systemUTC());
    }

    InMemoryCpfGatewayRateLimitCounterAdapter(int maxCounters, Clock clock) {
        this(maxCounters, MAX_BATCH_DEDUPE, clock);
    }

    InMemoryCpfGatewayRateLimitCounterAdapter(int maxCounters, int maxBatchDedupe, Clock clock) {
        if (maxCounters < 100 || maxCounters > 5_000_000) {
            throw new IllegalArgumentException("maxCounters must be between 100 and 5000000");
        }
        if (maxBatchDedupe < 1 || maxBatchDedupe > MAX_BATCH_DEDUPE) {
            throw new IllegalArgumentException("maxBatchDedupe must be between 1 and " + MAX_BATCH_DEDUPE);
        }
        this.maxCounters = maxCounters;
        this.maxBatchDedupe = maxBatchDedupe;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CounterResult consume(CounterCommand command) {
        return consumeAtomically(List.of(Objects.requireNonNull(command, "command")))
                .results().getFirst();
    }

    @Override
    public BatchResult consumeAtomically(List<CounterCommand> commands) {
        Objects.requireNonNull(commands, "commands");
        if (commands.isEmpty()) return new BatchResult(true, -1, List.of());
        if (commands.size() > MAX_ATOMIC_SCOPES) {
            throw new IllegalArgumentException("too many atomic rate-limit scopes");
        }
        List<CounterCommand> immutable = commands.stream()
                .map(command -> Objects.requireNonNull(command, "command"))
                .toList();
        Set<String> uniqueKeys = new HashSet<>();
        Long policyVersion = null;
        Long observationTime = null;
        for (CounterCommand command : immutable) {
            if (!uniqueKeys.add(command.counterKey())) {
                throw new IllegalArgumentException("duplicate counterKey in atomic batch");
            }
            if (policyVersion == null) policyVersion = command.policyVersion();
            else if (policyVersion.longValue() != command.policyVersion()) {
                throw new IllegalArgumentException("atomic batch must use one policy version");
            }
            if (observationTime == null) observationTime = command.nowEpochMillis();
            else if (observationTime.longValue() != command.nowEpochMillis()) {
                throw new IllegalArgumentException("atomic batch must use one observation time");
            }
        }

        synchronized (mutationLock) {
            long now = immutable.stream().mapToLong(CounterCommand::nowEpochMillis).max().orElse(0L);
            cleanup(now);
            String batchKey = batchKey(immutable);
            String payloadHash = payloadHash(immutable);
            BatchMemo previous = batchRequests.get(batchKey);
            if (previous != null && previous.expiresAt() <= now) {
                batchRequests.remove(batchKey);
                previous = null;
            }
            if (previous != null) {
                if (!MessageDigest.isEqual(
                        previous.payloadHash().getBytes(StandardCharsets.US_ASCII),
                        payloadHash.getBytes(StandardCharsets.US_ASCII))) {
                    throw new IllegalStateException("Rate-limit request payload conflict");
                }
                return duplicate(previous.result());
            }
            ensureBatchDedupeCapacity(batchKey, now);
            ensureCapacity(uniqueKeys, now);

            LinkedHashMap<String, WindowState> working = new LinkedHashMap<>();
            List<CounterResult> results = new ArrayList<>(immutable.size());
            int limitingIndex = -1;
            for (int index = 0; index < immutable.size(); index++) {
                CounterCommand command = immutable.get(index);
                WindowState state = working.computeIfAbsent(command.counterKey(), key ->
                        prepareState(states.get(key), command));
                CounterResult result = evaluate(state, command);
                results.add(result);
                if (!result.accepted()) {
                    limitingIndex = index;
                    break;
                }
            }

            if (limitingIndex >= 0) {
                CounterCommand deniedCommand = immutable.get(limitingIndex);
                states.put(deniedCommand.counterKey(), working.get(deniedCommand.counterKey()));
                while (results.size() < immutable.size()) {
                    CounterCommand skipped = immutable.get(results.size());
                    results.add(new CounterResult(
                            false, false, 0L, 0L, skipped.resetAtEpochMillis(), 0L, 0,
                            "ATOMIC_BATCH_NOT_EVALUATED"));
                }
                BatchResult denied = new BatchResult(false, limitingIndex, results);
                remember(batchKey, payloadHash, denied);
                return denied;
            }

            working.forEach(states::put);
            BatchResult accepted = new BatchResult(true, -1, results);
            remember(batchKey, payloadHash, accepted);
            return accepted;
        }
    }

    @Override
    public CounterHealth health() {
        return new CounterHealth(true, states.size(), "LOCAL_IN_MEMORY", Instant.now(clock));
    }

    @Override
    public boolean distributed() {
        return false;
    }

    private CounterResult evaluate(WindowState state, CounterCommand command) {
        long effectiveReset = Math.max(command.resetAtEpochMillis(), state.blockedUntil);
        DecisionMemo memo;
        if (state.blockedUntil > command.nowEpochMillis()) {
            memo = new DecisionMemo(false, state.used, 0L, effectiveReset,
                    state.blockedUntil, state.rejected, "ABUSE_BLOCKED");
        } else {
            long capacity = Math.addExact((long) command.quota(), (long) command.burst());
            long requested = Math.addExact(state.used, command.units());
            if (requested <= capacity) {
                state.used = requested;
                memo = new DecisionMemo(true, state.used, capacity - state.used,
                        command.resetAtEpochMillis(), 0L, state.rejected, "ALLOWED");
            } else {
                state.rejected++;
                if (command.abuseThreshold() > 0
                        && state.rejected >= command.abuseThreshold()
                        && command.blockMillis() > 0L) {
                    state.blockedUntil = Math.addExact(command.nowEpochMillis(), command.blockMillis());
                }
                effectiveReset = Math.max(command.resetAtEpochMillis(), state.blockedUntil);
                memo = new DecisionMemo(false, state.used, capacity - state.used, effectiveReset,
                        state.blockedUntil, state.rejected,
                        state.blockedUntil > command.nowEpochMillis()
                                ? "ABUSE_BLOCKED" : "QUOTA_EXCEEDED");
            }
        }
        return memo.toResult(false);
    }

    private static WindowState prepareState(WindowState current, CounterCommand command) {
        if (current == null || current.policyVersion != command.policyVersion()) {
            return new WindowState(command.policyVersion(), command.windowStartEpochMillis());
        }
        if (current.windowStart == command.windowStartEpochMillis()) {
            return current.copy();
        }
        WindowState nextWindow = new WindowState(
                command.policyVersion(), command.windowStartEpochMillis());
        if (current.blockedUntil > command.nowEpochMillis()) {
            nextWindow.blockedUntil = current.blockedUntil;
            nextWindow.rejected = current.rejected;
        }
        return nextWindow;
    }

    private void ensureCapacity(Set<String> uniqueKeys, long now) {
        long newKeys = uniqueKeys.stream().filter(key -> !states.containsKey(key)).count();
        if (states.size() + newKeys <= maxCounters) return;
        cleanupExpired(now, true);
        newKeys = uniqueKeys.stream().filter(key -> !states.containsKey(key)).count();
        if (states.size() + newKeys > maxCounters) {
            throw new IllegalStateException("Gateway rate-limit counter capacity exceeded");
        }
    }

    private void cleanup(long now) {
        if ((operations.incrementAndGet() & 1023L) == 0L) {
            cleanupExpired(now, false);
            batchRequests.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        }
    }

    private void remember(String batchKey, String payloadHash, BatchResult result) {
        long expiresAt = result.results().stream()
                .mapToLong(value -> Math.max(value.resetAtEpochMillis(), value.blockedUntilEpochMillis()))
                .max()
                .orElse(0L);
        batchRequests.put(batchKey, new BatchMemo(payloadHash, result, expiresAt));
    }

    private void ensureBatchDedupeCapacity(String batchKey, long now) {
        if (batchRequests.containsKey(batchKey)) return;
        batchRequests.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        if (batchRequests.size() >= maxBatchDedupe) {
            throw new IllegalStateException("Gateway rate-limit dedupe capacity exceeded");
        }
    }

    private static BatchResult duplicate(BatchResult result) {
        List<CounterResult> duplicateResults = result.results().stream()
                .map(value -> new CounterResult(
                        value.accepted(), true, value.used(), value.remaining(),
                        value.resetAtEpochMillis(), value.blockedUntilEpochMillis(),
                        value.rejectedCount(), value.reason()))
                .toList();
        return new BatchResult(result.accepted(), result.limitingIndex(), duplicateResults);
    }

    private static String batchKey(List<CounterCommand> commands) {
        StringBuilder value = new StringBuilder();
        for (CounterCommand command : commands) {
            value.append(command.counterKey()).append('\u0001')
                    .append(command.requestId()).append('\u0002');
        }
        return sha256(value.toString());
    }

    private static String payloadHash(List<CounterCommand> commands) {
        StringBuilder value = new StringBuilder();
        for (CounterCommand command : commands) {
            value.append(command.policyVersion()).append('|')
                    .append(command.counterKey()).append('|')
                    .append(command.requestId()).append('|')
                    .append(command.windowStartEpochMillis()).append('|')
                    .append(command.windowMillis()).append('|')
                    .append(command.quota()).append('|')
                    .append(command.burst()).append('|')
                    .append(command.units()).append('|')
                    .append(command.abuseThreshold()).append('|')
                    .append(command.blockMillis()).append('\n');
        }
        return sha256(value.toString());
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private void cleanupExpired(long now, boolean aggressive) {
        states.entrySet().removeIf(entry -> {
            WindowState state = entry.getValue();
            long retention = aggressive ? 0L : 60_000L;
            long lastRelevant = Math.max(state.windowStart + 86_400_000L, state.blockedUntil);
            return lastRelevant + retention < now;
        });
    }

    private static final class WindowState {
        private final long policyVersion;
        private final long windowStart;
        private long used;
        private int rejected;
        private long blockedUntil;

        private WindowState(long policyVersion, long windowStart) {
            this.policyVersion = policyVersion;
            this.windowStart = windowStart;
        }

        private WindowState copy() {
            WindowState copy = new WindowState(policyVersion, windowStart);
            copy.used = used;
            copy.rejected = rejected;
            copy.blockedUntil = blockedUntil;
            return copy;
        }
    }

    private record BatchMemo(String payloadHash, BatchResult result, long expiresAt) {
    }

    private record DecisionMemo(
            boolean accepted,
            long used,
            long remaining,
            long resetAt,
            long blockedUntil,
            int rejectedCount,
            String reason) {
        private CounterResult toResult(boolean duplicate) {
            return new CounterResult(accepted, duplicate, used, remaining, resetAt,
                    blockedUntil, rejectedCount, reason);
        }
    }
}
