package com.cpf.common.calendar.api;

import java.time.LocalDate;

/**
 * 고객 업무에서 영업일을 조회·이동할 때 사용하는 CPF Common Calendar 공개 API입니다.
 *
 * <p>개발자는 저장소나 휴일 Provider를 직접 조회하지 않고 이 계약을 주입받아 사용합니다.
 * 임의의 영업일 이동은 {@link #shiftBusinessDay(String, LocalDate, int)}, 바로 다음/이전
 * 영업일은 {@link #nextBusinessDay(String, LocalDate)} / {@link #previousBusinessDay(String, LocalDate)}를 사용합니다.</p>
 */
public interface CpfCalendarService {
    /** 지정 일자가 해당 Calendar의 영업일인지 반환합니다. */
    boolean isBusinessDay(String calendarId, LocalDate date);

    /**
     * 지정 일자에서 offset만큼 영업일을 이동합니다.
     * offset 양수는 미래, 음수는 과거, 0은 입력 일자를 그대로 반환합니다.
     */
    LocalDate shiftBusinessDay(String calendarId, LocalDate from, int offset);

    /** 바로 다음 영업일을 반환합니다. */
    default LocalDate nextBusinessDay(String calendarId, LocalDate from) {
        return shiftBusinessDay(calendarId, from, 1);
    }

    /** 바로 이전 영업일을 반환합니다. */
    default LocalDate previousBusinessDay(String calendarId, LocalDate from) {
        return shiftBusinessDay(calendarId, from, -1);
    }
}
