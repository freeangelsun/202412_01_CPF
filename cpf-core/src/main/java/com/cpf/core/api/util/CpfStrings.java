package com.cpf.core.api.util;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CPF 문자열 편의 API입니다.
 *
 * <p>업무 Source에서 반복되는 null/blank 정규화, 코드 정규화, 길이 제한을
 * 공개 API 한 곳에서 일관되게 제공합니다.</p>
 */
public final class CpfStrings {
    private CpfStrings() {}

    public static boolean hasText(String value) { return value != null && !value.isBlank(); }

    public static String trimToNull(String value) {
        if (!hasText(value)) return null;
        return value.trim();
    }

    /** 값이 blank일 때만 fallback을 사용하며, 유효한 원문 값은 임의로 trim하지 않습니다. */
    public static String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    public static String requireText(String value, String name) {
        String normalized = trimToNull(value);
        if (normalized == null) throw new IllegalArgumentException(name + "은(는) 필수입니다.");
        return normalized;
    }

    public static String abbreviate(String value, int maxLength) {
        if (value == null) return null;
        if (maxLength < 4) throw new IllegalArgumentException("maxLength는 4 이상이어야 합니다.");
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    public static String left(String value, int length) {
        if (value == null) return null;
        if (length < 0) throw new IllegalArgumentException("length는 0 이상이어야 합니다.");
        return value.substring(0, Math.min(value.length(), length));
    }

    public static String right(String value, int length) {
        if (value == null) return null;
        if (length < 0) throw new IllegalArgumentException("length는 0 이상이어야 합니다.");
        return value.substring(Math.max(0, value.length() - length));
    }

    public static String joinNonBlank(Collection<?> values, String delimiter) {
        if (values == null || values.isEmpty()) return "";
        return values.stream().filter(Objects::nonNull).map(String::valueOf)
                .map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.joining(delimiter));
    }

    /** Legacy CMN 의미를 보존해 null/blank는 빈 문자열로, 나머지는 trim + 대문자로 정규화합니다. */
    public static String normalizeCode(String value) {
        return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }

    /** null/blank를 null로 유지해야 하는 신규 코드용 정규화 API입니다. */
    public static String normalizeCodeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    public static String repeat(String value, int count) {
        if (count < 0) throw new IllegalArgumentException("count는 0 이상이어야 합니다.");
        return value == null ? null : value.repeat(count);
    }
}
