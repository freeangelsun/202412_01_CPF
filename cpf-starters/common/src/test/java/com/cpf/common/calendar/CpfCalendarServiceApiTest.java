package com.cpf.common.calendar;

import com.cpf.common.calendar.api.CpfCalendarService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class CpfCalendarServiceApiTest {
    @Test void exposesClearNextPreviousAndShiftNames() {
        CpfCalendarService calendar = new CmnWeekendCalendar();
        LocalDate friday = LocalDate.of(2026, 8, 14);
        assertThat(calendar.nextBusinessDay("DEFAULT", friday)).isEqualTo(LocalDate.of(2026, 8, 17));
        assertThat(calendar.previousBusinessDay("DEFAULT", LocalDate.of(2026, 8, 17))).isEqualTo(friday);
        assertThat(calendar.shiftBusinessDay("DEFAULT", friday, 2)).isEqualTo(LocalDate.of(2026, 8, 18));
    }
}
