package com.cpf.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ADM integration-closure runtime selection policy.
 *
 * <p>The feature is disabled by default. Ephemeral providers are intended only for local/dev
 * verification and must never be enabled for production state ownership.</p>
 */
@ConfigurationProperties(prefix = "cpf.adm.integration-closure")
public class AdmIntegrationClosureProperties {
    private boolean enabled;
    private boolean ephemeralProvidersEnabled;
    private Duration correctionApprovalTtl = Duration.ofMinutes(15);
    private final Webhook webhook = new Webhook();
    private final Crypto crypto = new Crypto();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEphemeralProvidersEnabled() { return ephemeralProvidersEnabled; }
    public void setEphemeralProvidersEnabled(boolean ephemeralProvidersEnabled) { this.ephemeralProvidersEnabled = ephemeralProvidersEnabled; }
    public Duration getCorrectionApprovalTtl() { return correctionApprovalTtl; }
    public void setCorrectionApprovalTtl(Duration correctionApprovalTtl) {
        if (correctionApprovalTtl == null || correctionApprovalTtl.isNegative() || correctionApprovalTtl.isZero()) {
            throw new IllegalArgumentException("correctionApprovalTtl must be positive");
        }
        this.correctionApprovalTtl = correctionApprovalTtl;
    }
    public Webhook getWebhook() { return webhook; }
    public Crypto getCrypto() { return crypto; }

    public static final class Webhook {
        private Set<String> allowedHosts = new LinkedHashSet<>();
        private int maxAttempts = 5;
        private Duration baseDelay = Duration.ofSeconds(1);
        public Set<String> getAllowedHosts() { return allowedHosts; }
        public void setAllowedHosts(Set<String> allowedHosts) { this.allowedHosts = allowedHosts == null ? new LinkedHashSet<>() : new LinkedHashSet<>(allowedHosts); }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) {
            if (maxAttempts < 1) throw new IllegalArgumentException("webhook.maxAttempts must be >= 1");
            this.maxAttempts = maxAttempts;
        }
        public Duration getBaseDelay() { return baseDelay; }
        public void setBaseDelay(Duration baseDelay) {
            if (baseDelay == null || baseDelay.isNegative() || baseDelay.isZero()) throw new IllegalArgumentException("webhook.baseDelay must be positive");
            this.baseDelay = baseDelay;
        }
    }

    public static final class Crypto {
        private boolean enabled;
        private String activeKeyVersion;
        private String activeKeyBase64;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getActiveKeyVersion() { return activeKeyVersion; }
        public void setActiveKeyVersion(String activeKeyVersion) { this.activeKeyVersion = activeKeyVersion; }
        public String getActiveKeyBase64() { return activeKeyBase64; }
        public void setActiveKeyBase64(String activeKeyBase64) { this.activeKeyBase64 = activeKeyBase64; }
    }
}
