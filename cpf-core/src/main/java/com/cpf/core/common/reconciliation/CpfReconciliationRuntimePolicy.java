package com.cpf.core.common.reconciliation;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** Reconciliation Worker가 실제 소비하는 versioned Runtime 정책입니다. */
public final class CpfReconciliationRuntimePolicy {
    public static final int DEFAULT_MAXIMUM_CIRCUIT_ENTRIES = 256;
    public static final long DEFAULT_CIRCUIT_IDLE_TTL_MILLIS = 1_800_000L;

    private final AtomicReference<Snapshot> reference =
            new AtomicReference<>(Snapshot.defaults());

    public Snapshot current() {
        return reference.get();
    }

    /** 기존 Consumer 호환용 overload입니다. */
    public Snapshot replace(
            long version,
            boolean enabled,
            long queryIntervalMillis,
            int thresholdSeconds,
            int batchSize,
            int leaseSeconds,
            boolean manualResolutionRequired,
            Set<String> unknownTypes) {
        return replace(
                version, enabled, queryIntervalMillis, thresholdSeconds, batchSize, leaseSeconds,
                manualResolutionRequired, unknownTypes, 8, 3, 30_000L,
                DEFAULT_MAXIMUM_CIRCUIT_ENTRIES, DEFAULT_CIRCUIT_IDLE_TTL_MILLIS);
    }

    /** 기존 11-argument Consumer 호환용 overload입니다. */
    public Snapshot replace(
            long version,
            boolean enabled,
            long queryIntervalMillis,
            int thresholdSeconds,
            int batchSize,
            int leaseSeconds,
            boolean manualResolutionRequired,
            Set<String> unknownTypes,
            int maxAttempts,
            int circuitFailureThreshold,
            long circuitOpenMillis) {
        return replace(
                version, enabled, queryIntervalMillis, thresholdSeconds, batchSize, leaseSeconds,
                manualResolutionRequired, unknownTypes, maxAttempts, circuitFailureThreshold,
                circuitOpenMillis, DEFAULT_MAXIMUM_CIRCUIT_ENTRIES,
                DEFAULT_CIRCUIT_IDLE_TTL_MILLIS);
    }

    public Snapshot replace(
            long version,
            boolean enabled,
            long queryIntervalMillis,
            int thresholdSeconds,
            int batchSize,
            int leaseSeconds,
            boolean manualResolutionRequired,
            Set<String> unknownTypes,
            int maxAttempts,
            int circuitFailureThreshold,
            long circuitOpenMillis,
            int maximumCircuitEntries,
            long circuitIdleTtlMillis) {
        Snapshot next = new Snapshot(
                version, enabled, queryIntervalMillis, thresholdSeconds, batchSize, leaseSeconds,
                manualResolutionRequired, normalize(unknownTypes), maxAttempts,
                circuitFailureThreshold, circuitOpenMillis, maximumCircuitEntries,
                circuitIdleTtlMillis);
        while (true) {
            Snapshot current = reference.get();
            if (version < current.version()) {
                throw new IllegalArgumentException("reconciliation policy version rollback is forbidden");
            }
            if (version == current.version()) {
                if (next.equals(current)) return current;
                throw new IllegalStateException("same reconciliation policy version has different content");
            }
            if (reference.compareAndSet(current, next)) return next;
        }
    }

    private static Set<String> normalize(Set<String> source) {
        if (source == null) return Set.of();
        if (source.size() > 100) {
            throw new IllegalArgumentException("unknownTypes allowlist exceeds 100 entries");
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : source) {
            if (value != null && !value.isBlank()) {
                String normalized = value.trim().toUpperCase(Locale.ROOT);
                if (normalized.length() > 100 || !normalized.matches("[A-Z0-9][A-Z0-9._:-]*")) {
                    throw new IllegalArgumentException("invalid reconciliation unknown type: " + value);
                }
                result.add(normalized);
            }
        }
        return Set.copyOf(result);
    }

    public record Snapshot(
            long version,
            boolean enabled,
            long queryIntervalMillis,
            int thresholdSeconds,
            int batchSize,
            int leaseSeconds,
            boolean manualResolutionRequired,
            Set<String> unknownTypes,
            int maxAttempts,
            int circuitFailureThreshold,
            long circuitOpenMillis,
            int maximumCircuitEntries,
            long circuitIdleTtlMillis) {

        /** Source-compatible constructor for policy snapshots created before bounded circuit state. */
        public Snapshot(
                long version,
                boolean enabled,
                long queryIntervalMillis,
                int thresholdSeconds,
                int batchSize,
                int leaseSeconds,
                boolean manualResolutionRequired,
                Set<String> unknownTypes,
                int maxAttempts,
                int circuitFailureThreshold,
                long circuitOpenMillis) {
            this(version, enabled, queryIntervalMillis, thresholdSeconds, batchSize, leaseSeconds,
                    manualResolutionRequired, unknownTypes, maxAttempts, circuitFailureThreshold,
                    circuitOpenMillis, DEFAULT_MAXIMUM_CIRCUIT_ENTRIES,
                    DEFAULT_CIRCUIT_IDLE_TTL_MILLIS);
        }

        public Snapshot {
            unknownTypes = unknownTypes == null ? Set.of() : Set.copyOf(unknownTypes);
            if (queryIntervalMillis < 1_000L
                    || queryIntervalMillis > 3_600_000L
                    || thresholdSeconds < 0
                    || batchSize < 1
                    || batchSize > 1_000
                    || leaseSeconds < 5
                    || leaseSeconds > 3_600
                    || maxAttempts < 1
                    || maxAttempts > 100
                    || circuitFailureThreshold < 1
                    || circuitFailureThreshold > 100
                    || circuitOpenMillis < 1_000L
                    || circuitOpenMillis > 3_600_000L
                    || maximumCircuitEntries < 1
                    || maximumCircuitEntries > 10_000
                    || circuitIdleTtlMillis < 1_000L
                    || circuitIdleTtlMillis > 86_400_000L) {
                throw new IllegalArgumentException("reconciliation policy 범위 오류");
            }
            if (enabled && unknownTypes.isEmpty()) {
                throw new IllegalArgumentException(
                        "reconciliation enabled 상태에는 unknownTypes allowlist가 필요합니다.");
            }
        }

        private static Snapshot defaults() {
            return new Snapshot(
                    0L, false, 30_000L, 60, 100, 60, true, Set.of(),
                    8, 3, 30_000L, DEFAULT_MAXIMUM_CIRCUIT_ENTRIES,
                    DEFAULT_CIRCUIT_IDLE_TTL_MILLIS);
        }
    }
}
