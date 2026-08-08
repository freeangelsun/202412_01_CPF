package com.cpf.core.api.util;

import java.util.Collection;

/** Controller/Service의 반복적인 방어 검증을 간단하게 표현하는 CPF API입니다. */
public final class CpfValidation {
    private CpfValidation() {}
    /** 조건이 거짓이면 지정 메시지로 fail-fast 합니다.
     * @param condition 참이어야 하는 검증 조건
     * @param message 검증 실패 시 노출할 오류 메시지
     * @throws IllegalArgumentException condition이 false인 경우
     */
    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    /** 필수 문자열을 공통 문자열 정책으로 검증합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param name 오류 메시지에 사용할 논리 필드명
     * @return 정규화/변환된 문자열 또는 계약상 null
     * @throws IllegalArgumentException null/blank인 경우
     */
    public static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value.trim();
    }
    /** nullable 문자열의 최대 길이를 검증합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param name 오류 메시지에 사용할 논리 필드명
     * @param max 허용 최대값
     * @return 정규화/변환된 문자열 또는 계약상 null
     * @throws IllegalArgumentException max를 초과한 경우
     */
    public static String maxLength(String value, String name, int max) {
        if (value != null && value.length() > max) throw new IllegalArgumentException(name + "은(는) " + max + "자를 초과할 수 없습니다.");
        return value;
    }
    /** Collection이 null/empty인지 검증합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param name 오류 메시지에 사용할 논리 필드명
     * @return 계약에 따른 결과 값
     * @throws IllegalArgumentException null 또는 empty Collection인 경우
     */
    public static <T extends Collection<?>> T notEmpty(T value, String name) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException(name + "은(는) 비어 있을 수 없습니다.");
        return value;
    }
}
