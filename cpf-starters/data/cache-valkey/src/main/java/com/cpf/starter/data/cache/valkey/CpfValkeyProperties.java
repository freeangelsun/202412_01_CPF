package com.cpf.starter.data.cache.valkey;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.data.cache.valkey")
public class CpfValkeyProperties {
    private boolean enabled;
    private String keyPrefix = "cpf:";
    private String invalidationChannel = "cpf.cache.invalidate";
    private Duration defaultTtl = Duration.ofMinutes(10);
    private boolean required = true;
    private boolean tls;
    private long maximumPayloadBytes = 1_048_576;
    private long scanCount = 500;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getInvalidationChannel() { return invalidationChannel; }
    public void setInvalidationChannel(String invalidationChannel) { this.invalidationChannel = invalidationChannel; }
    public Duration getDefaultTtl() { return defaultTtl; }
    public void setDefaultTtl(Duration defaultTtl) { this.defaultTtl = defaultTtl; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public boolean isTls() { return tls; }
    public void setTls(boolean tls) { this.tls = tls; }
    public long getMaximumPayloadBytes() { return maximumPayloadBytes; }
    public void setMaximumPayloadBytes(long maximumPayloadBytes) { this.maximumPayloadBytes = maximumPayloadBytes; }
    public long getScanCount() { return scanCount; }
    public void setScanCount(long scanCount) { this.scanCount = scanCount; }

    public void validate() {
        if (!enabled) {
            return;
        }
        if (keyPrefix == null || keyPrefix.isBlank() || invalidationChannel == null || invalidationChannel.isBlank()) {
            throw new IllegalStateException("Valkey key-prefix and invalidation-channel are required");
        }
        if (!keyPrefix.matches("[A-Za-z0-9._:-]{1,120}")) {
            throw new IllegalStateException("Valkey key-prefix format is invalid");
        }
        if (!invalidationChannel.matches("[A-Za-z0-9._:-]{1,180}")) {
            throw new IllegalStateException("Valkey invalidation-channel format is invalid");
        }
        if (defaultTtl == null || defaultTtl.isNegative() || defaultTtl.isZero()) {
            throw new IllegalStateException("Valkey default-ttl must be positive");
        }
        if (maximumPayloadBytes < 1 || maximumPayloadBytes > 64L * 1024 * 1024) {
            throw new IllegalStateException("Valkey maximum-payload-bytes must be between 1 byte and 64 MiB");
        }
        if (scanCount < 10 || scanCount > 10_000) {
            throw new IllegalStateException("Valkey scan-count must be between 10 and 10000");
        }
    }
}
