package com.cpf.web.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** CPF Controller 개발정책 Runtime 설정입니다. */
@ConfigurationProperties("cpf.web.controller-policy")
public class CpfControllerPolicyProperties {
    private boolean enabled = true;
    private boolean requireBaseClass = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isRequireBaseClass() { return requireBaseClass; }
    public void setRequireBaseClass(boolean requireBaseClass) { this.requireBaseClass = requireBaseClass; }
}
