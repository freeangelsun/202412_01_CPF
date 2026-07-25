package com.cpf.core.service.observability;

import com.cpf.core.api.observability.CpfTelemetry;

import java.util.Map;

final class CpfNoopTelemetry implements CpfTelemetry {
    private static final CpfTelemetrySpan SPAN = new CpfTelemetrySpan() {
        @Override public void error(Throwable throwable) { }
        @Override public void close() { }
    };

    @Override
    public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
        return SPAN;
    }

    @Override
    public Map<String, Object> status() {
        return Map.of("enabled", false, "provider", "CPF_NOOP");
    }
}
