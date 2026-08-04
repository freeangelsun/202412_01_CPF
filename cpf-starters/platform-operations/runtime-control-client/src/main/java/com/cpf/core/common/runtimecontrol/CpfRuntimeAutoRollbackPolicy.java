package com.cpf.core.common.runtimecontrol;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Controller automatic rollback의 allowlist·attempt·rate·shared circuit 판정입니다. */
final class CpfRuntimeAutoRollbackPolicy {
    private final Set<String> allowlist;
    private final int maxAttempts;
    private final int maxPerRun;
    private final int circuitFailureThreshold;
    private final long circuitOpenMillis;

    CpfRuntimeAutoRollbackPolicy(
            Set<String> allowlist,
            int maxAttempts,
            int maxPerRun,
            int circuitFailureThreshold,
            long circuitOpenMillis) {
        this.allowlist = normalize(allowlist);
        this.maxAttempts = bounded(maxAttempts, 1, 100, "maxAttempts");
        this.maxPerRun = bounded(maxPerRun, 1, 100, "maxPerRun");
        this.circuitFailureThreshold =
                bounded(circuitFailureThreshold, 1, 100, "circuitFailureThreshold");
        if (circuitOpenMillis < 1_000L || circuitOpenMillis > 3_600_000L) {
            throw new IllegalArgumentException("circuitOpenMillis 범위 오류");
        }
        this.circuitOpenMillis = circuitOpenMillis;
    }

    Decision decide(
            String changeType,
            String approvalId,
            String breakGlassId,
            int attempts,
            int recentFailures) {
        String normalizedType = normalize(changeType);
        if (!allowed(normalizedType)) {
            return Decision.blocked("TYPE_NOT_ALLOWLISTED");
        }
        if (blank(approvalId) && blank(breakGlassId)) {
            return Decision.blocked("APPROVAL_REQUIRED");
        }
        if (attempts >= maxAttempts) {
            return Decision.blocked("ATTEMPT_LIMIT_EXCEEDED");
        }
        if (recentFailures >= circuitFailureThreshold) {
            return Decision.blocked("CIRCUIT_OPEN");
        }
        return Decision.permit();
    }

    int maxPerRun() {
        return maxPerRun;
    }

    long circuitOpenMillis() {
        return circuitOpenMillis;
    }

    private boolean allowed(String changeType) {
        if (allowlist.isEmpty() || changeType.isBlank()) {
            return false;
        }
        for (String entry : allowlist) {
            if (changeType.equals(entry)
                    || changeType.startsWith(entry + "_")
                    || changeType.startsWith(entry + ":")) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> normalize(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isBlank()) {
                result.add(normalized);
            }
        }
        return Set.copyOf(result);
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static int bounded(int value, int min, int max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " 범위 오류");
        }
        return value;
    }

    record Decision(boolean allowed, String reason) {
        static Decision permit() {
            return new Decision(true, "ALLOWED");
        }

        static Decision blocked(String reason) {
            return new Decision(false, reason);
        }
    }
}
