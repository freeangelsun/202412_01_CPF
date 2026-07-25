package com.cpf.common.ref.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CacheRefreshEventPublisherTest {
    @Test
    void failedDeliveryIsQueuedAndCanBeRetriedWithoutBusinessException() {
        CacheRefreshEventStore store = mock(CacheRefreshEventStore.class);
        doThrow(new IllegalStateException("db down"))
                .doNothing()
                .when(store).insert(anyString(), anyString(), anyString(), any(), anyString());
        CacheRefreshEventPublisher publisher = new CacheRefreshEventPublisher(store);

        publisher.publish("responseCodeCache", "UPDATE", "EREF010001", "operator");
        assertThat(publisher.status().get("pendingCount")).isEqualTo(1);
        assertThat(publisher.status().get("failedCount")).isEqualTo(1L);

        publisher.retryPendingEvents();
        assertThat(publisher.status().get("pendingCount")).isEqualTo(0);
        assertThat(publisher.status().get("retriedCount")).isEqualTo(1L);
        verify(store, times(2)).insert(anyString(), anyString(), anyString(), any(), anyString());
    }
}
