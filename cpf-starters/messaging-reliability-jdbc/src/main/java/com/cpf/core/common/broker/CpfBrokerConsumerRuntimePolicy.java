package com.cpf.core.common.broker;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** Generic Broker Worker와 transport adapter가 공유하는 immutable Runtime 정책입니다. */
public final class CpfBrokerConsumerRuntimePolicy {
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.defaults());

    public Snapshot current() {
        return snapshot.get();
    }

    public Snapshot replaceConsumer(boolean paused, int concurrency, int prefetch) {
        return snapshot.updateAndGet(current -> current.withConsumer(paused, concurrency, prefetch));
    }

    public Snapshot replaceRetry(
            int maxAttempts,
            long initialBackoffMillis,
            long maxBackoffMillis,
            Set<String> retryableExceptionClasses) {
        return snapshot.updateAndGet(current -> current.withRetry(
                maxAttempts, initialBackoffMillis, maxBackoffMillis, retryableExceptionClasses));
    }

    public record Snapshot(
            boolean paused,
            int concurrency,
            int prefetch,
            int maxAttempts,
            long initialBackoffMillis,
            long maxBackoffMillis,
            Set<String> retryableExceptionClasses) {

        private static Snapshot defaults() {
            return new Snapshot(false, 1, 1, 1, 0L, 0L, Set.of());
        }

        private Snapshot withConsumer(boolean paused, int concurrency, int prefetch) {
            validateConsumer(concurrency, prefetch);
            return new Snapshot(paused, concurrency, prefetch, maxAttempts,
                    initialBackoffMillis, maxBackoffMillis, retryableExceptionClasses);
        }

        private Snapshot withRetry(
                int maxAttempts,
                long initialBackoffMillis,
                long maxBackoffMillis,
                Set<String> retryableExceptionClasses) {
            if (maxAttempts < 1 || maxAttempts > 10) {
                throw new IllegalArgumentException("maxAttempts는 1~10이어야 합니다.");
            }
            if (initialBackoffMillis < 0 || initialBackoffMillis > 60000) {
                throw new IllegalArgumentException("initialBackoffMillis는 0~60000이어야 합니다.");
            }
            if (maxBackoffMillis < initialBackoffMillis || maxBackoffMillis > 600000) {
                throw new IllegalArgumentException("maxBackoffMillis 범위가 유효하지 않습니다.");
            }
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            if (retryableExceptionClasses != null) {
                if (retryableExceptionClasses.size() > 100) {
                    throw new IllegalArgumentException("retryableExceptionClasses는 최대 100개입니다.");
                }
                retryableExceptionClasses.stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(value -> value.trim().toUpperCase(Locale.ROOT))
                        .forEach(normalized::add);
            }
            return new Snapshot(paused, concurrency, prefetch, maxAttempts,
                    initialBackoffMillis, maxBackoffMillis, Set.copyOf(normalized));
        }

        public boolean retryable(RuntimeException ex) {
            if (ex == null || retryableExceptionClasses.isEmpty()) return false;
            Class<?> type = ex.getClass();
            while (type != null && RuntimeException.class.isAssignableFrom(type)) {
                String name = type.getName().toUpperCase(Locale.ROOT);
                String simple = type.getSimpleName().toUpperCase(Locale.ROOT);
                if (retryableExceptionClasses.contains(name) || retryableExceptionClasses.contains(simple)) return true;
                type = type.getSuperclass();
            }
            return false;
        }

        public long backoffMillis(int failedAttempt) {
            if (initialBackoffMillis <= 0) return 0L;
            int exponent = Math.max(0, Math.min(failedAttempt - 1, 20));
            long multiplied;
            try {
                multiplied = Math.multiplyExact(initialBackoffMillis, 1L << exponent);
            } catch (ArithmeticException ex) {
                multiplied = maxBackoffMillis;
            }
            return Math.min(multiplied, maxBackoffMillis);
        }

        private static void validateConsumer(int concurrency, int prefetch) {
            if (concurrency < 1 || concurrency > 1024) throw new IllegalArgumentException("concurrency 범위 오류");
            if (prefetch < 1 || prefetch > 100000) throw new IllegalArgumentException("prefetch 범위 오류");
        }
    }
}
