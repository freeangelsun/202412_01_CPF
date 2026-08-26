package com.cpf.common.calendar;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** DB 없는 Unit/EDU를 위한 thread-safe Calendar Store입니다. 운영 영업일 정본으로 사용하지 않습니다. */
public final class CmnInMemoryCalendarStore implements CmnCalendarStore {
    private final ConcurrentMap<String,CmnCalendarDay> values = new ConcurrentHashMap<>();

    @Override
    public Optional<CmnCalendarDay> find(String calendarId, LocalDate businessDate) {
        return Optional.ofNullable(values.get(key(calendarId, businessDate)));
    }

    @Override
    public List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        return values.values().stream()
                .filter(v -> normalize(calendarId).equals(v.calendarId()))
                .filter(v -> from == null || !v.businessDate().isBefore(from))
                .filter(v -> to == null || !v.businessDate().isAfter(to))
                .sorted(Comparator.comparing(value -> value.businessDate()))
                .limit(safeLimit)
                .toList();
    }

    @Override
    public synchronized CmnCalendarDay save(CmnCalendarDay day, long expectedVersion) {
        String key = key(day.calendarId(), day.businessDate());
        CmnCalendarDay current = values.get(key);
        long actualVersion = current == null ? 0L : current.version();
        if (actualVersion != expectedVersion) {
            throw new IllegalStateException("Calendar version 충돌입니다. expected=" + expectedVersion + ", actual=" + actualVersion);
        }
        CmnCalendarDay saved = new CmnCalendarDay(
                day.calendarId(), day.businessDate(), day.businessDay(), day.dayType(),
                day.institutionCode(), day.reason(), actualVersion + 1);
        values.put(key, saved);
        return saved;
    }

    @Override
    public synchronized void delete(String calendarId, LocalDate businessDate, long expectedVersion) {
        String key = key(calendarId, businessDate);
        CmnCalendarDay current = values.get(key);
        if (current == null) return;
        if (current.version() != expectedVersion) {
            throw new IllegalStateException("Calendar version 충돌입니다. expected=" + expectedVersion + ", actual=" + current.version());
        }
        values.remove(key);
    }

    private String key(String calendarId, LocalDate date) { return normalize(calendarId) + "|" + date; }
    private String normalize(String calendarId) { return calendarId == null || calendarId.isBlank() ? "DEFAULT" : calendarId.trim(); }
}
