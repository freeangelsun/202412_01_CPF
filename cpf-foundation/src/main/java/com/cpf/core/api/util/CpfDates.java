package com.cpf.core.api.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** LocalDate 중심의 CPF 날짜 API입니다. 영업일 계산은 cpf-common Calendar 계약을 사용합니다. */
public final class CpfDates {
    public static final DateTimeFormatter BASIC = DateTimeFormatter.BASIC_ISO_DATE;
    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private CpfDates() {}

    /** ISO 또는 지정 formatter로 날짜 문자열을 엄격하게 파싱합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @return 변환된 날짜 또는 계약상 null

     * @throws IllegalArgumentException 입력이 blank이거나 날짜 형식이 유효하지 않은 경우

     */

    public static LocalDate parse(String value) { return parse(value, ISO); }
    /** yyyyMMdd BASIC 형식의 날짜를 파싱합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 변환된 날짜 또는 계약상 null
     * @throws IllegalArgumentException 입력이 blank이거나 yyyyMMdd 형식이 아닌 경우
     */
    public static LocalDate parseBasic(String value) { return parse(value, BASIC); }
    /** ISO 또는 지정 formatter로 날짜 문자열을 엄격하게 파싱합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param formatter null이 아닌 날짜 formatter
     * @return 변환된 날짜 또는 계약상 null
     * @throws IllegalArgumentException 입력이 blank이거나 날짜 형식이 유효하지 않은 경우
     */
    public static LocalDate parse(String value, DateTimeFormatter formatter) {
        try { return LocalDate.parse(requireText(value, "date"), formatter); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("유효하지 않은 날짜입니다: " + value, ex); }
    }
    /** blank/invalid 입력을 예외 대신 null로 정규화해 선택 입력에 사용합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 변환된 날짜 또는 계약상 null
     */
    public static LocalDate parseOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try { return parse(value); } catch (IllegalArgumentException ex) { return null; }
    }
    /** LocalDate를 ISO yyyy-MM-dd 문자열로 변환합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String format(LocalDate value) { return value == null ? null : ISO.format(value); }
    /** LocalDate를 yyyyMMdd 문자열로 변환합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String formatBasic(LocalDate value) { return value == null ? null : BASIC.format(value); }
    /** 날짜가 선택적인 시작/종료 경계 안에 포함되는지 판단합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @param from 포함 시작일. null이면 하한 없음
     * @param to 포함 종료일. null이면 상한 없음
     * @return 조건을 만족하면 true, 아니면 false
     */
    public static boolean betweenInclusive(LocalDate value, LocalDate from, LocalDate to) {
        if (value == null) return false;
        return (from == null || !value.isBefore(from)) && (to == null || !value.isAfter(to));
    }
    private static String requireText(String value, String name) { if(value==null||value.isBlank()) throw new IllegalArgumentException(name+" must not be blank"); return value.trim(); }
}
