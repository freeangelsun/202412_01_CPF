package com.cpf.core.api.util;

import java.util.OptionalInt;
import java.util.OptionalLong;

/** 숫자 입력 파싱/범위 제한에서 반복 예외 처리를 줄이는 CPF API입니다. */
public final class CpfNumbers {
    private CpfNumbers() {}
    /** 문자열을 int로 안전하게 시도하고 실패 시 OptionalInt.empty를 반환합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 파싱 성공 값 또는 empty OptionalInt
     */
    public static OptionalInt toInt(String value) {
        if (!CpfStrings.hasText(value)) return OptionalInt.empty();
        try { return OptionalInt.of(Integer.parseInt(value.trim())); } catch (NumberFormatException ex) { return OptionalInt.empty(); }
    }
    /** 문자열을 long으로 안전하게 시도하고 실패 시 OptionalLong.empty를 반환합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 파싱 성공 값 또는 empty OptionalLong
     */
    public static OptionalLong toLong(String value) {
        if (!CpfStrings.hasText(value)) return OptionalLong.empty();
        try { return OptionalLong.of(Long.parseLong(value.trim())); } catch (NumberFormatException ex) { return OptionalLong.empty(); }
    }
    /** 숫자를 [min,max] 범위로 제한합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param min 허용 최소값
     * @param max 허용 최대값
     * @return 계산된 숫자 값
     * @throws IllegalArgumentException min이 max보다 큰 경우
     */
    public static int clamp(int value, int min, int max) {
        if (min > max) throw new IllegalArgumentException("min은 max보다 클 수 없습니다.");
        return Math.max(min, Math.min(max, value));
    }
    /** 숫자를 [min,max] 범위로 제한합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param min 허용 최소값
     * @param max 허용 최대값
     * @return 계산된 숫자 값
     * @throws IllegalArgumentException min이 max보다 큰 경우
     */
    public static long clamp(long value, long min, long max) {
        if (min > max) throw new IllegalArgumentException("min은 max보다 클 수 없습니다.");
        return Math.max(min, Math.min(max, value));
    }
}
