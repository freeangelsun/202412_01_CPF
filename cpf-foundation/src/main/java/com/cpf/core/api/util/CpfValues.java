package com.cpf.core.api.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Map/전문/JSON 값의 반복적인 안전 형변환을 줄이는 공개 utility입니다. */
public final class CpfValues {
    private CpfValues(){}
    /** 값을 nullable 문자열로 변환합니다.
     * @param v 변환할 nullable 입력 값
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String string(Object v){ return v==null?null:String.valueOf(v); }
    /** 값을 nullable Integer로 변환합니다.
     * @param v 변환할 nullable 입력 값
     * @return 변환된 Integer 또는 입력이 null이면 null
     * @throws NumberFormatException 숫자로 변환할 수 없는 문자열인 경우
     */
    public static Integer integer(Object v){ if(v==null)return null; if(v instanceof Number n)return n.intValue(); return Integer.valueOf(String.valueOf(v).trim()); }
    /** 값을 nullable Long으로 변환합니다.
     * @param v 변환할 nullable 입력 값
     * @return 변환된 Long 또는 입력이 null이면 null
     * @throws NumberFormatException 숫자로 변환할 수 없는 문자열인 경우
     */
    public static Long longValue(Object v){ if(v==null)return null; if(v instanceof Number n)return n.longValue(); return Long.valueOf(String.valueOf(v).trim()); }
    /** 값을 nullable BigDecimal로 변환합니다.
     * @param v 변환할 nullable 입력 값
     * @return 변환/정규화된 Decimal 또는 계약상 null
     * @throws NumberFormatException Decimal로 변환할 수 없는 문자열인 경우
     */
    public static BigDecimal decimal(Object v){ if(v==null)return null; if(v instanceof BigDecimal b)return b; return new BigDecimal(String.valueOf(v).trim()); }
    /** Boolean/Y/N/1/0/true/false 값만 허용해 nullable Boolean으로 변환합니다.
     * @param v 변환할 nullable 입력 값
     * @return 변환된 Boolean 또는 입력이 null이면 null
     * @throws IllegalArgumentException 허용된 boolean 표현이 아닌 경우
     */
    public static Boolean bool(Object v){ if(v==null)return null; if(v instanceof Boolean b)return b; String s=String.valueOf(v).trim(); if("Y".equalsIgnoreCase(s)||"1".equals(s)||"true".equalsIgnoreCase(s))return true; if("N".equalsIgnoreCase(s)||"0".equals(s)||"false".equalsIgnoreCase(s))return false; throw new IllegalArgumentException("Boolean으로 변환할 수 없습니다: "+s); }
    /** 값을 CPF 날짜 규칙의 nullable LocalDate로 변환합니다.
     * @param v 변환할 nullable 입력 값
     * @return 변환된 날짜 또는 계약상 null
     * @throws IllegalArgumentException 날짜 형식이 유효하지 않은 경우
     */
    public static LocalDate date(Object v){ return v==null?null:CpfDates.parse(String.valueOf(v)); }
    /** 값을 ISO-8601 nullable Instant로 변환합니다.
     * @param v 변환할 nullable 입력 값
     * @return 변환된 Instant 또는 계약상 null
     * @throws java.time.format.DateTimeParseException ISO Instant 형식이 아닌 경우
     */
    public static Instant instant(Object v){ return v==null?null:Instant.parse(String.valueOf(v).trim()); }
}
