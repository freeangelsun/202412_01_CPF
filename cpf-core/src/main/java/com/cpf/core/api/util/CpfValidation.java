package com.cpf.core.api.util;

import java.util.Collection;

/** Controller/Service의 반복적인 방어 검증을 간단하게 표현하는 CPF API입니다. */
public final class CpfValidation {
    private CpfValidation() {}
    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    public static String requireText(String value, String name) { return CpfStrings.requireText(value, name); }
    public static String maxLength(String value, String name, int max) {
        if (value != null && value.length() > max) throw new IllegalArgumentException(name + "은(는) " + max + "자를 초과할 수 없습니다.");
        return value;
    }
    public static <T extends Collection<?>> T notEmpty(T value, String name) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException(name + "은(는) 비어 있을 수 없습니다.");
        return value;
    }
}
