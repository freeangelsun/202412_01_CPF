package com.cpf.batch.api;

import java.util.Map;

/**
 * Batch 운영 명령의 낙관적 잠금 Version 계약입니다.
 *
 * <p>ADM/BAT/Remote topology가 동일한 Version 규칙을 사용하도록 공개 API 경계에 둡니다.
 * Version이 누락되거나 현재 Row와 다르면 위험 조치를 시작하지 않고 fail-closed 합니다.</p>
 */
public final class CpfBatchOptimisticVersion {
    private static final String[] VERSION_KEYS = {
            "row_version", "rowVersion", "expectedVersion", "version", "ROW_VERSION", "VERSION"
    };

    private CpfBatchOptimisticVersion() {}

    public static long require(Long value, String operation) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("expectedVersion is required for " + operation);
        }
        return value;
    }

    public static long read(Map<String, ?> row, String target) {
        if (row == null) {
            throw new IllegalStateException("BAT optimistic-lock row is missing: " + target);
        }
        for (String key : VERSION_KEYS) {
            Object raw = row.get(key);
            if (raw == null) continue;
            if (raw instanceof Number number) return number.longValue();
            try {
                return Long.parseLong(String.valueOf(raw).trim());
            } catch (NumberFormatException ignored) {
                throw new IllegalStateException("BAT optimistic-lock version is invalid: " + target);
            }
        }
        throw new IllegalStateException("BAT optimistic-lock version is missing: " + target);
    }

    public static void assertMatches(Map<String, ?> row, long expectedVersion, String target) {
        long actual = read(row, target);
        if (actual != expectedVersion) {
            throw new IllegalStateException(
                    "BAT optimistic-lock conflict: target=" + target
                            + " expectedVersion=" + expectedVersion
                            + " actualVersion=" + actual);
        }
    }
}
