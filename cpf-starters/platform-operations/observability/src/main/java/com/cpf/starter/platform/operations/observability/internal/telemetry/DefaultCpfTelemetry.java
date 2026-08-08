package com.cpf.starter.platform.operations.observability.internal.telemetry;

import com.cpf.core.api.observability.CpfTelemetry;
import com.cpf.core.api.observability.CpfTraceContext;
import com.cpf.core.api.security.CpfSensitiveData;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Bounded, provider-neutral telemetry runtime used when no customer/provider implementation is supplied. */
public final class DefaultCpfTelemetry implements CpfTelemetry, AutoCloseable {
    private static final int MAX_ATTRIBUTES = 64;
    private static final int MAX_ATTRIBUTE_KEY = 96;
    private static final int MAX_ATTRIBUTE_VALUE = 256;
    private static final long MAX_SPAN_DURATION_NANOS = Duration.ofDays(7).toNanos();

    private final Clock clock;
    private final int maxActiveSpans;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentHashMap<Long, Span> active = new ConcurrentHashMap<>();
    private final AtomicInteger activeCount = new AtomicInteger();
    private final ReentrantReadWriteLock lifecycle = new ReentrantReadWriteLock();
    private final AtomicLong completed = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong totalDurationNanos = new AtomicLong();
    private final AtomicReference<Instant> lastErrorAt = new AtomicReference<>();
    private final AtomicReference<String> lastErrorType = new AtomicReference<>();

    public DefaultCpfTelemetry() {
        this(Clock.systemUTC(), 16_384);
    }

    public DefaultCpfTelemetry(Clock clock, int maxActiveSpans) {
        this.clock = Objects.requireNonNull(clock, "clock");
        if (maxActiveSpans < 1 || maxActiveSpans > 1_000_000) {
            throw new IllegalArgumentException("maxActiveSpans must be between 1 and 1000000");
        }
        this.maxActiveSpans = maxActiveSpans;
    }

