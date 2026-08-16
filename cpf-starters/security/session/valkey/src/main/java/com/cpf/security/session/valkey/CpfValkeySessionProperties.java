package com.cpf.security.session.valkey;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Valkey Session Provider 설정. */
@ConfigurationProperties("cpf.security.session.valkey")
public class CpfValkeySessionProperties {
    private boolean enabled;
    private String keyPrefix = "cpf:session:";
    private Duration defaultTtl = Duration.ofMinutes(30);
    private int maxConcurrentSessions = 5;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public Duration getDefaultTtl() { return defaultTtl; }
    public void setDefaultTtl(Duration defaultTtl) { this.defaultTtl = defaultTtl; }
    public int getMaxConcurrentSessions() { return maxConcurrentSessions; }
    public void setMaxConcurrentSessions(int maxConcurrentSessions) { this.maxConcurrentSessions = maxConcurrentSessions; }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate() {
        if (keyPrefix == null || keyPrefix.isBlank()) throw new IllegalStateException("cpf.security.session.valkey.key-prefix must not be blank");
        if (defaultTtl == null || defaultTtl.isNegative() || defaultTtl.isZero()) throw new IllegalStateException("cpf.security.session.valkey.default-ttl must be positive");
        if (maxConcurrentSessions < 1 || maxConcurrentSessions > 100) throw new IllegalStateException("cpf.security.session.valkey.max-concurrent-sessions must be 1..100");
    }
}
