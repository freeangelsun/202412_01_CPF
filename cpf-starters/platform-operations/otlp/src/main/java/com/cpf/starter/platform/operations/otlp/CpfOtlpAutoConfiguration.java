package com.cpf.starter.platform.operations.otlp;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@EnableConfigurationProperties(CpfOtlpProperties.class)
@ConditionalOnProperty(prefix="cpf.platform-operations.otlp",name="enabled",havingValue="true")
public class CpfOtlpAutoConfiguration {
 @Bean(destroyMethod="close") OtlpGrpcSpanExporter cpfOtlpSpanExporter(CpfOtlpProperties p){p.validate();return OtlpGrpcSpanExporter.builder().setEndpoint(p.getEndpoint()).setTimeout(p.getTimeout()).build();}
 @Bean(destroyMethod="close") SdkTracerProvider cpfSdkTracerProvider(OtlpGrpcSpanExporter e,CpfOtlpProperties p){return SdkTracerProvider.builder().setSampler(Sampler.traceIdRatioBased(p.getSampleProbability())).addSpanProcessor(BatchSpanProcessor.builder(e).build()).build();}
 @Bean OpenTelemetry cpfOpenTelemetry(SdkTracerProvider p){return OpenTelemetrySdk.builder().setTracerProvider(p).build();}
}