    @Override
    public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
        lifecycle.readLock().lock();
        try {
            if (closed.get()) {
                rejected.incrementAndGet();
                throw new IllegalStateException("CPF telemetry is closed");
            }
            CpfTraceContext.SpanKind spanKind = parseKind(kind);
            String canonicalName = CpfTraceContext.canonicalSpanName(spanKind, name);
            Map<String, String> safeAttributes = sanitizeAttributes(attributes);
            reserveActiveSlot();
            boolean registered = false;
            try {
                long id = nextSpanId();
                Span span = new Span(id, canonicalName, spanKind, safeAttributes,
                        clock.instant(), System.nanoTime());
                Span previous = active.putIfAbsent(id, span);
                if (previous != null) {
                    rejected.incrementAndGet();
                    throw new IllegalStateException("telemetry span sequence collision");
                }
                registered = true;
                return span;
            } finally {
                if (!registered) activeCount.decrementAndGet();
            }
        } finally {
            lifecycle.readLock().unlock();
        }
    }

    private void reserveActiveSlot() {
        while (true) {
            int current = activeCount.get();
            if (current >= maxActiveSpans) {
                rejected.incrementAndGet();
                throw new RejectedExecutionException("CPF_TELEMETRY_ACTIVE_SPAN_LIMIT");
            }
            if (activeCount.compareAndSet(current, current + 1)) return;
        }
    }

    private long nextSpanId() {
        long id = sequence.incrementAndGet();
        if (id <= 0) {
            rejected.incrementAndGet();
            throw new IllegalStateException("telemetry span sequence exhausted");
        }
        return id;
    }

    @Override
    public Map<String, Object> status() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("state", closed.get() ? "CLOSED" : (rejected.get() > 0 || errors.get() > 0 ? "DEGRADED" : "RUNNING"));
        result.put("activeSpans", (long) activeCount.get());
        result.put("maxActiveSpans", maxActiveSpans);
        result.put("completedSpans", completed.get());
        result.put("errorSpans", errors.get());
        result.put("rejectedSpans", rejected.get());
        long done = completed.get();
        result.put("averageDurationNanos", done == 0 ? 0L : totalDurationNanos.get() / done);
        if (lastErrorAt.get() != null) result.put("lastErrorAt", lastErrorAt.get());
        if (lastErrorType.get() != null) result.put("lastErrorType", lastErrorType.get());
        return Map.copyOf(result);
    }

    @Override
    public void close() {
        lifecycle.writeLock().lock();
        try {
            if (!closed.compareAndSet(false, true)) return;
            for (Span span : active.values()) span.close();
            active.clear();
            activeCount.set(0);
        } finally {
            lifecycle.writeLock().unlock();
        }
    }

    private static CpfTraceContext.SpanKind parseKind(String kind) {
        String normalized = required(kind, "kind", 32);
        try {
            return CpfTraceContext.SpanKind.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException("unsupported span kind", invalid);
        }
    }

    private static Map<String, String> sanitizeAttributes(Map<String, String> source) {
        if (source == null || source.isEmpty()) return Map.of();
        if (source.size() > MAX_ATTRIBUTES) throw new IllegalArgumentException("too many trace attributes");
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        source.forEach((rawKey, rawValue) -> {
            String key = required(rawKey, "attribute key", MAX_ATTRIBUTE_KEY).toLowerCase(Locale.ROOT);
            if (!key.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("invalid trace attribute key: " + rawKey);
            String value = required(rawValue, key, MAX_ATTRIBUTE_VALUE);
            String sanitized = CpfSensitiveData.sanitizeAuditText(value);
            if (!value.equals(sanitized)) throw new IllegalArgumentException("sensitive trace attribute is forbidden: " + key);
            if (result.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("duplicate canonical trace attribute key: " + key);
            }
        });
        return Map.copyOf(result);
    }

    private static String required(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String normalized = value.trim();
        if (normalized.length() > maxLength) throw new IllegalArgumentException(name + " exceeds " + maxLength);
        if (normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(name + " contains control character");
        }
        return normalized;
    }

    private final class Span implements CpfTelemetrySpan {
        private final long id;
        private final String name;
        private final CpfTraceContext.SpanKind kind;
        private final Map<String, String> attributes;
        private final Instant startedAt;
        private final long startedNanos;
        private final AtomicBoolean finished = new AtomicBoolean();
        private final AtomicBoolean failed = new AtomicBoolean();

        private Span(long id, String name, CpfTraceContext.SpanKind kind,
                     Map<String, String> attributes, Instant startedAt, long startedNanos) {
            this.id = id;
            this.name = name;
            this.kind = kind;
            this.attributes = attributes;
            this.startedAt = startedAt;
            this.startedNanos = startedNanos;
        }

        @Override
        public synchronized void error(Throwable throwable) {
            if (finished.get() || throwable == null || !failed.compareAndSet(false, true)) return;
            errors.incrementAndGet();
            lastErrorAt.set(clock.instant());
            lastErrorType.set(throwable.getClass().getName());
        }

        @Override
        public void close() {
            lifecycle.readLock().lock();
            try {
                finishUnderLifecycleLock();
            } finally {
                lifecycle.readLock().unlock();
            }
        }

        private synchronized void finishUnderLifecycleLock() {
            if (!finished.compareAndSet(false, true)) return;
            if (active.remove(id, this)) activeCount.decrementAndGet();
            long elapsed = Math.max(0L, System.nanoTime() - startedNanos);
            totalDurationNanos.addAndGet(Math.min(elapsed, MAX_SPAN_DURATION_NANOS));
            completed.incrementAndGet();
        }

        @Override public String toString() {
            return "CpfTelemetrySpan[id=" + id + ",name=" + name + ",kind=" + kind
                    + ",attributes=" + attributes.size() + ",startedAt=" + startedAt + "]";
        }
    }
}
