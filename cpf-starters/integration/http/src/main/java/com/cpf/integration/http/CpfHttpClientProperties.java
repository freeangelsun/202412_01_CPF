package com.cpf.integration.http;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.http-client")
/** CpfHttpClientProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfHttpClientProperties {
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration requestTimeout = Duration.ofSeconds(10);
    private int maxRequestBytes = 4 * 1024 * 1024;
    private int maxResponseBytes = 4 * 1024 * 1024;
    private boolean requireIdempotencyKeyForMutations = true;
    private Set<String> allowedHosts = new LinkedHashSet<>(Set.of("localhost", "127.0.0.1", "::1"));
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private boolean allowUnlistedHosts;

    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration value) { connectTimeout = value; }
    public Duration getRequestTimeout() { return requestTimeout; }
    public void setRequestTimeout(Duration value) { requestTimeout = value; }
    public int getMaxRequestBytes() { return maxRequestBytes; }
    public void setMaxRequestBytes(int value) { maxRequestBytes = value; }
    public int getMaxResponseBytes() { return maxResponseBytes; }
    public void setMaxResponseBytes(int value) { maxResponseBytes = value; }
    public boolean isRequireIdempotencyKeyForMutations() { return requireIdempotencyKeyForMutations; }
    public void setRequireIdempotencyKeyForMutations(boolean value) { requireIdempotencyKeyForMutations = value; }
    public Set<String> getAllowedHosts() { return Set.copyOf(allowedHosts); }
    public void setAllowedHosts(Set<String> values) {
        allowedHosts = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) allowedHosts.add(normalizeHostPattern(value));
            }
        }
    }
    public boolean isAllowUnlistedHosts() { return allowUnlistedHosts; }
    public void setAllowUnlistedHosts(boolean value) { allowUnlistedHosts = value; }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate() {
        requirePositive(connectTimeout, "connect-timeout");
        requirePositive(requestTimeout, "request-timeout");
        if (maxRequestBytes < 1024 || maxRequestBytes > 268_435_456) {
            throw new IllegalStateException("max-request-bytes must be between 1024 and 268435456");
        }
        if (maxResponseBytes < 1024 || maxResponseBytes > 268_435_456) {
            throw new IllegalStateException("max-response-bytes must be between 1024 and 268435456");
        }
        if (!allowUnlistedHosts && allowedHosts.isEmpty()) {
            throw new IllegalStateException("allowed-hosts is required when allow-unlisted-hosts=false");
        }
        for (String value : allowedHosts) normalizeHostPattern(value);
    }

    boolean allowsHost(String host) {
        String normalized = normalizeHost(host);
        if (allowUnlistedHosts) return true;
        for (String pattern : allowedHosts) {
            if (pattern.equals(normalized)) return true;
            if (pattern.startsWith("*.") && normalized.endsWith(pattern.substring(1))
                    && normalized.length() > pattern.length() - 1) {
                return true;
            }
        }
        return false;
    }

    static String normalizeHost(String value) {
        String host = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (host.startsWith("[") && host.endsWith("]")) host = host.substring(1, host.length() - 1);
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        if (host.isBlank()) throw new IllegalArgumentException("host is required");
        if (host.indexOf(':') >= 0) return host; // IPv6 literal
        try {
            return java.net.IDN.toASCII(host, java.net.IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException("invalid host", invalid);
        }
    }

    private static String normalizeHostPattern(String value) {
        String pattern = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (pattern.contains("://") || pattern.contains("/") || pattern.contains("@")
                || pattern.contains("?") || pattern.contains("#")) {
            throw new IllegalArgumentException("allowed host must not contain URI components");
        }
        if (pattern.startsWith("*.")) {
            String suffix = normalizeHost(pattern.substring(2));
            if (!suffix.contains(".")) throw new IllegalArgumentException("wildcard host suffix is too broad");
            return "*." + suffix;
        }
        return normalizeHost(pattern);
    }

    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isNegative() || value.isZero() || value.compareTo(Duration.ofMinutes(10)) > 0) {
            throw new IllegalStateException(name + " must be positive and at most 10 minutes");
        }
    }
}
