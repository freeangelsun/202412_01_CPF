package com.cpf.common.calendar;

import java.time.Instant;
import java.time.LocalDate;

/** 영업일 원장의 변경을 cache/provider adapter에 전달하는 불변 이벤트입니다. */
public record CmnCalendarChangeEvent(
        String operation,
        String calendarId,
        LocalDate businessDate,
        long version,
        Instant changedAt) {
    public CmnCalendarChangeEvent {
        operation = operation == null || operation.isBlank() ? "UNKNOWN" : operation;
        calendarId = calendarId == null || calendarId.isBlank() ? "DEFAULT" : calendarId.trim();
        changedAt = changedAt == null ? Instant.now() : changedAt;
    }
}
