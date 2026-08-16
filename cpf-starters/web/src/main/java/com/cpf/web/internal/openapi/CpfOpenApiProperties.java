package com.cpf.web.internal.openapi;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** CPF Web Profile 내부 OpenAPI 운영 Runtime 설정입니다. */
@ConfigurationProperties("cpf.openapi.webmvc")
public class CpfOpenApiProperties {
    private boolean enabled = true;
    private boolean apiDocsEnabled = true;
    private String apiDocsPath = "/v3/api-docs";
    private String instanceId = "local";
    private Duration minimumRefreshInterval = Duration.ofSeconds(1);
    public boolean isEnabled(){ return enabled; }
    public void setEnabled(boolean enabled){ this.enabled=enabled; }
    public boolean isApiDocsEnabled(){ return apiDocsEnabled; }
    public void setApiDocsEnabled(boolean apiDocsEnabled){ this.apiDocsEnabled=apiDocsEnabled; }
    public String getApiDocsPath(){ return apiDocsPath; }
    public void setApiDocsPath(String value){
        if(value==null||value.isBlank()||!value.trim().startsWith("/")||value.contains("..")) throw new IllegalArgumentException("unsafe apiDocsPath");
        this.apiDocsPath=value.trim();
    }
    public String getInstanceId(){ return instanceId; }
    public void setInstanceId(String value){ if(value==null||value.isBlank())throw new IllegalArgumentException("instanceId is required");this.instanceId=value.trim(); }
    public Duration getMinimumRefreshInterval(){ return minimumRefreshInterval; }
    public void setMinimumRefreshInterval(Duration value){ if(value==null||value.isNegative())throw new IllegalArgumentException("minimumRefreshInterval must be >= 0");this.minimumRefreshInterval=value; }
}
