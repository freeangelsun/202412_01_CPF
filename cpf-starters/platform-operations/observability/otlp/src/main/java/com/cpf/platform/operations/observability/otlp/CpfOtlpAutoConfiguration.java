package com.cpf.platform.operations.observability.otlp;

import com.cpf.platform.operations.observability.api.CpfTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.api.common.AttributeKey;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CpfOtlpProperties.class)
@ConditionalOnProperty(prefix = "cpf.platform-operations.observability.otlp", name = "enabled", havingValue = "true")
public class CpfOtlpAutoConfiguration {
    @Bean(destroyMethod = "close")
    OtlpGrpcSpanExporter cpfOtlpSpanExporter(CpfOtlpProperties properties) {
        properties.validate();
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(properties.getEndpoint())
                .setTimeout(properties.getTimeout())
                .build();
    }

    @Bean(destroyMethod = "close")
    SdkTracerProvider cpfSdkTracerProvider(OtlpGrpcSpanExporter exporter, CpfOtlpProperties properties) {
        Resource resource = Resource.getDefault().merge(Resource.builder()
                .put(AttributeKey.stringKey("service.name"), properties.getServiceName())
                .put(AttributeKey.stringKey("service.version"), properties.getServiceVersion())
                .put(AttributeKey.stringKey("deployment.environment.name"), properties.getDeploymentEnvironmentName())
                .put(AttributeKey.stringKey("service.instance.id"), properties.getServiceInstanceId())
                .build());
        return SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(Sampler.traceIdRatioBased(properties.getSampleProbability()))
                .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(OpenTelemetry.class)
    OpenTelemetry cpfOpenTelemetry(SdkTracerProvider tracerProvider) {
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    @ConditionalOnMissingBean(CpfTelemetry.class)
    CpfTelemetry cpfTelemetry(OpenTelemetry openTelemetry, CpfOtlpProperties properties) {
        return new CpfOtlpTelemetry(openTelemetry.getTracer("com.cpf.platform.operations"), properties.getMaxAttributesPerSpan());
    }
}
