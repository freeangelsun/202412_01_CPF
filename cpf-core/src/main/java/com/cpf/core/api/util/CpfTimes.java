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

    public static Instant now() { return Instant.now(); }
    public static OffsetDateTime now(ZoneId zoneId) { return OffsetDateTime.now(zoneId); }
    public static String today() { return LocalDate.now().format(DATE_FORMAT); }
    public static String nowDateTime() { return LocalDateTime.now().format(DATETIME_FORMAT); }
    public static String nowDateTimeMillis() { return LocalDateTime.now().format(DATETIME_MILLIS_FORMAT); }

    public static Instant toInstant(LocalDateTime value, ZoneId zoneId) {
        if (value == null) return null;
        if (zoneId == null) throw new IllegalArgumentException("zoneId는 필수입니다.");
        return value.atZone(zoneId).toInstant();
    }

    public static LocalDateTime toLocalDateTime(Instant value, ZoneId zoneId) {
        if (value == null) return null;
        if (zoneId == null) throw new IllegalArgumentException("zoneId는 필수입니다.");
        return LocalDateTime.ofInstant(value, zoneId);
    }
}
