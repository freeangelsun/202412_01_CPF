package com.cpf.foundation.util;

import com.cpf.core.api.error.CpfValidationException;
import java.math.BigDecimal;

/** 외부 입력 값을 안전한 기본 타입으로 변환하는 기술중립 Utility입니다. */
public final class CpfValues {
    private CpfValues() {
    }

    public static Integer integer(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            long converted = number.longValue();
            if (converted < Integer.MIN_VALUE || converted > Integer.MAX_VALUE) {
                throw new CpfValidationException("integer 범위를 벗어났습니다.");
            }
            return (int) converted;
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (RuntimeException ex) {
            throw new CpfValidationException("integer 값으로 변환할 수 없습니다.");
        }
    }

    public static BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (RuntimeException ex) {
            throw new CpfValidationException("decimal 값으로 변환할 수 없습니다.");
        }
    }

    public static Boolean bool(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String normalized = value.toString().trim().toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "true", "1", "y", "yes", "on" -> Boolean.TRUE;
            case "false", "0", "n", "no", "off" -> Boolean.FALSE;
            default -> throw new CpfValidationException("boolean 값으로 변환할 수 없습니다.");
        };
    }
}
