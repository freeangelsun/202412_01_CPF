package com.cpf.common.calendar;

import com.cpf.common.ref.service.CacheRefreshEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Product-mode Calendar change publisher backed by the shared durable cmnDB cache-refresh outbox.
 *
 * <p>The adapter calls {@code publishRequired}; therefore it must participate in the caller's
 * {@code cmnTransactionManager} transaction. An outbox insert failure is propagated and rolls back
 * the Calendar mutation. The durable listener/retry/reconcile path is shared with other CMN caches.</p>
 */
@Component
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
@ConditionalOnMissingBean(CmnCalendarChangePublisher.class)
public final class CmnDurableCalendarChangePublisher implements CmnCalendarChangePublisher {
    static final String CACHE_NAME = "businessCalendar";
    private final CacheRefreshEventPublisher publisher;

    public CmnDurableCalendarChangePublisher(CacheRefreshEventPublisher publisher) {
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    @Override
    public void publish(CmnCalendarChangeEvent event) {
        Objects.requireNonNull(event, "event");
        String eventKey = event.calendarId() + ":" + event.businessDate() + ":" + event.version();
        publisher.publishRequired(CACHE_NAME, event.operation(), eventKey, "CMN_CALENDAR");
    }
}
