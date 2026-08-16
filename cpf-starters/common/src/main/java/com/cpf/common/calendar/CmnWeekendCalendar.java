package com.cpf.common.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

/** DB 없이 사용할 수 있는 기본 주말 Calendar. 고객사는 Bean/SPI로 교체합니다. */
public final class CmnWeekendCalendar implements CmnBusinessCalendar {
    @Override
    public boolean isBusinessDay(String calendarId, LocalDate date) {
        DayOfWeek dayOfWeek = Objects.requireNonNull(date, "date").getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    @Override
    public LocalDate shiftBusinessDay(String calendarId, LocalDate from, int offset) {
        LocalDate cursor = Objects.requireNonNull(from, "from");
        if (offset == 0) {
            return cursor;
        }
        int direction = offset > 0 ? 1 : -1;
        int remaining = Math.abs(offset);
        while (remaining > 0) {
            cursor = cursor.plusDays(direction);
            if (isBusinessDay(calendarId, cursor)) {
                remaining--;
            }
        }
        return cursor;
    }
}
