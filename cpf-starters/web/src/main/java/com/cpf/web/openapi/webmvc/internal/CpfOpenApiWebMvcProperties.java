package com.cpf.web.openapi.webmvc.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.openapi.webmvc")
/** CpfOpenApiWebMvcProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfOpenApiWebMvcProperties {
    private boolean enabled = true;
    private boolean apiDocsEnabled;
    private boolean managementEnabled = true;
    private String apiDocsPath = "/v3/api-docs";
    private String title = "CPF API";
    private String version = "unspecified";
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private String description = "Core Platform Framework API";
    private String instanceId = "local";
    private Duration minimumRefreshInterval = Duration.ofSeconds(1);

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isApiDocsEnabled() { return apiDocsEnabled; }
    public void setApiDocsEnabled(boolean apiDocsEnabled) { this.apiDocsEnabled = apiDocsEnabled; }
    public boolean isManagementEnabled() { return managementEnabled; }
    public void setManagementEnabled(boolean managementEnabled) { this.managementEnabled = managementEnabled; }
    public String getApiDocsPath() { return apiDocsPath; }
    public void setApiDocsPath(String apiDocsPath) { this.apiDocsPath = requiredPath(apiDocsPath); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = required(title, "title"); }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = required(version, "version"); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description.trim(); }
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = required(instanceId, "instanceId"); }
    public Duration getMinimumRefreshInterval() { return minimumRefreshInterval; }
    public void setMinimumRefreshInterval(Duration minimumRefreshInterval) {
        if (minimumRefreshInterval == null || minimumRefreshInterval.isNegative()) {
            throw new IllegalArgumentException("minimumRefreshInterval must be >= 0");
        }
        this.minimumRefreshInterval = minimumRefreshInterval;
    }

    private static String requiredPath(String value) {
        String result = required(value, "apiDocsPath");
        if (!result.startsWith("/") || result.contains("..")) throw new IllegalArgumentException("unsafe apiDocsPath");
        return result;
    }
    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }
}
