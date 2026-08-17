package com.cpf.data.cache.valkey;
import com.cpf.data.cache.rediscommon.SpringDataRedisCpfCache;
import org.springframework.data.redis.core.StringRedisTemplate;
/** Valkey-specific CPF cache provider; protocol/runtime behavior is shared with Redis. */
public final class ValkeyCpfCache extends SpringDataRedisCpfCache {
    public ValkeyCpfCache(StringRedisTemplate redis, CpfValkeyProperties properties, boolean durableInvalidationConfigured) {
        super("VALKEY", redis, properties, durableInvalidationConfigured);
    }
}
