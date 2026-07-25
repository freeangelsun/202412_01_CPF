package com.cpf.common.calendar;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * CPF 고객 업무공통 영업일 Service입니다.
 *
 * <p>저장된 Override가 있으면 우선 적용하고, 없으면 토/일을 휴일로 판단합니다.
 * 정식 제품 구성은 CMN canonical store를 사용합니다. DB-less Library 모드에서는
 * 토/일 기본정책만 제공하는 조회 전용 fallback으로 동작하며 운영 변경은 fail-closed합니다.</p>
 */
@Service
public class CmnCalendarService implements CmnBusinessCalendar {
    private static final int MAX_SHIFT_DAYS = 3660;
    private final CmnCalendarStore store;

    @Autowired
    public CmnCalendarService(ObjectProvider<CmnCalendarStore> storeProvider) {
        CmnCalendarStore resolved = storeProvider.getIfAvailable();
        this.store = resolved == null ? new CmnWeekendCalendarStore() : resolved;
    }

    /** Unit/EDU에서 명시적인 Store를 주입할 때 사용합니다. */
    CmnCalendarService(CmnCalendarStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public boolean isBusinessDay(String calendarId, LocalDate date) {
        Objects.requireNonNull(date, "date");
        return store.find(normalize(calendarId), date)
                .map(CmnCalendarDay::businessDay)
                .orElseGet(() -> date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY);
    }

    @Override
    public LocalDate shiftBusinessDay(String calendarId, LocalDate from, int offset) {
        Objects.requireNonNull(from, "from");
        if (offset == 0) return from;
        int direction = offset > 0 ? 1 : -1;
        int remaining = Math.abs(offset);
        LocalDate cursor = from;
        int guard = 0;
        while (remaining > 0) {
            cursor = cursor.plusDays(direction);
            if (isBusinessDay(calendarId, cursor)) remaining--;
            if (++guard > MAX_SHIFT_DAYS) throw new IllegalStateException("영업일 계산 한도를 초과했습니다.");
        }
        return cursor;
    }

    public List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit) {
        return store.findRange(normalize(calendarId), from, to, limit);
    }

    public CmnCalendarDay save(CmnCalendarDay day, long expectedVersion) {
        requireWritable();
        return store.save(day, expectedVersion);
    }

    public void delete(String calendarId, LocalDate businessDate, long expectedVersion) {
        requireWritable();
        store.delete(normalize(calendarId), businessDate, expectedVersion);
    }

    public boolean writable() { return store.writable(); }

    private void requireWritable() {
        if (!store.writable()) {
            throw new IllegalStateException("Calendar Store가 조회 전용입니다. 운영 Override 저장소를 구성하십시오.");
        }
    }

    private String normalize(String calendarId) {
        return calendarId == null || calendarId.isBlank() ? "DEFAULT" : calendarId.trim();
    }
}
