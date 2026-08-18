package com.cpf.platform.operations.channelregistry.model;

import java.time.Instant;
import java.util.Locale;

/** 업무 Operation과 직전 호출 Channel 조합에 적용하는 Canonical Channel Policy입니다. */
public record CpfChannelExecutionPolicy(
        String policyKey,
        String operationId,
        String callerChannel,
        boolean allowed,
        boolean authenticationRequired,
        boolean signatureRequired,
        int maxTps,
        Instant effectiveFrom,
        Instant effectiveTo,
        boolean active,
        long version) {

    public CpfChannelExecutionPolicy {
        policyKey = normalize(policyKey, "정책 키", "[A-Z][A-Z0-9_.-]{2,99}");
        operationId = normalizeOperationId(operationId);
        callerChannel = normalizeChannel(callerChannel);
        if (maxTps < 0) {
            throw new IllegalArgumentException("최대 TPS는 0 이상이어야 합니다.");
        }
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("정책 적용 시작일시는 종료일시보다 늦을 수 없습니다.");
        }
        if (version < 0) {
            throw new IllegalArgumentException("정책 버전은 0 이상이어야 합니다.");
        }
    }

    public boolean isEffectiveAt(Instant instant) {
        Instant target = instant == null ? Instant.now() : instant;
        return active
                && (effectiveFrom == null || !target.isBefore(effectiveFrom))
                && (effectiveTo == null || !target.isAfter(effectiveTo));
    }

    private static String normalizeOperationId(String value) {
        String normalized = value == null ? "" : value.trim();
        if (!"*".equals(normalized) && !normalized.matches("[A-Za-z][A-Za-z0-9_.:-]{2,159}")) {
            throw new IllegalArgumentException("Canonical operationId 또는 * 형식이 올바르지 않습니다. value=" + value);
        }
        return normalized;
    }

    private static String normalizeChannel(String value) {
        return normalize(value, "호출 Channel", "(?:ANY|[A-Z][A-Z0-9_]{1,29})");
    }

    private static String normalize(String value, String fieldName, String pattern) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches(pattern)) {
            throw new IllegalArgumentException(fieldName + " 형식이 올바르지 않습니다. value=" + value);
        }
        return normalized;
    }
}
