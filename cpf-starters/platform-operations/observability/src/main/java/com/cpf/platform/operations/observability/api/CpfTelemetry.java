package com.cpf.platform.operations.observability.api;

import java.util.Map;
import java.util.Objects;

/**
 * CPF vendor-neutral telemetry contract.
 *
 * <p>OpenTelemetry or other provider SDK types are never exposed from this public API. Implementations
 * must bound resources, sanitize attributes, preserve transaction correlation, and make span close
 * idempotent.</p>
 */
/** CpfTelemetry 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfTelemetry {
    CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes);

    default CpfTelemetrySpan startSpan(CpfTraceContext context) {
        Objects.requireNonNull(context, "context");
        return startSpan(context.spanName(), context.kind().name(), context.attributes());
    }

    Map<String, Object> status();

    static CpfTelemetry noop() {
        return NoopHolder.INSTANCE;
    }

    /** CpfTelemetrySpan 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    interface CpfTelemetrySpan extends AutoCloseable {
        void error(Throwable throwable);
        @Override void close();
    }

    final class NoopHolder {
        private static final CpfTelemetry INSTANCE = new CpfTelemetry() {
            private final CpfTelemetrySpan span = new CpfTelemetrySpan() {
                @Override public void error(Throwable throwable) { }
                @Override public void close() { }
            };
            @Override public CpfTelemetrySpan startSpan(
                    String name, String kind, Map<String, String> attributes) {
                return span;
            }
            @Override public Map<String, Object> status() {
                return Map.of("state", "NOOP", "activeSpans", 0L, "completedSpans", 0L,
                        "errorSpans", 0L, "rejectedSpans", 0L);
            }
        };
        private NoopHolder() { }
    }
}
