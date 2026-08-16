package com.cpf.starter.runtime;

/**
 * Base Starter logging에서 사용하는 최소 fail-safe masking utility입니다.
 * Security Capability가 선택되지 않아도 로그 원문을 직접 노출하지 않도록 보장합니다.
 */
final class CpfLogMasking {
    private CpfLogMasking() { }

    static String mask(String value, int maxLength) {
        if (value == null) return null;
        int safeLimit = Math.max(0, maxLength);
        String v = value.length() <= safeLimit ? value : value.substring(0, safeLimit);
        if (v.isEmpty()) return v;
        if (v.length() == 1) return "*";
        if (v.length() == 2) return v.substring(0, 1) + "*";
        return v.substring(0, 1) + "*".repeat(Math.max(1, v.length() - 2)) + v.substring(v.length() - 1);
    }
}
