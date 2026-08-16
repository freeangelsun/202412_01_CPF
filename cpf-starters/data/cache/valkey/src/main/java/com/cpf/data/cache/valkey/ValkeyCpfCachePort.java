package com.cpf.data.cache.valkey;
import com.cpf.data.cache.rediscommon.SpringDataRedisCpfCachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
/** Valkey-specific CPF cache provider; protocol/runtime behavior is shared with Redis. */
public final class ValkeyCpfCachePort extends SpringDataRedisCpfCachePort {
    public ValkeyCpfCachePort(StringRedisTemplate redis, CpfValkeyProperties properties, boolean durableInvalidationConfigured) {
        super("VALKEY", redis, properties, durableInvalidationConfigured);
    }
}
