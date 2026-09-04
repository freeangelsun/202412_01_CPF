package com.cpf.backoffice.web.shared.config;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "backoffice.web")
public record BackofficeWebProperties(
        Mode mode,
        URI gatewayBaseUri,
        URI directBaseUri,
        String targetSystemCode,
        String channelCode,
        Duration connectTimeout,
        Duration requestTimeout,
        String accessCookieName,
        String refreshCookieName,
        boolean secureCookies,
        String cookieSameSite) {

    public enum Mode { GATEWAY, DIRECT }

    public BackofficeWebProperties {
        mode = mode == null ? Mode.GATEWAY : mode;
        targetSystemCode = systemCode(targetSystemCode, "MBW");
        channelCode = systemCode(channelCode, "MBW");
        connectTimeout = positive(connectTimeout, Duration.ofSeconds(3));
        requestTimeout = positive(requestTimeout, Duration.ofSeconds(10));
        accessCookieName = cookieName(accessCookieName, "CPF_MBW_ACCESS");
        refreshCookieName = cookieName(refreshCookieName, "CPF_MBW_REFRESH");
        cookieSameSite = sameSite(cookieSameSite);
        URI selected = mode == Mode.GATEWAY ? gatewayBaseUri : directBaseUri;
        if (selected == null || !selected.isAbsolute()) {
            throw new IllegalArgumentException("Backoffice Web " + mode + " base URI must be absolute");
        }
        String scheme = selected.getScheme().toLowerCase(Locale.ROOT);
        if (!(scheme.equals("http") || scheme.equals("https"))) {
            throw new IllegalArgumentException("Backoffice Web upstream scheme must be http/https");
        }
    }

    public URI selectedBaseUri() { return mode == Mode.GATEWAY ? gatewayBaseUri : directBaseUri; }

    private static String systemCode(String value, String fallback) {
        String normalized = token(value, fallback, 3).toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9]{3}")) throw new IllegalArgumentException("systemCode must be 3 characters");
        return normalized;
    }
    private static String token(String value, String fallback, int max) {
        String v = value == null || value.isBlank() ? fallback : value.trim();
        if (v.length() > max || v.indexOf('\r') >= 0 || v.indexOf('\n') >= 0) throw new IllegalArgumentException("invalid Backoffice Web token");
        return v;
    }
    private static String cookieName(String value, String fallback) {
        String v = token(value, fallback, 64);
        if (!v.matches("[A-Za-z0-9_.-]+")) throw new IllegalArgumentException("invalid Backoffice Web cookie name");
        return v;
    }
    private static String sameSite(String value) {
        String v = token(value, "Strict", 16);
        if (!(v.equalsIgnoreCase("Strict") || v.equalsIgnoreCase("Lax") || v.equalsIgnoreCase("None"))) {
            throw new IllegalArgumentException("cookie SameSite must be Strict, Lax, or None");
        }
        return Character.toUpperCase(v.charAt(0)) + v.substring(1).toLowerCase(Locale.ROOT);
    }
    private static Duration positive(Duration value, Duration fallback) {
        Duration v = value == null ? fallback : value;
        if (v.isZero() || v.isNegative()) throw new IllegalArgumentException("timeout must be positive");
        return v;
    }
}
