package com.cpf.starter.data.persistence.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.data.persistence.jdbc")
public class CpfJdbcStarterProperties {
    private boolean enabled = true;
    private boolean required = true;
    private String validationQuery = "SELECT 1";
    private int validationTimeoutSeconds = 5;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public String getValidationQuery() { return validationQuery; }
    public void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }
    public int getValidationTimeoutSeconds() { return validationTimeoutSeconds; }
    public void setValidationTimeoutSeconds(int validationTimeoutSeconds) { this.validationTimeoutSeconds = validationTimeoutSeconds; }

    public void validate() {
        if (validationTimeoutSeconds < 1 || validationTimeoutSeconds > 60) throw new IllegalStateException("validation-timeout-seconds must be 1..60");
        if (validationQuery == null || validationQuery.isBlank()) throw new IllegalStateException("validation-query is required");
    }
}
