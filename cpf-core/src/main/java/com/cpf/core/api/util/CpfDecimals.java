package com.cpf.core.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 금융/업무 금액 계산에서 scale과 rounding을 명시하도록 돕는 CPF Decimal API입니다. */
public final class CpfDecimals {
    private CpfDecimals() {}
    public static BigDecimal parse(String value) {
        try { return new BigDecimal(CpfStrings.requireText(value, "decimal")); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("유효하지 않은 숫자입니다: " + value, ex); }
    }
    public static BigDecimal scale(BigDecimal value, int scale, RoundingMode roundingMode) {
        if (value == null) return null;
        return value.setScale(scale, roundingMode == null ? RoundingMode.HALF_UP : roundingMode);
    }
    public static BigDecimal zeroIfNull(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    public static boolean isZero(BigDecimal value) { return value != null && value.compareTo(BigDecimal.ZERO) == 0; }
}
