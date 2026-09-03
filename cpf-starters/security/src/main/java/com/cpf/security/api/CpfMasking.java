package com.cpf.security.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF Security Owner가 제공하는 안전한 표시·로그 마스킹 Facade입니다.
 *
 * <p>원문 payload를 자동 기록하지 않으며, 구조화 값은 {@link CpfSensitiveData}의 분류·예산 정책을
 * 통과한 복사본만 반환합니다. 일반 개발자는 직접 마스킹 알고리즘을 구현하지 않고 이 API를 사용합니다.</p>
 */
public final class CpfMasking {
    private CpfMasking() { }

    public static String mask(String value) { return mask(value, 256); }
    public static String mask(String value, int maxLength) {
        String v = truncate(value, maxLength);
        if (v == null || v.isEmpty()) return v;
        if (v.length() == 1) return "*";
        if (v.length() == 2) return v.substring(0, 1) + "*";
        return v.substring(0, 1) + "*".repeat(Math.max(1, v.length() - 2)) + v.substring(v.length() - 1);
    }
    /** truncate 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String truncate(String value, int maxLength) {
        if (value == null) return null;
        int safe = Math.max(0, maxLength);
        return value.length() <= safe ? value : value.substring(0, safe);
    }
    public static int policyVersion() { return Math.toIntExact(CpfMaskingRuntime.policyVersion()); }
    /** 구조화/로그 문자열에 현재 CPF 마스킹 정책을 적용합니다. */
    public static String sanitize(String value) { return CpfMaskingRuntime.mask(value); }
    /** 구조화/로그 문자열에 현재 CPF 마스킹 정책과 길이 상한을 적용합니다. */
    public static String sanitize(String value, int maxLength) { return CpfMaskingRuntime.mask(value, maxLength); }
    /** 식별자는 뒤 4자리만 보존하는 fail-closed 표시값으로 변환합니다. */
    public static String identifier(String value) { return CpfMaskingRuntime.maskIdentifier(value); }
    /** 현재 동적 마스킹 정책을 운영용 Snapshot으로 반환합니다. */
    public static CpfMaskingPolicySnapshot runtimePolicy() {
        CpfMaskingRuntime.MaskingPolicy p = CpfMaskingRuntime.currentPolicy();
        return new CpfMaskingPolicySnapshot(p.version(), p.sensitiveKeys(), p.maxLength(), p.maskBearerToken(),
                p.valueRules(), p.updatedAt(), "CPF_SYSTEM", "active runtime masking policy");
    }

    public static long activePolicyVersion() { return CpfMaskingRuntime.policyVersion(); }
    /** classifyField 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfSensitiveData.Classification classifyField(String fieldName) { return CpfSensitiveData.classifyField(fieldName); }
    public static Object structured(Object value) { return CpfSensitiveData.sanitizeAuditSnapshot(value); }
    public static Map<String, Object> detail(Map<?, ?> value) {
        if (value == null) return Map.of();
        Object masked = structured(value);
        if (!(masked instanceof Map<?, ?> map)) return Map.of();
        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        map.forEach((key, item) -> copy.put(String.valueOf(key), item));
        return Collections.unmodifiableMap(copy);
    }
    /** list 작업을 CPF 표준 계약에 따라 수행한다. */
    public static List<Object> list(Iterable<?> value) {
        if (value == null) return List.of();
        Object masked = structured(value);
        if (!(masked instanceof List<?> list)) return List.of();
        return Collections.unmodifiableList(new ArrayList<>(list));
    }
    public static String name(String value) { return mask(value); }
    /** email 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String email(String value) {
        if (value == null || value.isBlank()) return value;
        int at = value.indexOf('@');
        if (at <= 0) return mask(value);
        String local = value.substring(0, at);
        return (local.length() <= 2 ? local.substring(0, 1) + "*" : local.substring(0, 2) + "***") + value.substring(at);
    }
    /** mobile 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String mobile(String value) {
        if (value == null || value.isBlank()) return value;
        String d = value.replaceAll("[^0-9]", "");
        return d.length() < 7 ? mask(value) : d.substring(0, 3) + "****" + d.substring(d.length() - 4);
    }
    public static String maskSensitive(String value) { return mask(value); }
    public static String maskName(String value) { return name(value); }
    /** maskEmail 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String maskEmail(String value) { return email(value); }
    public static String maskMobile(String value) { return mobile(value); }
}
