package com.cpf.web.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fail-closed internal ingress trust resolver.
 *
 * <p>A request Header never proves caller identity. Trusted internal identity comes from either a
 * security/mTLS filter that sets {@link #VERIFIED_INTERNAL_CALLER_ATTRIBUTE}, or from an explicit
 * operator-owned peer-to-system mapping. Network trust and caller identity are therefore evaluated
 * independently from {@code X-Caller-Channel}.</p>
 */
public final class CpfConfiguredIngressTrustResolver implements CpfHttpIngressTrustResolver {
    public static final String PEER_IDENTITIES_PROPERTY = "cpf.web.internal-peer-identities";
    private final Map<String, String> peerIdentities;

    public CpfConfiguredIngressTrustResolver(Environment environment) {
        String configured = environment == null ? "" : environment.getProperty(PEER_IDENTITIES_PROPERTY, "");
        this.peerIdentities = parse(configured);
    }

    @Override
    public Decision resolve(HttpServletRequest request) {
        if (request == null) return external();

        Object verified = request.getAttribute(VERIFIED_INTERNAL_CALLER_ATTRIBUTE);
        if (verified instanceof String caller && !caller.isBlank()) {
            return new Decision(CpfHttpIngressTrust.TRUSTED_INTERNAL, normalizeSystem(caller));
        }

        String remoteAddress = request.getRemoteAddr();
        for (Map.Entry<String, String> entry : peerIdentities.entrySet()) {
            if (matchesPeer(remoteAddress, entry.getKey())) {
                return new Decision(CpfHttpIngressTrust.TRUSTED_INTERNAL, entry.getValue());
            }
        }
        return external();
    }

    private static Decision external() {
        return new Decision(CpfHttpIngressTrust.UNTRUSTED_EXTERNAL, null);
    }

    /**
     * Format: {@code peer-or-cidr=SYSTEM;peer-or-cidr=SYSTEM}. Duplicate peers and invalid systems
     * fail startup instead of silently weakening the trust boundary.
     */
    static Map<String, String> parse(String configured) {
        if (configured == null || configured.isBlank()) return Map.of();
        LinkedHashMap<String, String> mappings = new LinkedHashMap<>();
        for (String raw : configured.split(";")) {
            String entry = raw.trim();
            if (entry.isEmpty()) continue;
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator == entry.length() - 1) {
                throw new IllegalArgumentException(PEER_IDENTITIES_PROPERTY + " must use peer-or-cidr=SYSTEM entries");
            }
            String peer = entry.substring(0, separator).trim();
            String system = normalizeSystem(entry.substring(separator + 1));
            if (!isPeerRule(peer) || mappings.putIfAbsent(peer, system) != null) {
                throw new IllegalArgumentException("Invalid or duplicate internal peer identity mapping: " + peer);
            }
        }
        return Map.copyOf(mappings);
    }

    private static boolean matchesPeer(String address, String rule) {
        if (address == null || address.isBlank()) return false;
        String normalizedAddress = address.trim();
        if (normalizedAddress.equals(rule)) return true;
        if (!rule.contains("/")) return false;
        try {
            String[] parts = rule.split("/", 2);
            long current = ipv4(normalizedAddress);
            long base = ipv4(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > 32) return false;
            long mask = prefix == 0 ? 0L : (-1L << (32 - prefix)) & 0xFFFF_FFFFL;
            return (current & mask) == (base & mask);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isPeerRule(String rule) {
        if (rule == null || rule.isBlank()) return false;
        if (!rule.contains("/")) {
            try { ipv4(rule); return true; } catch (RuntimeException ignored) { return false; }
        }
        String[] parts = rule.split("/", 2);
        try {
            ipv4(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            return prefix >= 0 && prefix <= 32;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static long ipv4(String value) {
        String[] octets = value.split("\\.");
        if (octets.length != 4) throw new IllegalArgumentException("IPv4 required");
        long result = 0;
        for (String octet : octets) {
            int parsed = Integer.parseInt(octet);
            if (parsed < 0 || parsed > 255) throw new IllegalArgumentException("IPv4 octet");
            result = (result << 8) | parsed;
        }
        return result;
    }

    private static String normalizeSystem(String value) {
        if (value == null) throw new IllegalArgumentException("systemCode");
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{1,31}")) {
            throw new IllegalArgumentException("Invalid systemCode in internal peer identity mapping");
        }
        return normalized;
    }
}
