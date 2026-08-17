package com.cpf.data.cache.valkey;

import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.data.cache.api.CpfCache;
import com.cpf.data.cache.api.CpfCacheValue;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/** Legacy string facade delegating to the canonical provider-neutral cache port. */
public final class CpfValkeyCache {
    private final CpfCache cache;
    private final CpfValkeyProperties properties;
    public CpfValkeyCache(CpfCache cache, CpfValkeyProperties properties) { this.cache=cache; this.properties=properties; }
    public void put(String key, String value, Duration ttl) {
        requireKey(key); if (value == null) throw new IllegalArgumentException("cache value is required");
        cache.put(cacheKey(key), new CpfCacheValue(true,false,value.getBytes(StandardCharsets.UTF_8),"text/plain;charset=UTF-8",0,null), ttl==null?properties.getDefaultTtl():ttl);
    }
    public Optional<String> get(String key) {
        requireKey(key); var value=cache.get(cacheKey(key));
        return value.found()?Optional.of(new String(value.payload(),StandardCharsets.UTF_8)):Optional.empty();
    }
    public boolean evict(String key) { requireKey(key); return cache.evict(cacheKey(key)); }
    private static CpfCacheKey cacheKey(String key){ return new CpfCacheKey("legacy","value","default:"+key); }
    private static void requireKey(String key){ if(key==null||key.isBlank()) throw new IllegalArgumentException("cache key is required"); }
}
