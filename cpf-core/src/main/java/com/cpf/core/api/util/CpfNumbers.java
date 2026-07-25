package com.cpf.core.api.util;

import java.util.OptionalInt;
import java.util.OptionalLong;

/** 숫자 입력 파싱/범위 제한에서 반복 예외 처리를 줄이는 CPF API입니다. */
public final class CpfNumbers {
    private CpfNumbers() {}
    public static OptionalInt toInt(String value) {
        if (!CpfStrings.hasText(value)) return OptionalInt.empty();
        try { return OptionalInt.of(Integer.parseInt(value.trim())); } catch (NumberFormatException ex) { return OptionalInt.empty(); }
    }
    public static OptionalLong toLong(String value) {
        if (!CpfStrings.hasText(value)) return OptionalLong.empty();
        try { return OptionalLong.of(Long.parseLong(value.trim())); } catch (NumberFormatException ex) { return OptionalLong.empty(); }
    }
    public static int clamp(int value, int min, int max) {
        if (min > max) throw new IllegalArgumentException("min은 max보다 클 수 없습니다.");
        return Math.max(min, Math.min(max, value));
    }
    public static long clamp(long value, long min, long max) {
        if (min > max) throw new IllegalArgumentException("min은 max보다 클 수 없습니다.");
        return Math.max(min, Math.min(max, value));
    }
}
