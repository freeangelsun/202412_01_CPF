package com.cpf.common.calendar;

import com.cpf.common.ref.service.CacheRefreshEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CmnDurableCalendarChangePublisherTest {
    @Test
    void writesCalendarChangeToRequiredDurableOutbox() {
        CacheRefreshEventPublisher delegate = mock(CacheRefreshEventPublisher.class);
        CmnDurableCalendarChangePublisher publisher = new CmnDurableCalendarChangePublisher(delegate);

        publisher.publish(new CmnCalendarChangeEvent(
                "UPSERT", "BANK", LocalDate.of(2026, 8, 5), 7, Instant.EPOCH));

        verify(delegate).publishRequired(
                "businessCalendar", "UPSERT", "BANK:2026-08-05:7", "CMN_CALENDAR");
    }
}
