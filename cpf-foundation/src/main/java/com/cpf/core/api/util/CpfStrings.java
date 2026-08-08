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

    /** 문자열이 null/blank가 아닌지 검사합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @return 조건을 만족하면 true, 아니면 false

     */

    public static boolean hasText(String value) { return value != null && !value.isBlank(); }

    /** null/blank는 null, 나머지는 trim된 문자열로 정규화합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @return 정규화/변환된 문자열 또는 계약상 null

     */

    public static String trimToNull(String value) {
        if (!hasText(value)) return null;
        return value.trim();
    }

        /** blank일 때만 fallback을 사용하고 유효 원문은 그대로 유지합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param fallback 입력이 blank/null일 때 사용할 대체 값
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    /** 필수 문자열을 trim하고 blank 입력을 fail-fast 합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @param name 오류 메시지에 사용할 논리 필드명

     * @return 정규화/변환된 문자열 또는 계약상 null

     * @throws IllegalArgumentException 입력이 null/blank인 경우

     */

    public static String requireText(String value, String name) {
        String normalized = trimToNull(value);
        if (normalized == null) throw new IllegalArgumentException(name + "은(는) 필수입니다.");
        return normalized;
    }

    /** 최대 길이를 넘는 문자열을 말줄임표 포함 길이로 줄입니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @param maxLength 허용할 최대 문자열 길이

     * @return 정규화/변환된 문자열 또는 계약상 null

     * @throws IllegalArgumentException maxLength가 4 미만인 경우

     */

    public static String abbreviate(String value, int maxLength) {
        if (value == null) return null;
        if (maxLength < 4) throw new IllegalArgumentException("maxLength는 4 이상이어야 합니다.");
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    /** 문자열 왼쪽에서 지정 길이만 반환합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @param length 반환할 최대 문자 수

     * @return 정규화/변환된 문자열 또는 계약상 null

     * @throws IllegalArgumentException length가 음수인 경우

     */

    public static String left(String value, int length) {
        if (value == null) return null;
        if (length < 0) throw new IllegalArgumentException("length는 0 이상이어야 합니다.");
        return value.substring(0, Math.min(value.length(), length));
    }

    /** 문자열 오른쪽에서 지정 길이만 반환합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @param length 반환할 최대 문자 수

     * @return 정규화/변환된 문자열 또는 계약상 null

     * @throws IllegalArgumentException length가 음수인 경우

     */

    public static String right(String value, int length) {
        if (value == null) return null;
        if (length < 0) throw new IllegalArgumentException("length는 0 이상이어야 합니다.");
        return value.substring(Math.max(0, value.length() - length));
    }

    /** null/blank 원소를 제거하고 delimiter로 연결합니다.

     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.

     * @param delimiter 값 사이에 사용할 구분자

     * @return 정규화/변환된 문자열 또는 계약상 null

     */

    public static String joinNonBlank(Collection<?> values, String delimiter) {
        if (values == null || values.isEmpty()) return "";
        return values.stream().filter(Objects::nonNull).map(String::valueOf)
                .map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.joining(delimiter));
    }

        /** legacy 코드 규칙에 따라 blank를 빈 문자열, 나머지를 trim+대문자로 정규화합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String normalizeCode(String value) {
        return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }

        /** 신규 nullable 코드 규칙에 따라 blank를 null, 나머지를 trim+대문자로 정규화합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String normalizeCodeOrNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    /** 문자열을 지정 횟수 반복합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @param count 0 이상의 반복 횟수

     * @return 정규화/변환된 문자열 또는 계약상 null

     * @throws IllegalArgumentException count가 음수인 경우

     */

    public static String repeat(String value, int count) {
        if (count < 0) throw new IllegalArgumentException("count는 0 이상이어야 합니다.");
        return value == null ? null : value.repeat(count);
    }
}
