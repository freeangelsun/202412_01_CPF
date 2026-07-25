package com.cpf.core.service.observability;

import com.cpf.core.api.observability.CpfTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OTel은 명시적으로 활성화한 환경에서만 OTLP exporter를 생성합니다. */
@Configuration
public class CpfTelemetryConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "cpf.observability.otel.enabled", havingValue = "true")
    SdkTracerProvider cpfSdkTracerProvider(
            @Value("${cpf.observability.otel.endpoint:http://127.0.0.1:4317}") String endpoint) {
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
        return SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "cpf.observability.otel.enabled", havingValue = "true")
    OpenTelemetry cpfOpenTelemetry(SdkTracerProvider tracerProvider) {
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    @ConditionalOnProperty(name = "cpf.observability.otel.enabled", havingValue = "true")
    CpfTelemetry cpfOpenTelemetryAdapter(
            OpenTelemetry openTelemetry,
            @Value("${cpf.observability.otel.endpoint:http://127.0.0.1:4317}") String endpoint) {
        return new CpfOpenTelemetryAdapter(openTelemetry.getTracer("com.cpf"), endpoint);
    }

    @Bean
    @ConditionalOnMissingBean(CpfTelemetry.class)
    CpfTelemetry cpfNoopTelemetry() {
        return new CpfNoopTelemetry();
    }
}
