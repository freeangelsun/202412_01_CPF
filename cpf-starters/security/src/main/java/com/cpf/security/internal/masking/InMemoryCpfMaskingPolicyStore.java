package com.cpf.security.internal.masking;

import com.cpf.security.api.CpfMaskingPolicyRuntimeStatus;
import com.cpf.security.api.CpfMaskingPolicySnapshot;
import com.cpf.security.spi.CpfMaskingPolicyStore;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded single-JVM masking policy store used when no distributed provider is configured. */
public final class InMemoryCpfMaskingPolicyStore implements CpfMaskingPolicyStore {
    public static final int DEFAULT_MAXIMUM_HISTORY = 64;
    public static final int DEFAULT_MAXIMUM_COMMAND_RECORDS = 4_096;
    public static final Duration DEFAULT_COMMAND_TTL = Duration.ofDays(7);

    private final Clock clock;
    private final int maximumHistory;
    private final int maximumCommandRecords;
    private final Duration commandTtl;
    private final LinkedHashMap<Long, CpfMaskingPolicySnapshot> versions = new LinkedHashMap<>();
    private final LinkedHashMap<String, CommandRecord> commands = new LinkedHashMap<>();
    private final AtomicLong rejectedCommandCount = new AtomicLong();
    private CpfMaskingPolicySnapshot current;

    public InMemoryCpfMaskingPolicyStore(CpfMaskingPolicySnapshot initial, Clock clock) {
        this(initial, DEFAULT_MAXIMUM_HISTORY, DEFAULT_MAXIMUM_COMMAND_RECORDS, DEFAULT_COMMAND_TTL, clock);
    }

    public InMemoryCpfMaskingPolicyStore(CpfMaskingPolicySnapshot initial, Clock clock,
            int maximumHistory, int maximumCommandRecords) {
        this(initial, maximumHistory, maximumCommandRecords, DEFAULT_COMMAND_TTL, clock);
    }

    public InMemoryCpfMaskingPolicyStore(CpfMaskingPolicySnapshot initial,
            int maximumHistory, int maximumCommandRecords, Duration commandTtl, Clock clock) {
        this.current = Objects.requireNonNull(initial, "initial");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (maximumHistory < 2 || maximumHistory > 4_096) {
            throw new IllegalArgumentException("maximumHistory must be between 2 and 4096");
        }
        if (maximumCommandRecords < 16 || maximumCommandRecords > 65_536) {
            throw new IllegalArgumentException("maximumCommandRecords must be between 16 and 65536");
        }
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("commandTtl must be positive and <= 365d");
        }
        this.maximumHistory = maximumHistory;
        this.maximumCommandRecords = maximumCommandRecords;
        this.commandTtl = commandTtl;
        versions.put(initial.version(), initial);
    }

    @Override public synchronized Optional<CpfMaskingPolicySnapshot> current() {
        return Optional.of(current);
    }

    @Override public synchronized Optional<CpfMaskingPolicySnapshot> findVersion(long version) {
        return Optional.ofNullable(versions.get(version));
    }

    @Override public synchronized List<CpfMaskingPolicySnapshot> history(int limit) {
        int bounded = Math.max(1, Math.min(limit, maximumHistory));
        return versions.values().stream()
                .sorted(Comparator.comparingLong(CpfMaskingPolicySnapshot::version).reversed())
                .limit(bounded)
                .toList();
    }

    @Override public synchronized WriteResult compareAndSet(long expectedVersion, String commandId,
            String commandHash, CpfMaskingPolicySnapshot next) {
        commandId = required(commandId, "commandId");
        commandHash = hash(commandHash, "commandHash");
        next = Objects.requireNonNull(next, "next");
        evictExpiredCommands(clock.instant());
        CommandRecord existing = commands.get(commandId);
        if (existing != null) {
            if (existing.commandHash.equals(commandHash)) {
                return new WriteResult(Status.IDEMPOTENT_REPLAY, existing.snapshot);
            }
            rejectedCommandCount.incrementAndGet();
            return new WriteResult(Status.COMMAND_CONFLICT, existing.snapshot);
        }
        if (commands.size() >= maximumCommandRecords) {
            rejectedCommandCount.incrementAndGet();
            return new WriteResult(Status.RESOURCE_EXHAUSTED, current);
        }
        if (current.version() != expectedVersion || next.version() != expectedVersion + 1L) {
            rejectedCommandCount.incrementAndGet();
            return new WriteResult(Status.VERSION_CONFLICT, current);
        }
        current = next;
        versions.put(next.version(), next);
        commands.put(commandId, new CommandRecord(commandHash, next, clock.instant().plus(commandTtl)));
        trimHistory();
        return new WriteResult(Status.APPLIED, next);
    }

    @Override public synchronized CpfMaskingPolicyRuntimeStatus runtimeStatus() {
        evictExpiredCommands(clock.instant());
        return new CpfMaskingPolicyRuntimeStatus(
                CpfMaskingPolicyRuntimeStatus.Health.UP,
                current.version(), versions.size(), commands.size(), maximumHistory,
                maximumCommandRecords, rejectedCommandCount.get(), 0L, clock.instant());
    }

    private void evictExpiredCommands(Instant now) {
        commands.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }

    private void trimHistory() {
        while (versions.size() > maximumHistory) {
            Long oldest = versions.keySet().iterator().next();
            if (oldest.longValue() == current.version()) break;
            versions.remove(oldest);
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (normalized.length() > 128 || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }

    private static String hash(String value, String field) {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(field + " must be lowercase SHA-256");
        }
        return value;
    }

    private record CommandRecord(String commandHash, CpfMaskingPolicySnapshot snapshot, Instant expiresAt) { }
}
