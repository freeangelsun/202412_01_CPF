package com.cpf.core.api.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** Timestamp/Instant 변환에서 timezone 누락을 줄이는 CPF 시간 API입니다. */
public final class CpfTimes {
    private CpfTimes() {}
    public static Instant now() { return Instant.now(); }
    public static OffsetDateTime now(ZoneId zoneId) { return OffsetDateTime.now(zoneId); }
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
