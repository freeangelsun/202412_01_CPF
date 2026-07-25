package com.cpf.core.service.observability;

import com.cpf.core.api.observability.CpfTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** OpenTelemetry SDK adapter. Export 실패가 업무 Exception으로 역전파되지 않도록 span API를 방어적으로 감쌉니다. */
final class CpfOpenTelemetryAdapter implements CpfTelemetry {
    private final Tracer tracer;
    private final String endpoint;
    private final AtomicLong started = new AtomicLong();
    private final AtomicLong ended = new AtomicLong();
    private final AtomicLong instrumentationFailures = new AtomicLong();

    CpfOpenTelemetryAdapter(Tracer tracer, String endpoint) {
        this.tracer = tracer;
        this.endpoint = endpoint;
    }

    @Override
    public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
        try {
            Span span = tracer.spanBuilder(name)
                    .setSpanKind(toKind(kind))
                    .startSpan();
            if (attributes != null) {
                attributes.forEach((key, value) -> {
                    if (key != null && value != null && !isSensitiveKey(key)) span.setAttribute(key, value);
                });
            }
            Scope scope = span.makeCurrent();
            started.incrementAndGet();
            return new CpfTelemetrySpan() {
                private boolean closed;
                @Override
                public void error(Throwable throwable) {
                    if (throwable != null) {
                        span.recordException(throwable);
                        span.setStatus(StatusCode.ERROR);
                    }
                }
                @Override
                public void close() {
                    if (closed) return;
                    closed = true;
                    try {
                        scope.close();
                        span.end();
                        ended.incrementAndGet();
                    } catch (RuntimeException ex) {
                        instrumentationFailures.incrementAndGet();
                    }
                }
            };
        } catch (RuntimeException ex) {
            instrumentationFailures.incrementAndGet();
            return new CpfNoopTelemetry().startSpan(name, kind, attributes);
        }
    }

    @Override
    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", true);
        status.put("provider", "OPENTELEMETRY");
        status.put("endpoint", endpoint);
        status.put("startedSpanCount", started.get());
        status.put("endedSpanCount", ended.get());
        status.put("instrumentationFailureCount", instrumentationFailures.get());
        return status;
    }

    private SpanKind toKind(String kind) {
        if (kind == null) return SpanKind.INTERNAL;
        try {
            return SpanKind.valueOf(kind.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SpanKind.INTERNAL;
        }
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key.toLowerCase().replace("-", "").replace("_", "");
        return normalized.contains("password") || normalized.contains("secret")
                || normalized.contains("token") || normalized.contains("authorization")
                || normalized.contains("cookie") || normalized.contains("account")
                || normalized.contains("memberno") || normalized.contains("customerno");
    }
}
