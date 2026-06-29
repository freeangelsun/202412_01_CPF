package cpf.pfw.common.header;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Locale;

/**
 * 클라이언트 IP를 해석할 때 신뢰 가능한 Proxy/LB/WAF 경유 요청인지 판단하는 정책입니다.
 *
 * <p>외부 클라이언트가 직접 보낸 {@code X-Forwarded-For}, {@code Forwarded}, {@code X-Real-IP},
 * {@code X-Client-IP}는 위조가 쉽기 때문에 기본적으로 신뢰하지 않습니다. 운영 환경에서는
 * {@code -Dcpf.framework.trusted-proxies=10.0.0.10,10.0.0.0/24} 또는
 * {@code CPF_TRUSTED_PROXIES} 환경변수로 실제 Gateway/LB/WAF 주소만 등록해야 합니다.</p>
 */
public final class CpfTrustedProxyPolicy {
    public static final String TRUSTED_PROXIES_PROPERTY = "cpf.framework.trusted-proxies";
    public static final String TRUSTED_PROXIES_ENV = "CPF_TRUSTED_PROXIES";

    private CpfTrustedProxyPolicy() {
    }

    /**
     * 거래 로그와 감사 화면에 사용할 해석된 클라이언트 IP를 반환합니다.
     *
     * <p>trusted proxy가 아닌 원격 주소에서는 전달 계열 헤더를 무시하고 WAS가 본
     * {@link HttpServletRequest#getRemoteAddr()} 값을 사용합니다. trusted proxy인 경우에만
     * Proxy/LB/WAF가 보정한 헤더를 순서대로 해석합니다.</p>
     */
    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String remoteAddr = trimToNull(request.getRemoteAddr());
        if (!isTrustedProxy(remoteAddr)) {
            return remoteAddr;
        }

        return firstText(
                firstForwardedFor(request.getHeader(CpfHeaderNames.FORWARDED_FOR)),
                firstForwardedHeaderFor(request.getHeader(CpfHeaderNames.FORWARDED)),
                trustedHeaderIp(request.getHeader(CpfHeaderNames.REAL_IP)),
                remoteAddr);
    }

    public static boolean isTrustedProxy(String remoteAddr) {
        String normalizedRemoteAddr = trimToNull(remoteAddr);
        if (normalizedRemoteAddr == null) {
            return false;
        }
        return Arrays.stream(configuredTrustedProxies().split(","))
                .map(CpfTrustedProxyPolicy::trimToNull)
                .filter(value -> value != null)
                .anyMatch(rule -> matchesRule(normalizedRemoteAddr, rule));
    }

    private static String configuredTrustedProxies() {
        String propertyValue = trimToNull(System.getProperty(TRUSTED_PROXIES_PROPERTY));
        if (propertyValue != null) {
            return propertyValue;
        }
        String envValue = trimToNull(System.getenv(TRUSTED_PROXIES_ENV));
        return envValue != null ? envValue : "";
    }

    private static boolean matchesRule(String remoteAddr, String rule) {
        if (remoteAddr.equals(rule)) {
            return true;
        }
        if (rule.contains("/")) {
            return matchesIpv4Cidr(remoteAddr, rule);
        }
        return false;
    }

    private static boolean matchesIpv4Cidr(String remoteAddr, String cidr) {
        String[] parts = cidr.split("/", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            long remote = ipv4ToLong(remoteAddr);
            long base = ipv4ToLong(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > 32) {
                return false;
            }
            long mask = prefix == 0 ? 0 : (-1L << (32 - prefix)) & 0xFFFF_FFFFL;
            return (remote & mask) == (base & mask);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static long ipv4ToLong(String value) {
        String[] octets = value.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("IPv4 형식이 아닙니다.");
        }
        long result = 0;
        for (String octet : octets) {
            int parsed = Integer.parseInt(octet);
            if (parsed < 0 || parsed > 255) {
                throw new IllegalArgumentException("IPv4 octet 범위를 벗어났습니다.");
            }
            result = (result << 8) + parsed;
        }
        return result;
    }

    private static String firstForwardedFor(String value) {
        String header = trimToNull(value);
        if (header == null) {
            return null;
        }
        return Arrays.stream(header.split(","))
                .map(CpfTrustedProxyPolicy::trimToNull)
                .filter(candidate -> candidate != null)
                .filter(CpfTrustedProxyPolicy::isUsableForwardedValue)
                .findFirst()
                .orElse(null);
    }

    private static String firstForwardedHeaderFor(String value) {
        String header = trimToNull(value);
        if (header == null) {
            return null;
        }
        for (String entry : header.split(",")) {
            for (String token : entry.split(";")) {
                String trimmed = trimToNull(token);
                if (trimmed == null || !trimmed.toLowerCase(Locale.ROOT).startsWith("for=")) {
                    continue;
                }
                String parsed = normalizeForwardedForValue(trimmed.substring(4));
                if (isUsableForwardedValue(parsed)) {
                    return parsed;
                }
            }
        }
        return null;
    }

    private static String normalizeForwardedForValue(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replace("\"", "");
        if (normalized.startsWith("[") && normalized.contains("]")) {
            return normalized.substring(1, normalized.indexOf(']'));
        }
        int colonIndex = normalized.indexOf(':');
        if (colonIndex > -1 && normalized.indexOf(':', colonIndex + 1) == -1) {
            return normalized.substring(0, colonIndex);
        }
        return normalized;
    }

    private static boolean isUsableForwardedValue(String value) {
        String normalized = trimToNull(value);
        return normalized != null
                && !"unknown".equalsIgnoreCase(normalized)
                && isLikelyIpAddress(normalized);
    }

    private static String trustedHeaderIp(String value) {
        String normalized = normalizeForwardedForValue(value);
        return isUsableForwardedValue(normalized) ? normalized : null;
    }

    private static boolean isLikelyIpAddress(String value) {
        return isIpv4Address(value) || isIpv6Address(value);
    }

    private static boolean isIpv4Address(String value) {
        try {
            ipv4ToLong(value);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static boolean isIpv6Address(String value) {
        if (value == null || !value.contains(":")) {
            return false;
        }
        return value.matches("(?i)[0-9a-f:.]+");
    }

    private static String firstText(String first, String second, String third, String fallback) {
        if (trimToNull(first) != null) {
            return first;
        }
        if (trimToNull(second) != null) {
            return second;
        }
        return trimToNull(third) != null ? third : fallback;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
