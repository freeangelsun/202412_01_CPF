package com.cpf.common.runtime.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Spring CacheManager 기반 Common local cache 갱신기입니다. */
@Component
public final class SpringCpfCommonCacheRefresher implements CpfCommonCacheRefresher {
    private static final Set<String> SNAPSHOT_CACHES = Set.of("codeCache", "configCache", "messageCache", "responseCodeCache");
    private final CacheManager cacheManager;

    public SpringCpfCommonCacheRefresher(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void refresh(String cacheName) {
        if (cacheName == null || cacheName.isBlank()) throw new IllegalArgumentException("cacheName");
        if (SNAPSHOT_CACHES.contains(cacheName)) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.clear();
            return;
        }
        if ("businessCalendar".equals(cacheName) || "commonTemplate".equals(cacheName)) return;
        throw new IllegalArgumentException("Unknown CPF Common cache: " + cacheName);
    }

    @Override
    public void refreshAll() {
        SNAPSHOT_CACHES.forEach(this::refresh);
    }
}
