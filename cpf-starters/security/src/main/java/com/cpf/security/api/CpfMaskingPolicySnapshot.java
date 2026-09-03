package com.cpf.security.api;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Immutable, versioned masking policy exposed without regex/provider implementation types. */
/** CpfMaskingPolicySnapshot 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfMaskingPolicySnapshot(
        long version,
        Set<String> sensitiveKeys,
        int maxLength,
        boolean maskBearerToken,
        /** 운영자가 ADM에서 선택한 값 규칙입니다(Harness §28.1). */
        Set<CpfMaskingValueRule> valueRules,
        Instant updatedAt,
        String updatedBy,
        String reason) {
    private static final Pattern KEY = Pattern.compile("[a-z0-9_.-]{2,64}");
    private static final int MAX_KEYS = 256;

    /**
     * 값 규칙 선택이 없던 기존 Consumer의 Source 호환 생성자입니다.
     *
     * <p>선택이 주어지지 않으면 fail-closed로 모든 값 규칙을 켠 기본 선택을 사용합니다.</p>
     */
    public CpfMaskingPolicySnapshot(long version, Set<String> sensitiveKeys, int maxLength,
            boolean maskBearerToken, Instant updatedAt, String updatedBy, String reason) {
        this(version, sensitiveKeys, maxLength, maskBearerToken, CpfMaskingValueRule.defaults(),
                updatedAt, updatedBy, reason);
    }

    public CpfMaskingPolicySnapshot {
        if (version < 1L) throw new IllegalArgumentException("version must be positive");
        if (sensitiveKeys == null) throw new IllegalArgumentException("sensitiveKeys is required");
        if (sensitiveKeys.size() > MAX_KEYS) throw new IllegalArgumentException("too many sensitive keys");
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String key : sensitiveKeys) {
            if (key == null || key.isBlank()) throw new IllegalArgumentException("sensitive key is blank");
            String value = key.trim().toLowerCase(Locale.ROOT);
            if (!KEY.matcher(value).matches()) throw new IllegalArgumentException("invalid sensitive key");
            if (!normalized.add(value)) throw new IllegalArgumentException("duplicate sensitive key");
        }
        sensitiveKeys = Set.copyOf(normalized);
        if (valueRules == null) throw new IllegalArgumentException("valueRules is required");
        valueRules = Set.copyOf(valueRules);
        if (maxLength < 256 || maxLength > 65_536) {
            throw new IllegalArgumentException("maxLength must be between 256 and 65536");
        }
        if (updatedAt == null) throw new IllegalArgumentException("updatedAt is required");
        updatedBy = required(updatedBy, "updatedBy", 128);
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }

    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (normalized.length() > max) throw new IllegalArgumentException(field + " is too long");
        if (normalized.chars().anyMatch(ch -> Character.isISOControl(ch))) {
            throw new IllegalArgumentException(field + " contains control characters");
        }
        return normalized;
    }
}
