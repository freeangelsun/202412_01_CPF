package com.cpf.bzachannel.shared.config;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bza.channel")
public record BzaChannelProperties(
        Mode mode,
        URI gatewayBaseUri,
        URI directBaseUri,
        String callerSystemCode,
        String targetSystemCode,
        String callerChannel,
        Duration connectTimeout,
        Duration requestTimeout) {

    public enum Mode { GATEWAY, DIRECT }

    public BzaChannelProperties {
        mode = mode == null ? Mode.GATEWAY : mode;
        callerSystemCode = systemCode(callerSystemCode, "BCH");
        targetSystemCode = systemCode(targetSystemCode, "BZA");
        callerChannel = token(callerChannel, "BZA", 32);
        connectTimeout = positive(connectTimeout, Duration.ofSeconds(3));
        requestTimeout = positive(requestTimeout, Duration.ofSeconds(10));
        URI selected = mode == Mode.GATEWAY ? gatewayBaseUri : directBaseUri;
        if (selected == null || !selected.isAbsolute()) {
            throw new IllegalArgumentException("BZA Channel " + mode + " base URI must be absolute");
        }
        String scheme = selected.getScheme().toLowerCase(Locale.ROOT);
        if (!(scheme.equals("http") || scheme.equals("https"))) {
            throw new IllegalArgumentException("BZA Channel upstream scheme must be http/https");
        }
    }

    public URI selectedBaseUri() {
        return mode == Mode.GATEWAY ? gatewayBaseUri : directBaseUri;
    }

    private static String systemCode(String value, String fallback) {
        String normalized = token(value, fallback, 3).toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9]{3}")) throw new IllegalArgumentException("systemCode must be 3 characters");
        return normalized;
    }

    private static String token(String value, String fallback, int max) {
        String v = value == null || value.isBlank() ? fallback : value.trim();
        if (v.length() > max || v.indexOf('\r') >= 0 || v.indexOf('\n') >= 0) throw new IllegalArgumentException("invalid BZA Channel token");
        return v;
    }

    private static Duration positive(Duration value, Duration fallback) {
        Duration v = value == null ? fallback : value;
        if (v.isZero() || v.isNegative()) throw new IllegalArgumentException("timeout must be positive");
        return v;
    }
}
