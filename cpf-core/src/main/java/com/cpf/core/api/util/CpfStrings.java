package com.cpf.core.api.util;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CPF 문자열 편의 API입니다.
 *
 * <p>JDK 기능을 단순히 이름만 바꾸지 않고, 업무 Source에서 반복되는 null/blank
 * 정규화와 길이 제한을 한 줄로 안전하게 처리하는 기능만 제공합니다.</p>
 */
public final class CpfStrings {
    private CpfStrings() {}

    public static boolean hasText(String value) { return value != null && !value.isBlank(); }
    public static String trimToNull(String value) {
        if (!hasText(value)) return null;
        return value.trim();
    }
    public static String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
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
}
