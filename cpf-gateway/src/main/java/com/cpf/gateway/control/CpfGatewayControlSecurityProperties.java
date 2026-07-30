package com.cpf.gateway.control;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Gateway 내부 Control API의 활성화와 서명 검증 정책입니다. */
@Component
@ConfigurationProperties(prefix = "cpf.gateway.control")
public class CpfGatewayControlSecurityProperties {
    private boolean enabled;
    private String sharedSecret = "";
    private long allowedSkewSeconds = 60;
    private long nonceRetentionSeconds = 300;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getSharedSecret() { return sharedSecret; }
    public void setSharedSecret(String sharedSecret) { this.sharedSecret = sharedSecret == null ? "" : sharedSecret; }
    public long getAllowedSkewSeconds() { return allowedSkewSeconds; }
    public void setAllowedSkewSeconds(long allowedSkewSeconds) { this.allowedSkewSeconds = allowedSkewSeconds; }
    public long getNonceRetentionSeconds() { return nonceRetentionSeconds; }
    public void setNonceRetentionSeconds(long nonceRetentionSeconds) { this.nonceRetentionSeconds = nonceRetentionSeconds; }

    public void validate() {
        if (!enabled) return;
        if (sharedSecret == null || sharedSecret.length() < 32) {
            throw new IllegalStateException("cpf.gateway.control.shared-secret은 32자 이상이어야 합니다.");
        }
        if (allowedSkewSeconds < 5 || allowedSkewSeconds > 300) {
            throw new IllegalStateException("cpf.gateway.control.allowed-skew-seconds는 5~300 범위여야 합니다.");
        }
        if (nonceRetentionSeconds < allowedSkewSeconds || nonceRetentionSeconds > 3600) {
            throw new IllegalStateException("nonce 보존 시간은 시각 허용 범위 이상, 3600초 이하여야 합니다.");
        }
    }
}
