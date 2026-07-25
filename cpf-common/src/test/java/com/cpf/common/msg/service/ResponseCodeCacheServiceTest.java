package com.cpf.common.msg.service;

import com.cpf.common.msg.mapper.ResponseCodeMapper;
import com.cpf.common.ref.service.CacheRefreshEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ResponseCodeCacheServiceTest {
    @Test
    void explicitReloadPopulatesAllAndIndividualResponseCodeKeys() {
        ResponseCodeMapper mapper = mock(ResponseCodeMapper.class);
        CacheManager manager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(manager.getCache(ResponseCodeCacheService.CACHE_NAME)).thenReturn(cache);
        when(mapper.findAllResponseCodes()).thenReturn(List.of(Map.of("responseCode", "EREF010001")));

        ResponseCodeCacheService service = new ResponseCodeCacheService(
                mapper, manager, mock(CacheRefreshEventPublisher.class));
        List<Map<String, Object>> snapshot = service.reloadResponseCodes();

        assertThat(snapshot).hasSize(1);
        verify(cache).clear();
        verify(cache).put(eq("ALL"), any());
        verify(cache).put(eq("CODE:EREF010001"), any());
        assertThat(service.cacheStatus().get("version")).isEqualTo(1L);
    }

    @Test
    void failedDatabaseReadPreservesExistingCache() {
        ResponseCodeMapper mapper = mock(ResponseCodeMapper.class);
        CacheManager manager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(manager.getCache(ResponseCodeCacheService.CACHE_NAME)).thenReturn(cache);
        when(mapper.findAllResponseCodes()).thenThrow(new IllegalStateException("db down"));
        ResponseCodeCacheService service = new ResponseCodeCacheService(
                mapper, manager, mock(CacheRefreshEventPublisher.class));

        assertThatThrownBy(service::reloadResponseCodes).isInstanceOf(IllegalStateException.class);
        verify(cache, never()).clear();
    }
}
