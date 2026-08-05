package com.cpf.common.calendar;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

/** 영업일 원장의 변경을 cache/provider adapter에 전달하는 불변 이벤트입니다. */
public record CmnCalendarChangeEvent(
        String operation,
        String calendarId,
        LocalDate businessDate,
        long version,
        Instant changedAt) {
    private static final Set<String> OPERATIONS = Set.of("UPSERT", "DELETE");

    public CmnCalendarChangeEvent {
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("operation is required");
        }
        operation = operation.trim().toUpperCase(Locale.ROOT);
        if (!OPERATIONS.contains(operation)) {
            throw new IllegalArgumentException("Unsupported Calendar change operation: " + operation);
        }
        calendarId = calendarId == null || calendarId.isBlank() ? "DEFAULT" : calendarId.trim();
        if (businessDate == null) {
            throw new IllegalArgumentException("businessDate is required");
        }
        if (version <= 0) {
            throw new IllegalArgumentException("version must be greater than zero");
        }
        changedAt = changedAt == null ? Instant.now() : changedAt;
    }
}
