package com.cpf.common.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DB-less Library 모드에서 사용하는 조회 전용 Calendar Store입니다.
 *
 * <p>Override는 보유하지 않으며 {@link CmnCalendarService}가 토/일 기본정책을 적용합니다.
 * 운영자가 변경한 값을 JVM 메모리에만 남기는 위험을 막기 위해 write는 fail-closed합니다.</p>
 */
final class CmnWeekendCalendarStore implements CmnCalendarStore {
    @Override
    public Optional<CmnCalendarDay> find(String calendarId, LocalDate businessDate) {
        return Optional.empty();
    }

    @Override
    public List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit) {
        return List.of();
    }

    @Override
    public CmnCalendarDay save(CmnCalendarDay day, long expectedVersion) {
        throw new IllegalStateException("DB-less Calendar는 조회 전용입니다. CMN Calendar Store를 구성하십시오.");
    }

    @Override
    public void delete(String calendarId, LocalDate businessDate, long expectedVersion) {
        throw new IllegalStateException("DB-less Calendar는 조회 전용입니다. CMN Calendar Store를 구성하십시오.");
    }

    @Override
    public boolean writable() {
        return false;
    }
}
