package com.cpf.common.calendar;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmnCalendarServiceProductModeTest {
    @Test
    void productModeDoesNotSwallowDurableOutboxFailure() {
        CmnInMemoryCalendarStore store = new CmnInMemoryCalendarStore();
        CmnCalendarChangePublisher failing = event -> {
            throw new IllegalStateException("outbox unavailable");
        };
        CmnCalendarService service = new CmnCalendarService(store, failing, true);

        assertThatThrownBy(() -> service.save(
                new CmnCalendarDay(
                        "DEFAULT", LocalDate.of(2026, 8, 5), true,
                        "BUSINESS", "BANK", "test", 0),
                0,
                "operator"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("outbox unavailable");
    }
}
