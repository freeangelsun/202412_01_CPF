package com.cpf.common.calendar;

import java.time.LocalDate;
import java.util.Objects;

/** 영업일/휴일 Override 한 건을 표현하는 고객 업무공통 값 객체입니다. */
public record CmnCalendarDay(
        String calendarId,
        LocalDate businessDate,
        boolean businessDay,
        String dayType,
        String institutionCode,
        String reason,
        long version) {

    public CmnCalendarDay {
        calendarId = normalize(calendarId, "DEFAULT");
        businessDate = Objects.requireNonNull(businessDate, "businessDate");
        dayType = normalize(dayType, businessDay ? "BUSINESS" : "HOLIDAY");
        institutionCode = normalize(institutionCode, "");
        reason = reason == null ? "" : reason.trim();
        if (version < 0) throw new IllegalArgumentException("version은 0 이상이어야 합니다.");
    }

    private static String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
