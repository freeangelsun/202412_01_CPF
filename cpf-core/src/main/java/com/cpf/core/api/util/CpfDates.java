package com.cpf.core.api.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** LocalDate 중심의 CPF 날짜 API입니다. 영업일 계산은 cpf-common Calendar 계약을 사용합니다. */
public final class CpfDates {
    public static final DateTimeFormatter BASIC = DateTimeFormatter.BASIC_ISO_DATE;
    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private CpfDates() {}

    public static LocalDate parse(String value) { return parse(value, ISO); }
    public static LocalDate parseBasic(String value) { return parse(value, BASIC); }
    public static LocalDate parse(String value, DateTimeFormatter formatter) {
        try { return LocalDate.parse(CpfStrings.requireText(value, "date"), formatter); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("유효하지 않은 날짜입니다: " + value, ex); }
    }
    public static LocalDate parseOrNull(String value) {
        if (!CpfStrings.hasText(value)) return null;
        try { return parse(value); } catch (IllegalArgumentException ex) { return null; }
    }
    public static String format(LocalDate value) { return value == null ? null : ISO.format(value); }
    public static String formatBasic(LocalDate value) { return value == null ? null : BASIC.format(value); }
    public static boolean betweenInclusive(LocalDate value, LocalDate from, LocalDate to) {
        if (value == null) return false;
        return (from == null || !value.isBefore(from)) && (to == null || !value.isAfter(to));
    }
}
