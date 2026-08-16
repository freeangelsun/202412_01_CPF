package com.cpf.starter.data.transaction.jta;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** CPF JTA runtime 설정. 기본값은 비활성화하여 LOCAL 사용에 XA provider를 강제하지 않습니다. */
@ConfigurationProperties("cpf.data.transaction.jta")
public class CpfJtaProperties {
    private boolean enabled;
    private boolean standalone;
    private int defaultTimeoutSeconds = 30;
    private boolean startupRecovery = true;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isStandalone() { return standalone; }
    public void setStandalone(boolean standalone) { this.standalone = standalone; }
    public int getDefaultTimeoutSeconds() { return defaultTimeoutSeconds; }
    public void setDefaultTimeoutSeconds(int v) { if (v < 1) throw new IllegalArgumentException("defaultTimeoutSeconds must be positive"); defaultTimeoutSeconds = v; }
    public boolean isStartupRecovery() { return startupRecovery; }
    public void setStartupRecovery(boolean startupRecovery) { this.startupRecovery = startupRecovery; }
}
