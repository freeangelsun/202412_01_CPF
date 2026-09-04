package com.cpf.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ADM integration-closure provider policy.
 *
 * <p>Integration-closure routes are mandatory whenever ADM is composed. Only local/dev
 * ephemeral providers are selectable; protected runtimes must compose the real providers and
 * fail closed when they are absent.</p>
 */
@ConfigurationProperties(prefix = "cpf.adm.integration-closure")
/** AdmIntegrationClosureProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class AdmIntegrationClosureProperties {
    private boolean ephemeralProvidersEnabled = true;
    private Duration correctionApprovalTtl = Duration.ofMinutes(15);
    /** Shared 256-bit HMAC key for single-use correction execution proof. */
    /** Raw secret is local/dev compatibility only; prod/stg must use approvalProofKeyRef. */
    private String approvalProofKeyBase64;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private String approvalProofKeyRef;
    private final Webhook webhook = new Webhook();
    private final Crypto crypto = new Crypto();

    public boolean isEphemeralProvidersEnabled() { return ephemeralProvidersEnabled; }
    public void setEphemeralProvidersEnabled(boolean ephemeralProvidersEnabled) { this.ephemeralProvidersEnabled = ephemeralProvidersEnabled; }
    public Duration getCorrectionApprovalTtl() { return correctionApprovalTtl; }
    public String getApprovalProofKeyBase64() { return approvalProofKeyBase64; }
    public void setApprovalProofKeyBase64(String approvalProofKeyBase64) { this.approvalProofKeyBase64 = approvalProofKeyBase64; }
    public String getApprovalProofKeyRef() { return approvalProofKeyRef; }
    public void setApprovalProofKeyRef(String approvalProofKeyRef) { this.approvalProofKeyRef = approvalProofKeyRef; }
    public void setCorrectionApprovalTtl(Duration correctionApprovalTtl) {
        if (correctionApprovalTtl == null || correctionApprovalTtl.isNegative() || correctionApprovalTtl.isZero()) {
            throw new IllegalArgumentException("correctionApprovalTtl must be positive");
        }
        this.correctionApprovalTtl = correctionApprovalTtl;
    }
    public Webhook getWebhook() { return webhook; }
    public Crypto getCrypto() { return crypto; }

    /** Webhook 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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

    /** Crypto 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class Crypto {
        private boolean enabled;
        private String activeKeyVersion;
        /** Raw secret is local/dev compatibility only; prod/stg must use activeKeyRef. */
        private String activeKeyBase64;
        private String activeKeyRef;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getActiveKeyVersion() { return activeKeyVersion; }
        public void setActiveKeyVersion(String activeKeyVersion) { this.activeKeyVersion = activeKeyVersion; }
        public String getActiveKeyBase64() { return activeKeyBase64; }
        public void setActiveKeyBase64(String activeKeyBase64) { this.activeKeyBase64 = activeKeyBase64; }
        public String getActiveKeyRef() { return activeKeyRef; }
        public void setActiveKeyRef(String activeKeyRef) { this.activeKeyRef = activeKeyRef; }
    }
}
