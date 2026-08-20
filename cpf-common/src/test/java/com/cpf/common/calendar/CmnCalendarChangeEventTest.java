package com.cpf.common.calendar;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmnCalendarChangeEventTest {
    @Test
    void normalizesSupportedOperationAndCalendarId() {
        CmnCalendarChangeEvent event = new CmnCalendarChangeEvent(
                " upsert ", " BANK ", LocalDate.of(2026, 8, 5), 3, Instant.EPOCH);

        assertThat(event.operation()).isEqualTo("UPSERT");
        assertThat(event.calendarId()).isEqualTo("BANK");
        assertThat(event.version()).isEqualTo(3);
    }

    @Test
    void rejectsUnknownOperationMissingDateAndNonPositiveVersion() {
        assertThatThrownBy(() -> new CmnCalendarChangeEvent(
                "UNKNOWN", "DEFAULT", LocalDate.of(2026, 8, 5), 1, Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported");
        assertThatThrownBy(() -> new CmnCalendarChangeEvent(
                "DELETE", "DEFAULT", null, 1, Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("businessDate");
        assertThatThrownBy(() -> new CmnCalendarChangeEvent(
                "DELETE", "DEFAULT", LocalDate.of(2026, 8, 5), 0, Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version");
    }
}
