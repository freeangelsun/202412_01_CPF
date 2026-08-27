package com.cpf.starter.platform.operations.observability.internal.logging;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionRuntimeStatus;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import com.cpf.platform.operations.observability.spi.logging.CpfLogPolicyVersionStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Explicit single-JVM provider with bounded history and expiring command records. */
public final class InMemoryCpfLogPolicyVersionStore implements CpfLogPolicyVersionStore {
    public static final int DEFAULT_MAXIMUM_TARGETS = 4_096;
    public static final int DEFAULT_MAXIMUM_HISTORY_PER_TARGET = 128;
    public static final int DEFAULT_MAXIMUM_COMMAND_RECORDS = 16_384;
    public static final Duration DEFAULT_COMMAND_TTL = Duration.ofDays(7);

    private final Clock clock;
    private final int maximumTargets;
    private final int maximumHistoryPerTarget;
    private final int maximumCommandRecords;
    private final Duration commandTtl;
    private final LinkedHashMap<TargetKey, LinkedHashMap<Long, CpfLogPolicyVersionSnapshot>> timelines = new LinkedHashMap<>();
    private final LinkedHashMap<String, CommandRecord> commands = new LinkedHashMap<>();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong unknown = new AtomicLong();

    public InMemoryCpfLogPolicyVersionStore(Clock clock) {
        this(DEFAULT_MAXIMUM_TARGETS, DEFAULT_MAXIMUM_HISTORY_PER_TARGET,
                DEFAULT_MAXIMUM_COMMAND_RECORDS, DEFAULT_COMMAND_TTL, clock);
    }

    public InMemoryCpfLogPolicyVersionStore(int maximumTargets, int maximumHistoryPerTarget,
            int maximumCommandRecords, Duration commandTtl, Clock clock) {
        if (maximumTargets < 1 || maximumTargets > 100_000) throw new IllegalArgumentException("maximumTargets is invalid");
        if (maximumHistoryPerTarget < 2 || maximumHistoryPerTarget > 4_096) throw new IllegalArgumentException("maximumHistoryPerTarget is invalid");
        if (maximumCommandRecords < 16 || maximumCommandRecords > 1_000_000) throw new IllegalArgumentException("maximumCommandRecords is invalid");
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) throw new IllegalArgumentException("commandTtl is invalid");
        this.maximumTargets = maximumTargets;
        this.maximumHistoryPerTarget = maximumHistoryPerTarget;
        this.maximumCommandRecords = maximumCommandRecords;
        this.commandTtl = commandTtl;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override public synchronized CpfLogPolicyVersionSnapshot ensureBaseline(CpfLogPolicyVersionSnapshot baseline) {
        Objects.requireNonNull(baseline, "baseline");
        if (baseline.version() != 1L || baseline.status() != CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            throw new IllegalArgumentException("baseline must be ACTIVE version 1");
        }
        TargetKey key = new TargetKey(baseline.targetType(), baseline.targetId());
        LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> existing = timelines.get(key);
        if (existing != null) return latest(existing);
        if (timelines.size() >= maximumTargets) throw new IllegalStateException("log policy target capacity exhausted");
        LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> created = new LinkedHashMap<>();
        created.put(1L, baseline);
        timelines.put(key, created);
        return baseline;
    }

    @Override public synchronized Optional<CpfLogPolicyVersionSnapshot> current(
            LogPolicyTargetType type, String targetId) {
        LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> timeline = timelines.get(new TargetKey(type, targetId));
        return timeline == null || timeline.isEmpty() ? Optional.empty() : Optional.of(latest(timeline));
    }
    @Override public synchronized Optional<CpfLogPolicyVersionSnapshot> findVersion(
            LogPolicyTargetType type, String targetId, long version) {
        LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> timeline = timelines.get(new TargetKey(type, targetId));
        return timeline == null ? Optional.empty() : Optional.ofNullable(timeline.get(version));
    }
    @Override public synchronized List<CpfLogPolicyVersionSnapshot> history(
            LogPolicyTargetType type, String targetId, int limit) {
        LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> timeline = timelines.get(new TargetKey(type, targetId));
        if (timeline == null) return List.of();
        int bounded = Math.max(1, Math.min(limit, maximumHistoryPerTarget));
        return timeline.values().stream()
                .sorted(Comparator.comparingLong(CpfLogPolicyVersionSnapshot::version).reversed())
                .limit(bounded).toList();
    }

