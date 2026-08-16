package com.cpf.data.lock.valkey;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Valkey Lock Provider 설정. */
@ConfigurationProperties("cpf.data.lock.valkey")
public class CpfValkeyLockProperties {
    private boolean enabled;
    private String namespace = "cpf";
    private int casRetries = 16;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public int getCasRetries() { return casRetries; }
    public void setCasRetries(int casRetries) { this.casRetries = casRetries; }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate() {
        if (namespace == null || namespace.isBlank()) throw new IllegalStateException("cpf.data.lock.valkey.namespace must not be blank");
        if (casRetries < 1 || casRetries > 128) throw new IllegalStateException("cpf.data.lock.valkey.cas-retries must be 1..128");
    }
}
