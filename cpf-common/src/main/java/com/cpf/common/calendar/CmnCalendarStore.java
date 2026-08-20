package com.cpf.common.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** 영업일 Override 저장소 SPI입니다. */
public interface CmnCalendarStore {
    Optional<CmnCalendarDay> find(String calendarId, LocalDate businessDate);
    List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit);
    CmnCalendarDay save(CmnCalendarDay day, long expectedVersion);
    void delete(String calendarId, LocalDate businessDate, long expectedVersion);
    default CmnCalendarDay save(CmnCalendarDay day, long expectedVersion, String operatorId) { return save(day, expectedVersion); }
    default void delete(String calendarId, LocalDate businessDate, long expectedVersion, String operatorId) { delete(calendarId, businessDate, expectedVersion); }
    default boolean writable() { return true; }
    /** 제품용 writable Store가 실제 actor를 원장에 기록할 수 있는지 표시합니다. */
    default boolean actorAwareMutations() { return false; }
}
