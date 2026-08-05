package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.api.resilience.CpfResilienceRuntimePolicy;
import com.cpf.core.spi.resilience.CpfResilienceRuntimePolicyResolver;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import org.springframework.core.env.Environment;

/** Resolves global and operation-specific runtime safety limits from Spring configuration. */
final class CpfEnvironmentResilienceRuntimePolicyResolver implements CpfResilienceRuntimePolicyResolver {
    private static final String GLOBAL = "cpf.integration.resilience.runtime.";
    private final Environment environment;

    CpfEnvironmentResilienceRuntimePolicyResolver(Environment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    @Override
    public CpfResilienceRuntimePolicy resolve(CpfResiliencePolicy policy, CpfResilienceCallContext context) {
        CpfResilienceRuntimePolicy fallback = CpfResilienceRuntimePolicy.legacyCompatible(policy);
        String operationPrefix = GLOBAL + "operations." + propertySegment(context.operationId()) + ".";
        return new CpfResilienceRuntimePolicy(
                policy,
                duration(operationPrefix, "connect-timeout", fallback.connectTimeout()),
                duration(operationPrefix, "tls-timeout", fallback.tlsTimeout()),
                duration(operationPrefix, "write-timeout", fallback.writeTimeout()),
                duration(operationPrefix, "response-header-timeout", fallback.responseHeaderTimeout()),
                duration(operationPrefix, "read-timeout", fallback.readTimeout()),
                duration(operationPrefix, "attempt-timeout", fallback.attemptTimeout()),
                duration(operationPrefix, "overall-timeout", fallback.overallTimeout()),
                duration(operationPrefix, "retry.initial-backoff", fallback.initialRetryBackoff()),
                duration(operationPrefix, "retry.max-backoff", fallback.maxRetryBackoff()),
                decimal(operationPrefix, "retry.jitter-ratio", fallback.jitterRatio()),
                integer(operationPrefix, "retry.budget-capacity", fallback.retryBudgetCapacity()),
                duration(operationPrefix, "retry.budget-window", fallback.retryBudgetWindow()),
                integer(operationPrefix, "bulkhead.queue-limit", fallback.bulkheadQueueLimit()),
                duration(operationPrefix, "bulkhead.queue-wait", fallback.bulkheadQueueWait()));
    }

    private Duration duration(String operationPrefix, String suffix, Duration fallback) {
        String value = first(operationPrefix + suffix, GLOBAL + suffix);
        if (value == null) return fallback;
        try {
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("p")) return Duration.parse(value.trim().toUpperCase(Locale.ROOT));
            if (normalized.endsWith("ms")) return Duration.ofMillis(number(normalized, 2));
            if (normalized.endsWith("s")) return Duration.ofSeconds(number(normalized, 1));
            if (normalized.endsWith("m")) return Duration.ofMinutes(number(normalized, 1));
            if (normalized.endsWith("h")) return Duration.ofHours(number(normalized, 1));
            return Duration.ofMillis(Long.parseLong(normalized));
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException("invalid resilience duration property " + suffix + "=" + value, invalid);
        }
    }

    private int integer(String operationPrefix, String suffix, int fallback) {
        String value = first(operationPrefix + suffix, GLOBAL + suffix);
        if (value == null) return fallback;
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("invalid resilience integer property " + suffix + "=" + value, invalid);
        }
    }

    private double decimal(String operationPrefix, String suffix, double fallback) {
        String value = first(operationPrefix + suffix, GLOBAL + suffix);
        if (value == null) return fallback;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("invalid resilience decimal property " + suffix + "=" + value, invalid);
        }
    }

    private String first(String operationKey, String globalKey) {
        String value = environment.getProperty(operationKey);
        if (value == null || value.isBlank()) value = environment.getProperty(globalKey);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static long number(String normalized, int suffixLength) {
        return Long.parseLong(normalized.substring(0, normalized.length() - suffixLength).trim());
    }

    private static String propertySegment(String operationId) {
        String normalized = operationId == null ? "unknown" : operationId.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (normalized.isBlank()) return "unknown";
        if (normalized.length() > 128) return normalized.substring(0, 128);
        return normalized;
    }
}
