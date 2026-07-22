package com.cpf.batch.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 독립 BAT worker의 식별, 처리 용량, polling, lease 정책을 type-safe하게 바인딩합니다.
 */
@ConfigurationProperties(prefix = "cpf.bat.worker")
public record BatWorkerProperties(
        boolean enabled,
        String workerId,
        String version,
        Set<String> capabilities,
        int maxConcurrency,
        int queueCapacity,
        long pollIntervalMs,
        long heartbeatIntervalMs,
        int leaseSeconds,
        boolean recoverExpiredLease) {

    public BatWorkerProperties {
        workerId = text(workerId, "");
        version = text(version, "unknown");
        capabilities = capabilities == null || capabilities.isEmpty()
                ? Set.of("*")
                : Set.copyOf(new LinkedHashSet<>(capabilities));
        maxConcurrency = positive(maxConcurrency, 1, "maxConcurrency");
        queueCapacity = positive(queueCapacity, maxConcurrency, "queueCapacity");
        pollIntervalMs = positive(pollIntervalMs, 1_000L, "pollIntervalMs");
        heartbeatIntervalMs = positive(heartbeatIntervalMs, 2_000L, "heartbeatIntervalMs");
        leaseSeconds = positive(leaseSeconds, 30, "leaseSeconds");
        if (queueCapacity < maxConcurrency) {
            throw new IllegalArgumentException("queueCapacity는 maxConcurrency 이상이어야 합니다.");
        }
        if (leaseSeconds * 1_000L <= heartbeatIntervalMs) {
            throw new IllegalArgumentException("leaseSeconds는 heartbeat 주기보다 길어야 합니다.");
        }
        for (String capability : capabilities) {
            if (capability == null || !capability.matches("[A-Za-z0-9_.:*\\-]{1,120}")) {
                throw new IllegalArgumentException("worker capability 형식이 올바르지 않습니다.");
            }
        }
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static int positive(int value, int fallback, String field) {
        int resolved = value == 0 ? fallback : value;
        if (resolved < 1) {
            throw new IllegalArgumentException(field + "는 1 이상이어야 합니다.");
        }
        return resolved;
    }

    private static long positive(long value, long fallback, String field) {
        long resolved = value == 0L ? fallback : value;
        if (resolved < 1L) {
            throw new IllegalArgumentException(field + "는 1 이상이어야 합니다.");
        }
        return resolved;
    }
}
