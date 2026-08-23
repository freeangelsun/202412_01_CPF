package com.cpf.common.runtime.cache;

import com.cpf.foundation.runtime.CpfInstanceIdentity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Durable Common cache event consumer의 gap/retry/bootstrap 회귀 Test입니다.
 */
class CpfCommonCacheRefreshListenerTest {

    @Test
    void eventGapIsReplayedAndDuplicateEventIdIsIgnored() throws Exception {
        var repository = mock(CpfCommonCacheRefreshEventRepository.class);
        var refresher = mock(CpfCommonCacheRefresher.class);
        String consumerId = consumerId();
        when(repository.checkpoint(consumerId)).thenReturn(3L);
        when(repository.findAfter(3L, 256)).thenReturn(List.of(
                event(5L, "codeCache"),
                event(5L, "codeCache"),
                event(8L, "messageCache")));

        var listener = listener(repository, refresher);
        listener.initialize();
        listener.poll();

        verify(refresher).refresh("codeCache");
        verify(refresher).refresh("messageCache");
        verify(repository).advanceCheckpoint(consumerId, 5L, "SYSTEM");
        verify(repository).advanceCheckpoint(consumerId, 8L, "SYSTEM");
        assertThat(listener.status().lastEventId()).isEqualTo(8L);
    }

    @Test
    void cacheFailureDoesNotAdvanceCheckpointAndNextPollRetriesSameEvent() throws Exception {
        var repository = mock(CpfCommonCacheRefreshEventRepository.class);
        var refresher = mock(CpfCommonCacheRefresher.class);
        String consumerId = consumerId();
        when(repository.checkpoint(consumerId)).thenReturn(10L);
        when(repository.findAfter(10L, 256)).thenReturn(List.of(event(11L, "parameterCache")));
        doThrow(new IllegalStateException("cache outage"))
                .doNothing()
                .when(refresher).refresh("parameterCache");

        var listener = listener(repository, refresher);
        listener.initialize();
        listener.poll();

        verify(repository, never()).advanceCheckpoint(consumerId, 11L, "SYSTEM");
        assertThat(listener.status().lastEventId()).isEqualTo(10L);
        assertThat(listener.status().lastFailureType()).isEqualTo("IllegalStateException");

        listener.poll();

        verify(repository).advanceCheckpoint(consumerId, 11L, "SYSTEM");
        verify(refresher, times(2)).refresh("parameterCache");
        assertThat(listener.status().lastEventId()).isEqualTo(11L);
        assertThat(listener.status().lastFailureType()).isNull();
    }

    @Test
    void firstInstanceBootstrapsFromHighWaterAfterFullRefresh() throws Exception {
        var repository = mock(CpfCommonCacheRefreshEventRepository.class);
        var refresher = mock(CpfCommonCacheRefresher.class);
        String consumerId = consumerId();
        when(repository.checkpoint(consumerId)).thenReturn(null, 77L);
        when(repository.maxEventId()).thenReturn(77L);

        var listener = listener(repository, refresher);
        listener.initialize();

        verify(refresher).refreshAll();
        verify(repository).establishCheckpoint(consumerId, 77L, "SYSTEM");
        assertThat(listener.status().lastEventId()).isEqualTo(77L);
        assertThat(listener.status().lastFailureType()).isNull();
    }

    private static CpfCommonCacheRefreshListener listener(
            CpfCommonCacheRefreshEventRepository repository,
            CpfCommonCacheRefresher refresher) throws Exception {
        var listener = new CpfCommonCacheRefreshListener(repository, refresher);
        set(listener, "enabled", true);
        set(listener, "pollLimit", 256);
        return listener;
    }

    private static String consumerId() {
        return "CMN_CACHE:" + CpfInstanceIdentity.instanceId();
    }

    private static Map<String, Object> event(long id, String cacheName) {
        return Map.of("event_id", id, "cache_name", cacheName);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
