package com.cpf.foundation.util;

import com.cpf.core.api.error.CpfValidationException;
import java.util.Locale;

/**
 * 기술중립 문자열 정규화 Utility입니다.
 *
 * <p>업무 규칙이나 특정 도메인 정책은 소유하지 않습니다.</p>
 */
public final class CpfStrings {
    private CpfStrings() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException((fieldName == null ? "value" : fieldName) + " 값은 필수입니다.");
        }
        return value.trim();
    }

    public static String defaultIfBlank(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    public static String normalizeCode(String value) {
        String normalized = requireText(value, "code").toUpperCase(Locale.ROOT);
        if (normalized.length() > 160) {
            throw new CpfValidationException("code 길이는 160 이하여야 합니다.");
        }
        if (!normalized.matches("[A-Z0-9][A-Z0-9_.:-]*")) {
            throw new CpfValidationException("code 형식이 올바르지 않습니다.");
        }
        return normalized;
    }
}
