package com.cpf.core.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 금융/업무 금액 계산에서 scale과 rounding을 명시하도록 돕는 CPF Decimal API입니다. */
public final class CpfDecimals {
    private CpfDecimals() {}
    /** 금액/수치 문자열을 BigDecimal로 엄격하게 파싱합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 변환/정규화된 Decimal 또는 계약상 null
     * @throws IllegalArgumentException blank 또는 숫자가 아닌 경우
     */
    public static BigDecimal parse(String value) {
        try { return new BigDecimal(requireText(value, "decimal")); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("유효하지 않은 숫자입니다: " + value, ex); }
    }
    /** BigDecimal의 scale과 rounding을 명시적으로 적용합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param scale 적용할 BigDecimal scale
     * @param roundingMode null이면 HALF_UP을 사용할 반올림 방식
     * @return 변환/정규화된 Decimal 또는 계약상 null
     */
    public static BigDecimal scale(BigDecimal value, int scale, RoundingMode roundingMode) {
        if (value == null) return null;
        return value.setScale(scale, roundingMode == null ? RoundingMode.HALF_UP : roundingMode);
    }
    /** null 금액을 ZERO로 정규화합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 변환/정규화된 Decimal 또는 계약상 null
     */
    public static BigDecimal zeroIfNull(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    /** scale과 무관하게 값이 0인지 비교합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 조건을 만족하면 true, 아니면 false
     */
    public static boolean isZero(BigDecimal value) { return value != null && value.compareTo(BigDecimal.ZERO) == 0; }
    private static String requireText(String value, String name) { if(value==null||value.isBlank()) throw new IllegalArgumentException(name+" must not be blank"); return value.trim(); }
}
