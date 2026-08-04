package com.cpf.core.common.reconciliation;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** Reconciliation Worker가 실제 소비하는 Runtime 정책입니다. */
public final class CpfReconciliationRuntimePolicy {
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
                version,
                enabled,
                queryIntervalMillis,
                thresholdSeconds,
                batchSize,
                leaseSeconds,
                manualResolutionRequired,
                unknownTypes,
                8,
                3,
                30_000L);
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
            long circuitOpenMillis) {
        Snapshot next =
                new Snapshot(
                        version,
                        enabled,
                        queryIntervalMillis,
                        thresholdSeconds,
                        batchSize,
                        leaseSeconds,
                        manualResolutionRequired,
                        normalize(unknownTypes),
                        maxAttempts,
                        circuitFailureThreshold,
                        circuitOpenMillis);
        reference.set(next);
        return next;
    }

    private static Set<String> normalize(Set<String> source) {
        if (source == null) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : source) {
            if (value != null && !value.isBlank()) {
                result.add(value.trim().toUpperCase(Locale.ROOT));
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
            long circuitOpenMillis) {
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
                    || circuitOpenMillis > 3_600_000L) {
                throw new IllegalArgumentException("reconciliation policy 범위 오류");
            }
            if (enabled && unknownTypes.isEmpty()) {
                throw new IllegalArgumentException(
                        "reconciliation enabled 상태에는 unknownTypes allowlist가 필요합니다.");
            }
        }

        private static Snapshot defaults() {
            return new Snapshot(
                    0L,
                    false,
                    30_000L,
                    60,
                    100,
                    60,
                    true,
                    Set.of(),
                    8,
                    3,
                    30_000L);
        }
    }
}
