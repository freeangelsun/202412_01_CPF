package com.cpf.common.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** DB-less Library/EDU/Test 전용 조회 Store입니다. 저장은 허용하지 않습니다. */
public final class CmnWeekendCalendarStore implements CmnCalendarStore {
    @Override public Optional<CmnCalendarDay> find(String calendarId, LocalDate businessDate) { return Optional.empty(); }
    @Override public List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit) { return List.of(); }
    @Override public CmnCalendarDay save(CmnCalendarDay day, long expectedVersion) { throw new IllegalStateException("DB-less Calendar는 조회 전용입니다."); }
    @Override public void delete(String calendarId, LocalDate businessDate, long expectedVersion) { throw new IllegalStateException("DB-less Calendar는 조회 전용입니다."); }
    @Override public boolean writable() { return false; }
}
