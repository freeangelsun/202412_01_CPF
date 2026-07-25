package com.cpf.core.service.observability;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfNoopTelemetryTest {
    @Test
    void noopNeverFailsBusinessFlow() {
        CpfNoopTelemetry telemetry = new CpfNoopTelemetry();
        try (var span = telemetry.startSpan("test", "SERVER", Map.of("password", "must-not-export"))) {
            span.error(new IllegalStateException("test"));
        }
        assertThat(telemetry.status()).containsEntry("enabled", false);
    }
}
