package com.cpf.starter.security;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/** ADM/BZA privileged console의 Session·Credential·CSRF 정책입니다. */
@ConfigurationProperties("cpf.security.session")
public record CpfServerSessionProperties(
        boolean enabled,
        String cookieName,
        Duration timeout,
        boolean secure,
        String sameSite,
        String cookiePath,
        List<String> allowedOrigins,
        String credentialKeyBase64,
        String credentialKeyId,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        int maxSessions,
        String contentSecurityPolicy) {

    /** 기존 5개 인자 생성자와의 Source 호환을 유지합니다. */
    public CpfServerSessionProperties(
            String cookieName, Duration timeout, boolean secure, String sameSite, String cookiePath) {
        this(true, cookieName, timeout, secure, sameSite, cookiePath, null, null, null,
                null, null, 1, null);
    }

    /** QA32 단계의 12개 인자 생성자와의 Source 호환을 유지합니다. */
    public CpfServerSessionProperties(
            boolean enabled,
            String cookieName,
            Duration timeout,
            boolean secure,
            String sameSite,
            String cookiePath,
            List<String> allowedOrigins,
            String credentialKeyBase64,
            String credentialKeyId,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            int maxSessions) {
        this(enabled, cookieName, timeout, secure, sameSite, cookiePath, allowedOrigins,
                credentialKeyBase64, credentialKeyId, accessTokenTtl, refreshTokenTtl,
                maxSessions, null);
    }

    @ConstructorBinding
    public CpfServerSessionProperties {
        cookieName = blank(cookieName) ? "CPFSESSION" : cookieName.trim();
        timeout = timeout == null ? Duration.ofMinutes(30) : timeout;
        sameSite = blank(sameSite) ? "Strict" : sameSite.trim();
        cookiePath = blank(cookiePath) ? "/" : cookiePath.trim();
        allowedOrigins = allowedOrigins == null
                ? List.of()
                : allowedOrigins.stream().filter(value -> !blank(value)).map(String::trim).distinct().toList();
        credentialKeyId = blank(credentialKeyId) ? "cpf-bff-v1" : credentialKeyId.trim();
        accessTokenTtl = accessTokenTtl == null ? Duration.ofMinutes(5) : accessTokenTtl;
        refreshTokenTtl = refreshTokenTtl == null ? Duration.ofMinutes(30) : refreshTokenTtl;
        maxSessions = maxSessions <= 0 ? 1 : maxSessions;
        contentSecurityPolicy = blank(contentSecurityPolicy)
                ? "default-src 'self'; base-uri 'self'; frame-ancestors 'none'; object-src 'none'; "
                        + "form-action 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
                        + "connect-src 'self'"
                : contentSecurityPolicy.trim();

        if (timeout.isNegative() || timeout.isZero() || timeout.compareTo(Duration.ofHours(12)) > 0) {
            throw new IllegalArgumentException("CPF session timeout must be between 1 second and 12 hours.");
        }
        if (!(sameSite.equals("Strict") || sameSite.equals("Lax"))) {
            throw new IllegalArgumentException("CPF privileged console Session SameSite must be Strict or Lax.");
        }
        if (accessTokenTtl.isNegative() || accessTokenTtl.isZero()
                || accessTokenTtl.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalArgumentException("CPF BFF access token TTL must be between 1 second and 1 hour.");
        }
        if (refreshTokenTtl.compareTo(accessTokenTtl) < 0
                || refreshTokenTtl.compareTo(Duration.ofHours(24)) > 0) {
            throw new IllegalArgumentException("CPF BFF refresh token TTL must be between access TTL and 24 hours.");
        }
        if (maxSessions > 20) {
            throw new IllegalArgumentException("CPF privileged console maxSessions must not exceed 20.");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
