package com.cpf.platform.operations.observability.otlp;
import com.cpf.platform.operations.observability.api.CpfTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@ConditionalOnProperty(name="cpf.observability.otel.enabled",havingValue="true")
public class CpfOtlpTelemetryAutoConfiguration {
 @Bean(destroyMethod="close") @ConditionalOnMissingBean SdkTracerProvider cpfSdkTracerProvider(@Value("${cpf.observability.otel.endpoint:http://127.0.0.1:4317}") String endpoint){var exporter=OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();return SdkTracerProvider.builder().addSpanProcessor(BatchSpanProcessor.builder(exporter).build()).build();}
 @Bean @ConditionalOnMissingBean(OpenTelemetry.class) OpenTelemetry cpfOpenTelemetry(SdkTracerProvider provider){return OpenTelemetrySdk.builder().setTracerProvider(provider).build();}
 @Bean @ConditionalOnMissingBean(CpfTelemetry.class) CpfTelemetry cpfOtlpTelemetry(OpenTelemetry telemetry,@Value("${cpf.observability.otel.endpoint:http://127.0.0.1:4317}") String endpoint){return new CpfOtlpTelemetryAdapter(telemetry.getTracer("com.cpf"),endpoint);}
}
