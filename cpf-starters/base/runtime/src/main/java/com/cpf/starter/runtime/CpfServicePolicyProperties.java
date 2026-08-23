package com.cpf.starter.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code @CpfService} Runtime 정책 설정입니다. */
@ConfigurationProperties("cpf.service-policy")
public class CpfServicePolicyProperties {
    private boolean enabled = true;
    private boolean requireBaseClass = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isRequireBaseClass() { return requireBaseClass; }
    public void setRequireBaseClass(boolean requireBaseClass) { this.requireBaseClass = requireBaseClass; }
}
