package com.cpf.core.api.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Timestamp/Instant 변환과 CPF 표준 날짜 문자열을 제공하는 공개 시간 API입니다. */
public final class CpfTimes {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter DATETIME_MILLIS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private CpfTimes() {}

    /** 현재 Instant를 반환합니다.

     * @return 변환된 Instant 또는 계약상 null

     */

    public static Instant now() { return Instant.now(); }
    /** 지정 Zone의 현재 OffsetDateTime을 반환합니다.
     * @param zoneId null이 아닌 시간대
     * @return 지정 Zone의 현재 시각
     */
    public static OffsetDateTime now(ZoneId zoneId) { return OffsetDateTime.now(zoneId); }
    /** 시스템 기본 Zone의 오늘을 yyyyMMdd로 반환합니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String today() { return LocalDate.now().format(DATE_FORMAT); }
    /** 시스템 기본 Zone 현재시각을 yyyyMMddHHmmss로 반환합니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String nowDateTime() { return LocalDateTime.now().format(DATETIME_FORMAT); }
    /** 시스템 기본 Zone 현재시각을 millisecond 포함 표준 문자열로 반환합니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String nowDateTimeMillis() { return LocalDateTime.now().format(DATETIME_MILLIS_FORMAT); }

    /** LocalDateTime을 명시 Zone 기준 Instant로 변환합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @param zoneId null이 아닌 시간대

     * @return 변환된 Instant 또는 계약상 null

     * @throws IllegalArgumentException value가 null이 아닌데 zoneId가 null인 경우

     */

    public static Instant toInstant(LocalDateTime value, ZoneId zoneId) {
        if (value == null) return null;
        if (zoneId == null) throw new IllegalArgumentException("zoneId는 필수입니다.");
        return value.atZone(zoneId).toInstant();
    }

    /** Instant를 명시 Zone 기준 LocalDateTime으로 변환합니다.

     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.

     * @param zoneId null이 아닌 시간대

     * @return 변환된 날짜 또는 계약상 null

     * @throws IllegalArgumentException value가 null이 아닌데 zoneId가 null인 경우

     */

    public static LocalDateTime toLocalDateTime(Instant value, ZoneId zoneId) {
        if (value == null) return null;
        if (zoneId == null) throw new IllegalArgumentException("zoneId는 필수입니다.");
        return LocalDateTime.ofInstant(value, zoneId);
    }
}
