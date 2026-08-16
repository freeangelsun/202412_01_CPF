package com.cpf.platform.operations.observability.otlp;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** OTLP exporter safe-default configuration. */
@ConfigurationProperties("cpf.platform-operations.observability.otlp")
/** CpfOtlpProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfOtlpProperties {
    private boolean enabled;
    private String endpoint;
    private Duration timeout = Duration.ofSeconds(10);
    private double sampleProbability = 1.0;
    private String serviceName = "cpf-application";
    /** Telemetry Resource에 기록할 서비스 배포 버전입니다. */
    private String serviceVersion = "unknown";
    /** Telemetry Resource에 기록할 배포 환경 식별자입니다. */
    private String deploymentEnvironmentName = "unknown";
    /** Telemetry Resource에 기록할 실행 인스턴스 식별자입니다. */
    private String serviceInstanceId = "unknown";
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int maxAttributesPerSpan = 32;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
    public double getSampleProbability() { return sampleProbability; }
    public void setSampleProbability(double sampleProbability) { this.sampleProbability = sampleProbability; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getServiceVersion() { return serviceVersion; }
    public void setServiceVersion(String serviceVersion) { this.serviceVersion = serviceVersion; }
    public String getDeploymentEnvironmentName() { return deploymentEnvironmentName; }
    public void setDeploymentEnvironmentName(String deploymentEnvironmentName) { this.deploymentEnvironmentName = deploymentEnvironmentName; }
    public String getServiceInstanceId() { return serviceInstanceId; }
    public void setServiceInstanceId(String serviceInstanceId) { this.serviceInstanceId = serviceInstanceId; }
    public int getMaxAttributesPerSpan() { return maxAttributesPerSpan; }
    public void setMaxAttributesPerSpan(int maxAttributesPerSpan) { this.maxAttributesPerSpan = maxAttributesPerSpan; }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate() {
        if (!enabled) return;
        if (endpoint == null || endpoint.isBlank()) throw new IllegalStateException("OTLP endpoint is required");
        if (timeout == null || timeout.isZero() || timeout.isNegative()) throw new IllegalStateException("OTLP timeout must be positive");
        if (sampleProbability < 0.0 || sampleProbability > 1.0) throw new IllegalStateException("sample-probability must be 0..1");
        if (serviceName == null || serviceName.isBlank()) throw new IllegalStateException("service-name is required");
        if (serviceVersion == null || serviceVersion.isBlank()) throw new IllegalStateException("service-version is required");
        if (deploymentEnvironmentName == null || deploymentEnvironmentName.isBlank()) throw new IllegalStateException("deployment-environment-name is required");
        if (serviceInstanceId == null || serviceInstanceId.isBlank()) throw new IllegalStateException("service-instance-id is required");
        if (maxAttributesPerSpan < 8 || maxAttributesPerSpan > 128) throw new IllegalStateException("max-attributes-per-span must be 8..128");
    }
}
