package com.cpf.platform.operations.observability.otlp;

import com.cpf.platform.operations.observability.api.CpfTelemetry;
import com.cpf.platform.operations.observability.api.CpfTraceContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

/** CPF vendor-neutral telemetry contract backed by OpenTelemetry. */
public final class CpfOtlpTelemetry implements CpfTelemetry {
    private static final int MAX_ATTRIBUTE_VALUE = 256;
    private final Tracer tracer;
    private final int maxAttributes;
    private final AtomicLong active = new AtomicLong();
    private final AtomicLong completed = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();

    public CpfOtlpTelemetry(Tracer tracer, int maxAttributes) {
        this.tracer = Objects.requireNonNull(tracer, "tracer");
        if (maxAttributes < 1) throw new IllegalArgumentException("maxAttributes must be positive");
        this.maxAttributes = maxAttributes;
    }

    @Override
    public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
        String spanName = require(name, "name", 96);
        Map<String, String> safe = sanitize(attributes);
        SpanBuilder builder = tracer.spanBuilder(spanName);
        applyKind(builder, kind);
        safe.forEach(builder::setAttribute);
        return wrap(builder.startSpan());
    }

    @Override
    public CpfTelemetrySpan startSpan(CpfTraceContext context) {
        Objects.requireNonNull(context, "context");
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>(context.attributes());
        // Keep the CPF semantic IDs as explicit attributes even if the OTel SDK allocates native span IDs.
        attributes.put("cpf.transaction_id", context.attributes().get("cpf.transaction_id"));
        attributes.put("cpf.trace_id", context.traceId());
        attributes.put("cpf.span_id", context.spanId());
        if (context.parentSpanId() != null) attributes.put("cpf.parent_span_id", context.parentSpanId());
        if (context.segmentId() != null) attributes.put("cpf.segment_id", bounded(context.segmentId()));
        attributes.put("cpf.attempt", Integer.toString(context.attempt()));
        return startSpan(context.spanName(), context.kind().name(), attributes);
    }

    @Override
    public Map<String, Object> status() {
        return Map.of(
                "state", "ACTIVE",
                "activeSpans", active.get(),
                "completedSpans", completed.get(),
                "errorSpans", errors.get(),
                "rejectedSpans", rejected.get());
    }

    private CpfTelemetrySpan wrap(Span span) {
        active.incrementAndGet();
        return new OtlpSpan(span);
    }

    private Map<String, String> sanitize(Map<String, String> source) {
        if (source == null || source.isEmpty()) return Map.of();
        if (source.size() > maxAttributes) {
            rejected.incrementAndGet();
            throw new IllegalArgumentException("too many telemetry attributes");
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            String safeKey = require(key, "attribute key", 96).toLowerCase(Locale.ROOT);
            if (safeKey.contains("password") || safeKey.contains("secret") || safeKey.contains("authorization") || safeKey.contains("cookie")) {
                rejected.incrementAndGet();
                throw new IllegalArgumentException("sensitive telemetry attribute is forbidden: " + safeKey);
            }
            result.put(safeKey, bounded(require(value, "attribute value", 1024)));
        });
        return Map.copyOf(result);
    }

    private static String bounded(String value) {
        return value.length() <= MAX_ATTRIBUTE_VALUE ? value : value.substring(0, MAX_ATTRIBUTE_VALUE);
    }

    private static String require(String value, String name, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String trimmed = value.trim();
        if (trimmed.length() > max) throw new IllegalArgumentException(name + " is too long");
        if (trimmed.chars().anyMatch(Character::isISOControl)) throw new IllegalArgumentException(name + " contains control characters");
        return trimmed;
    }

    private static void applyKind(SpanBuilder builder, String kind) {
        String normalized = kind == null ? "LOCAL" : kind.trim().toUpperCase(Locale.ROOT);
        io.opentelemetry.api.trace.SpanKind spanKind = switch (normalized) {
            case "REMOTE" -> io.opentelemetry.api.trace.SpanKind.CLIENT;
            case "MESSAGE" -> io.opentelemetry.api.trace.SpanKind.CONSUMER;
            case "BATCH", "FILE", "LOCAL" -> io.opentelemetry.api.trace.SpanKind.INTERNAL;
            default -> throw new IllegalArgumentException("unsupported CPF span kind: " + kind);
        };
        builder.setSpanKind(spanKind);
    }

    private final class OtlpSpan implements CpfTelemetrySpan {
        private final Span span;
        private final AtomicBoolean closed = new AtomicBoolean();
        private OtlpSpan(Span span) { this.span = Objects.requireNonNull(span, "span"); }
        @Override public void error(Throwable throwable) {
            if (throwable == null || closed.get()) return;
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR);
            errors.incrementAndGet();
        }
        @Override public void close() {
            if (!closed.compareAndSet(false, true)) return;
            try { span.end(); } finally { active.decrementAndGet(); completed.incrementAndGet(); }
        }
    }
}
