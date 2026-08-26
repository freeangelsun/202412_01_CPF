package com.cpf.web.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/** Resolves client IP only through explicitly trusted proxy peers; forwarded values from other peers are ignored. */
public final class CpfTrustedProxyClientIpResolver {
    public static final String TRUSTED_PROXIES_PROPERTY = "cpf.web.trusted-proxies";
    private static final int MAX_FORWARD_LENGTH = 2048;
    private static final int MAX_HOPS = 20;
    private final String configuredPeers;

    public CpfTrustedProxyClientIpResolver(Environment environment) {
        configuredPeers = environment == null ? "" : environment.getProperty(TRUSTED_PROXIES_PROPERTY, "");
    }

    public String resolve(HttpServletRequest request) {
        if (request == null) return null;
        String remote = normalizedAddress(request.getRemoteAddr());
        if (!trustedPeer(remote)) return remote;

        String forwarded = request.getHeader(CpfHttpHeaderNames.FORWARDED);
        if (forwarded != null && !forwarded.isBlank()) {
            String parsed = firstForwardedFor(forwarded);
            if (parsed != null) return parsed;
        }
        String xff = request.getHeader(CpfHttpHeaderNames.X_FORWARDED_FOR);
        if (xff != null && !xff.isBlank()) {
            String parsed = firstXForwardedFor(xff);
            if (parsed != null) return parsed;
        }
        return remote;
    }

    private String firstForwardedFor(String value) {
        bounded(value);
        String[] hops = value.split(",");
        if (hops.length > MAX_HOPS) throw invalidForwarded();
        for (String hop : hops) {
            for (String part : hop.split(";")) {
                String token = part.trim();
                if (!token.regionMatches(true, 0, "for=", 0, 4)) continue;
                String candidate = token.substring(4).trim();
                if (candidate.startsWith("\"") && candidate.endsWith("\"") && candidate.length() >= 2) {
                    candidate = candidate.substring(1, candidate.length() - 1);
                }
                candidate = stripPortAndBrackets(candidate);
                if (candidate.startsWith("_") || "unknown".equalsIgnoreCase(candidate)) return null;
                return normalizedAddress(candidate);
            }
        }
        return null;
    }

    private String firstXForwardedFor(String value) {
        bounded(value);
        String[] hops = value.split(",");
        if (hops.length > MAX_HOPS) throw invalidForwarded();
        for (String hop : hops) {
            String candidate = stripPortAndBrackets(hop.trim());
            if (!candidate.isBlank() && !"unknown".equalsIgnoreCase(candidate)) return normalizedAddress(candidate);
        }
        return null;
    }

    private void bounded(String value) {
        if (value.length() > MAX_FORWARD_LENGTH || value.chars().anyMatch(Character::isISOControl)) throw invalidForwarded();
    }

    private CpfHeaderValidationException invalidForwarded() {
        return new CpfHeaderValidationException(
                com.cpf.core.api.error.CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                CpfHttpHeaderNames.FORWARDED,
                "Forwarded header chain is malformed or oversized.", 400, "FORWARDED_HEADER_INVALID");
    }

    private String normalizedAddress(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > 128 || normalized.chars().anyMatch(Character::isISOControl)) throw invalidForwarded();
        return normalized;
    }

    private String stripPortAndBrackets(String value) {
        if (value == null) return "";
        String result = value.trim();
        if (result.startsWith("[") && result.contains("]")) return result.substring(1, result.indexOf(']'));
        int colon = result.indexOf(':');
        if (colon > 0 && result.indexOf(':', colon + 1) < 0) return result.substring(0, colon);
        return result;
    }

    private boolean trustedPeer(String remoteAddress) {
        if (remoteAddress == null || remoteAddress.isBlank()) return false;
        return Arrays.stream(configuredPeers.split(",")).map(value -> value.trim()).filter(v -> !v.isBlank())
                .anyMatch(rule -> matchesIpv4(remoteAddress, rule));
    }

    private boolean matchesIpv4(String address, String rule) {
        if (address.equalsIgnoreCase(rule)) return true;
        if (!rule.contains("/")) return false;
        try {
            String[] parts = rule.split("/", 2);
            long current = ipv4(address), base = ipv4(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > 32) return false;
            long mask = prefix == 0 ? 0L : (-1L << (32 - prefix)) & 0xFFFF_FFFFL;
            return (current & mask) == (base & mask);
        } catch (RuntimeException ignored) { return false; }
    }

    private long ipv4(String value) {
        String[] octets = value.split("\\.");
        if (octets.length != 4) throw new IllegalArgumentException();
        long result = 0;
        for (String octet : octets) {
            int parsed = Integer.parseInt(octet);
            if (parsed < 0 || parsed > 255) throw new IllegalArgumentException();
            result = (result << 8) | parsed;
        }
        return result;
    }
}
