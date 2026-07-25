package com.cpf.common.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 영업일 Override 저장소 SPI입니다.
 *
 * <p>CPF 제품 설치의 기본 구현은 CMN canonical calendar table을 사용합니다.
 * 고객이 외부 휴일/시장 Calendar를 사용해야 하는 경우 이 Port를 구현해 교체할 수 있습니다.</p>
 */
public interface CmnCalendarStore {
    Optional<CmnCalendarDay> find(String calendarId, LocalDate businessDate);
    List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit);
    CmnCalendarDay save(CmnCalendarDay day, long expectedVersion);
    void delete(String calendarId, LocalDate businessDate, long expectedVersion);

    /** 영속 변경을 지원하지 않는 조회 전용 Provider는 false를 반환합니다. */
    default boolean writable() { return true; }
}