    @Override public synchronized WriteResult compareAndSet(long expectedVersion, String commandId,
            String commandHash, CpfLogPolicyVersionSnapshot next) {
        Objects.requireNonNull(next, "next");
        commandId = required(commandId, "commandId");
        commandHash = hash(commandHash);
        Instant now = clock.instant();
        evictExpired(now);
        TargetKey key = new TargetKey(next.targetType(), next.targetId());
        CommandRecord previous = commands.get(commandId);
        if (previous != null) {
            if (previous.key().equals(key) && previous.commandHash().equals(commandHash)) {
                return new WriteResult(Status.IDEMPOTENT_REPLAY, previous.snapshot());
            }
            rejected.incrementAndGet();
            return new WriteResult(Status.COMMAND_CONFLICT, previous.snapshot());
        }
        if (commands.size() >= maximumCommandRecords) {
            rejected.incrementAndGet();
            return new WriteResult(Status.RESOURCE_EXHAUSTED, current(key.type(), key.targetId()).orElse(null));
        }
        LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> timeline = timelines.get(key);
        if (timeline == null) {
            rejected.incrementAndGet();
            return new WriteResult(Status.VERSION_CONFLICT, null);
        }
        CpfLogPolicyVersionSnapshot current = latest(timeline);
        if (current.version() != expectedVersion || next.version() != expectedVersion + 1L
                || current.status() != CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            rejected.incrementAndGet();
            return new WriteResult(Status.VERSION_CONFLICT, current);
        }
        timeline.put(next.version(), next);
        trim(timeline);
        commands.put(commandId, new CommandRecord(key, commandHash, next, safePlus(now, commandTtl)));
        return new WriteResult(Status.APPLIED, next);
    }

    @Override public synchronized StatusResult updateStatus(LogPolicyTargetType type, String targetId,
            long expectedVersion, CpfLogPolicyVersionSnapshot.Status expectedStatus,
            CpfLogPolicyVersionSnapshot.Status nextStatus, String actor, String reason) {
        TargetKey key = new TargetKey(type, targetId);
        LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> timeline = timelines.get(key);
        if (timeline == null) return new StatusResult(false, null);
        CpfLogPolicyVersionSnapshot current = latest(timeline);
        if (current.version() != expectedVersion || current.status() != expectedStatus) {
            return new StatusResult(false, current);
        }
        CpfLogPolicyVersionSnapshot changed = new CpfLogPolicyVersionSnapshot(type, targetId,
                current.version(), nextStatus, current.decision(), clock.instant(), actor, reason);
        timeline.put(changed.version(), changed);
        commands.replaceAll((commandId, record) -> record.key().equals(key)
                && record.snapshot().version() == changed.version()
                ? new CommandRecord(record.key(), record.commandHash(), changed, record.expiresAt())
                : record);
        if (nextStatus == CpfLogPolicyVersionSnapshot.Status.UNKNOWN) unknown.incrementAndGet();
        return new StatusResult(true, changed);
    }

    @Override public synchronized CpfLogPolicyVersionRuntimeStatus runtimeStatus() {
        evictExpired(clock.instant());
        int versions = timelines.values().stream().mapToInt(value -> value.size()).sum();
        return new CpfLogPolicyVersionRuntimeStatus(CpfLogPolicyVersionRuntimeStatus.Health.UP,
                timelines.size(), versions, commands.size(), maximumTargets, maximumHistoryPerTarget,
                maximumCommandRecords, rejected.get(), unknown.get(), 0L, 0L, clock.instant());
    }

    private static CpfLogPolicyVersionSnapshot latest(Map<Long, CpfLogPolicyVersionSnapshot> timeline) {
        return timeline.values().stream().max(Comparator.comparingLong(CpfLogPolicyVersionSnapshot::version))
                .orElseThrow();
    }
    private void trim(LinkedHashMap<Long, CpfLogPolicyVersionSnapshot> timeline) {
        while (timeline.size() > maximumHistoryPerTarget) timeline.remove(timeline.keySet().iterator().next());
    }
    private void evictExpired(Instant now) {
        commands.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }
    private static Instant safePlus(Instant now, Duration ttl) {
        try { return now.plus(ttl); } catch (RuntimeException overflow) { return Instant.MAX; }
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (normalized.length() > 128 || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }
    private static String hash(String value) {
        if (value == null || !value.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("commandHash is invalid");
        return value;
    }
    private record TargetKey(LogPolicyTargetType type, String targetId) {
        private TargetKey { Objects.requireNonNull(type, "type"); targetId = LogPolicyDecision.normalizeTargetId(targetId); }
    }
    private record CommandRecord(TargetKey key, String commandHash,
            CpfLogPolicyVersionSnapshot snapshot, Instant expiresAt) { }
}
