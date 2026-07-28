package com.cpf.common.ref.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/** Cache event가 memory queue로 숨겨지지 않고 durable 저장 실패를 호출자에게 전달하는지 검증합니다. */
class CacheRefreshEventPublisherTest {

    @AfterEach
    void clearTransactionState() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void outOfBandPublishPropagatesDatabaseFailureWithoutMemoryFallback() {
        CacheRefreshEventStore store = mock(CacheRefreshEventStore.class);
        doThrow(new IllegalStateException("db down"))
                .when(store).insertOutOfBand(anyString(), anyString(), any(), anyString(), anyString());
        CacheRefreshEventPublisher publisher = new CacheRefreshEventPublisher(store);
        ReflectionTestUtils.setField(publisher, "wasId", "was-1");

        assertThatThrownBy(() -> publisher.publish(
                "responseCodeCache", "UPDATE", "EREF010001", "operator"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("db down");

        assertThat(publisher.status())
                .containsEntry("durable", true)
                .containsEntry("memoryRetryQueue", false)
                .containsEntry("publishedCount", 0L)
                .containsEntry("failedCount", 1L);
    }

    @Test
    void requiredPublishParticipatesInActiveBusinessTransaction() {
        CacheRefreshEventStore store = mock(CacheRefreshEventStore.class);
        CacheRefreshEventPublisher publisher = new CacheRefreshEventPublisher(store);
        ReflectionTestUtils.setField(publisher, "wasId", "was-1");
        TransactionSynchronizationManager.setActualTransactionActive(true);

        publisher.publishRequired("codeCache", "UPSERT", "BANK_CODE", "operator");

        verify(store).insertRequired("codeCache", "UPSERT", "BANK_CODE", "was-1", "operator");
        verify(store, never()).insertOutOfBand(anyString(), anyString(), any(), anyString(), anyString());
        assertThat(publisher.status()).containsEntry("publishedCount", 1L);
    }
}
