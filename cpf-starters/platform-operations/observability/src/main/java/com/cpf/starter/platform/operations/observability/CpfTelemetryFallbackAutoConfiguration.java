package com.cpf.starter.platform.operations.observability;

import com.cpf.platform.operations.observability.api.CpfTelemetry;
import com.cpf.starter.platform.operations.observability.internal.telemetry.CpfNoopTelemetry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Provider-neutral fallback only; OTLP provider owns OpenTelemetry SDK/exporter wiring. */
@AutoConfiguration
public class CpfTelemetryFallbackAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfTelemetry.class)
    CpfTelemetry cpfNoopTelemetry() {
        return new CpfNoopTelemetry();
    }
}
