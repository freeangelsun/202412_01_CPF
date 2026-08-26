package com.cpf.common.calendar;

import com.cpf.common.calendar.api.CpfCalendarService;
import java.time.LocalDate;

/**
 * 고객사 영업일/휴일 정책의 공개 계약입니다.
 *
 * <p>Batch/Scheduler/Generated Domain은 저장소 구현을 직접 알지 않고 이 계약만 소비합니다.</p>
 */
@Deprecated(forRemoval = false)
public interface CmnBusinessCalendar extends CpfCalendarService {
    @Deprecated
    boolean isBusinessDay(String calendarId, LocalDate date);

    /** offset=1은 다음 영업일, offset=-1은 이전 영업일입니다. */
    @Deprecated
    LocalDate shiftBusinessDay(String calendarId, LocalDate from, int offset);

}
