package com.cpf.foundation.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/** Business-date policy with injectable holiday calendar. */
public final class CpfBusinessDate {
    private final Predicate<LocalDate> holiday;
    public CpfBusinessDate(Predicate<LocalDate> holiday) { this.holiday = Objects.requireNonNull(holiday, "holiday"); }
    public static CpfBusinessDate weekendsOnly() { return new CpfBusinessDate(d -> false); }
    public static CpfBusinessDate withHolidays(Set<LocalDate> holidays) { var copy = Set.copyOf(holidays); return new CpfBusinessDate(copy::contains); }
    public boolean isBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY && !holiday.test(date);
    }
    public LocalDate next(LocalDate date) { LocalDate v=date.plusDays(1); while(!isBusinessDay(v)) v=v.plusDays(1); return v; }
    public LocalDate previous(LocalDate date) { LocalDate v=date.minusDays(1); while(!isBusinessDay(v)) v=v.minusDays(1); return v; }
    public LocalDate shift(LocalDate date, int businessDays) {
        if (businessDays == 0) return date;
        int step = businessDays > 0 ? 1 : -1, remaining = Math.abs(businessDays); LocalDate v=date;
        while (remaining > 0) { v=v.plusDays(step); if (isBusinessDay(v)) remaining--; }
        return v;
    }
}
