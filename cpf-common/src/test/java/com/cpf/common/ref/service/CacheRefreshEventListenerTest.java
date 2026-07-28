package com.cpf.common.ref.service;

import com.cpf.common.cde.service.CodeCacheService;
import com.cpf.common.cfg.service.ConfigCacheService;
import com.cpf.common.msg.service.MessageCacheService;
import com.cpf.common.msg.service.ResponseCodeCacheService;
import com.cpf.common.ref.mapper.CacheRefreshEventMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Offline 복귀·checkpoint 실패·중복 replay에서 Event 역전/유실이 발생하지 않는지 검증합니다. */
class CacheRefreshEventListenerTest {
    private final CacheRefreshEventMapper mapper = mock(CacheRefreshEventMapper.class);
    private final CodeCacheService code = mock(CodeCacheService.class);
    private final MessageCacheService message = mock(MessageCacheService.class);
    private final ResponseCodeCacheService response = mock(ResponseCodeCacheService.class);
    private final ConfigCacheService config = mock(ConfigCacheService.class);

    @Test
    void newInstanceCapturesHighWaterBeforeBuildingCanonicalSnapshot() {
        when(mapper.findCheckpoint("CMN_CACHE:was-1")).thenReturn(null);
        when(mapper.findMaxEventId()).thenReturn(50L);
        when(mapper.insertCheckpoint("CMN_CACHE:was-1", 50L)).thenReturn(1);
        CacheRefreshEventListener listener = listener();

        listener.initialize();

        var ordered = inOrder(mapper, code, message, response, config);
        ordered.verify(mapper).findMaxEventId();
        ordered.verify(code).refreshCodes();
        ordered.verify(message).refreshMessages();
        ordered.verify(response).refreshResponseCodes();
        ordered.verify(config).refreshConfigs();
        ordered.verify(mapper).insertCheckpoint("CMN_CACHE:was-1", 50L);
        assertThat(listener.status())
                .containsEntry("lastEventId", 50L)
                .containsEntry("initialSnapshotHighWaterBeforeRefresh", true);
    }

    @Test
    void offlineInstanceReplaysFromDurableCheckpointInOrder() {
        when(mapper.findCheckpoint("CMN_CACHE:was-1")).thenReturn(10L);
        when(mapper.findEventsAfter(10L)).thenReturn(List.of(
                Map.of("eventId", 11L, "cacheName", "codeCache"),
                Map.of("eventId", 12L, "cacheName", "configCache")));
        when(mapper.updateCheckpoint("CMN_CACHE:was-1", 11L)).thenReturn(1);
        when(mapper.updateCheckpoint("CMN_CACHE:was-1", 12L)).thenReturn(1);
        CacheRefreshEventListener listener = listener();
        listener.initialize();

        listener.pollRefreshEvents();

        verify(code).refreshCodes();
        verify(config).refreshConfigs();
        assertThat(listener.status()).containsEntry("lastEventId", 12L);
    }

    @Test
    void checkpointFailureDoesNotAdvanceCursorAndEventIsReplayed() {
        when(mapper.findCheckpoint("CMN_CACHE:was-1")).thenReturn(10L);
        when(mapper.findEventsAfter(10L)).thenReturn(List.of(
                Map.of("eventId", 11L, "cacheName", "messageCache")));
        when(mapper.updateCheckpoint("CMN_CACHE:was-1", 11L)).thenReturn(0, 1);
        CacheRefreshEventListener listener = listener();
        listener.initialize();

        listener.pollRefreshEvents();
        assertThat(listener.status()).containsEntry("lastEventId", 10L);

        listener.pollRefreshEvents();

        verify(message, times(2)).refreshMessages();
        verify(mapper, times(2)).findEventsAfter(10L);
        assertThat(listener.status()).containsEntry("lastEventId", 11L);
    }

    @Test
    void duplicateReplayNeverMovesCheckpointBackwards() {
        when(mapper.findCheckpoint("CMN_CACHE:was-1")).thenReturn(12L);
        when(mapper.findEventsAfter(12L)).thenReturn(List.of(
                Map.of("eventId", 12L, "cacheName", "codeCache"),
                Map.of("eventId", 13L, "cacheName", "codeCache")));
        when(mapper.updateCheckpoint("CMN_CACHE:was-1", 13L)).thenReturn(1);
        CacheRefreshEventListener listener = listener();
        listener.initialize();

        listener.pollRefreshEvents();

        verify(code, times(1)).refreshCodes();
        verify(mapper).updateCheckpoint("CMN_CACHE:was-1", 13L);
        assertThat(listener.status()).containsEntry("lastEventId", 13L);
    }

    private CacheRefreshEventListener listener() {
        CacheRefreshEventListener listener =
                new CacheRefreshEventListener(mapper, code, message, response, config);
        ReflectionTestUtils.setField(listener, "enabled", true);
        ReflectionTestUtils.setField(listener, "wasId", "was-1");
        return listener;
    }
}
