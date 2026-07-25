package com.cpf.common.cde.service;

import com.cpf.common.cde.mapper.CodeMapper;
import com.cpf.common.ref.service.CacheRefreshEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CodeCacheServiceTest {
    @Test
    void explicitReloadPopulatesAllAndIndividualKeys() {
        CodeMapper mapper = mock(CodeMapper.class);
        CacheManager manager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        CacheRefreshEventPublisher publisher = mock(CacheRefreshEventPublisher.class);
        when(manager.getCache("codeCache")).thenReturn(cache);
        when(mapper.findAllCodes()).thenReturn(List.of(Map.of("codeKey", "OK", "codeValue", "0000")));

        CodeCacheService service = new CodeCacheService(mapper, manager, publisher);
        List<Map<String, Object>> snapshot = service.reloadCodes();

        assertThat(snapshot).hasSize(1);
        verify(cache).clear();
        verify(cache).put(eq("ALL"), any());
        verify(cache).put(eq("CODE:OK"), any());
        assertThat(service.cacheStatus().get("version")).isEqualTo(1L);
    }

    @Test
    void failedDatabaseReadDoesNotClearExistingCache() {
        CodeMapper mapper = mock(CodeMapper.class);
        CacheManager manager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(manager.getCache("codeCache")).thenReturn(cache);
        when(mapper.findAllCodes()).thenThrow(new IllegalStateException("db down"));
        CodeCacheService service = new CodeCacheService(mapper, manager, mock(CacheRefreshEventPublisher.class));

        org.assertj.core.api.Assertions.assertThatThrownBy(service::reloadCodes)
                .isInstanceOf(IllegalStateException.class);
        verify(cache, never()).clear();
    }
}
