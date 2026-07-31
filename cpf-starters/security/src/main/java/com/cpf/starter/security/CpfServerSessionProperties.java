package com.cpf.starter.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.security.session")
public record CpfServerSessionProperties(
        String cookieName, Duration timeout, boolean secure, String sameSite, String cookiePath) {
    public CpfServerSessionProperties {
        cookieName = blank(cookieName) ? "CPFSESSION" : cookieName.trim();
        timeout = timeout == null ? Duration.ofMinutes(30) : timeout;
        sameSite = blank(sameSite) ? "Strict" : sameSite.trim();
        cookiePath = blank(cookiePath) ? "/" : cookiePath.trim();
        if (timeout.isNegative() || timeout.isZero() || timeout.compareTo(Duration.ofHours(12)) > 0) {
            throw new IllegalArgumentException("CPF session timeout must be between 1 second and 12 hours.");
        }
        if (!(sameSite.equals("Strict") || sameSite.equals("Lax"))) {
            throw new IllegalArgumentException("CPF privileged console session SameSite must be Strict or Lax.");
        }
    }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
