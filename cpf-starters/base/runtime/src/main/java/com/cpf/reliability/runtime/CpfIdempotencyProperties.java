package com.cpf.reliability.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** CPF 멱등 Annotation Runtime 설정입니다. */
@ConfigurationProperties("cpf.reliability.idempotency")
public class CpfIdempotencyProperties {
    private boolean enabled = true;
    private boolean failClosed = true;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isFailClosed() { return failClosed; }
    public void setFailClosed(boolean failClosed) { this.failClosed = failClosed; }
}
