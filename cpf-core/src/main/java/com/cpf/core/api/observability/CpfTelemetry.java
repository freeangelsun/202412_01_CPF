package com.cpf.core.api.observability;

import java.util.Map;

/** CPF vendor-neutral telemetry contract. OTel SDK type을 Public API에 노출하지 않습니다. */
public interface CpfTelemetry {
    CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes);

    Map<String, Object> status();

    interface CpfTelemetrySpan extends AutoCloseable {
        void error(Throwable throwable);
        @Override void close();
    }
}
