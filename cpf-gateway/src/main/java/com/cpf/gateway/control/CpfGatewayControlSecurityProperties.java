package com.cpf.gateway.control;

import com.cpf.core.api.runtime.CpfInstanceIdentity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Gateway 내부 Control API의 활성화·서명·Audience·Key Rotation 정책입니다. */
@Component
@ConfigurationProperties(prefix = "cpf.gateway.control")
public class CpfGatewayControlSecurityProperties {
    private boolean enabled;
    private String audience = "";
    private String keyId = "current";
    private String sharedSecret = "";
    private String previousKeyId = "";
    private String previousSharedSecret = "";
    private long allowedSkewSeconds = 60;
    private long nonceRetentionSeconds = 300;
    private int maxBodyBytes = 1_048_576;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience == null ? "" : audience.trim(); }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId == null ? "" : keyId.trim(); }
    public String getSharedSecret() { return sharedSecret; }
    public void setSharedSecret(String sharedSecret) { this.sharedSecret = sharedSecret == null ? "" : sharedSecret; }
    public String getPreviousKeyId() { return previousKeyId; }
    public void setPreviousKeyId(String previousKeyId) { this.previousKeyId = previousKeyId == null ? "" : previousKeyId.trim(); }
    public String getPreviousSharedSecret() { return previousSharedSecret; }
    public void setPreviousSharedSecret(String previousSharedSecret) { this.previousSharedSecret = previousSharedSecret == null ? "" : previousSharedSecret; }
    public long getAllowedSkewSeconds() { return allowedSkewSeconds; }
    public void setAllowedSkewSeconds(long allowedSkewSeconds) { this.allowedSkewSeconds = allowedSkewSeconds; }
    public long getNonceRetentionSeconds() { return nonceRetentionSeconds; }
    public void setNonceRetentionSeconds(long nonceRetentionSeconds) { this.nonceRetentionSeconds = nonceRetentionSeconds; }
    public int getMaxBodyBytes() { return maxBodyBytes; }
    public void setMaxBodyBytes(int maxBodyBytes) { this.maxBodyBytes = maxBodyBytes; }

    public String resolvedAudience() {
        return audience == null || audience.isBlank()
                ? CpfInstanceIdentity.current().serverInstanceId()
                : audience.trim();
    }

    public String secretFor(String requestedKeyId) {
        if (keyId.equals(requestedKeyId)) return sharedSecret;
        if (!previousKeyId.isBlank() && previousKeyId.equals(requestedKeyId)) return previousSharedSecret;
        throw new SecurityException("허용되지 않은 Gateway Control keyId입니다.");
    }

    public void validate() {
        if (!enabled) return;
        if (keyId == null || keyId.isBlank()) throw new IllegalStateException("cpf.gateway.control.key-id가 필요합니다.");
        if (sharedSecret == null || sharedSecret.length() < 32) {
            throw new IllegalStateException("cpf.gateway.control.shared-secret은 32자 이상이어야 합니다.");
        }
        if (previousKeyId.isBlank() != previousSharedSecret.isBlank()) {
            throw new IllegalStateException("previous-key-id와 previous-shared-secret은 함께 설정해야 합니다.");
        }
        if (!previousSharedSecret.isBlank() && previousSharedSecret.length() < 32) {
            throw new IllegalStateException("previous-shared-secret은 32자 이상이어야 합니다.");
        }
        if (!previousKeyId.isBlank() && previousKeyId.equals(keyId)) {
            throw new IllegalStateException("current와 previous key-id는 달라야 합니다.");
        }
        if (allowedSkewSeconds < 5 || allowedSkewSeconds > 300) {
            throw new IllegalStateException("cpf.gateway.control.allowed-skew-seconds는 5~300 범위여야 합니다.");
        }
        if (nonceRetentionSeconds < allowedSkewSeconds || nonceRetentionSeconds > 3600) {
            throw new IllegalStateException("nonce 보존 시간은 시각 허용 범위 이상, 3600초 이하여야 합니다.");
        }
        if (maxBodyBytes < 0 || maxBodyBytes > 16 * 1024 * 1024) {
            throw new IllegalStateException("cpf.gateway.control.max-body-bytes는 0~16777216 범위여야 합니다.");
        }
        if (resolvedAudience().isBlank()) throw new IllegalStateException("Gateway Control audience를 결정할 수 없습니다.");
    }
}
