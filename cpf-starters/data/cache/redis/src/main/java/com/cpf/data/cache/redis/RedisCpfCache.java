package com.cpf.data.cache.redis;
import com.cpf.data.cache.rediscommon.SpringDataRedisCpfCache;
import org.springframework.data.redis.core.StringRedisTemplate;
/** Redis-specific CPF cache provider; shared behavior lives in the internal Spring Data Redis foundation leaf. */
public final class RedisCpfCache extends SpringDataRedisCpfCache {
    public RedisCpfCache(StringRedisTemplate redis, CpfRedisCacheProperties properties, boolean durableInvalidationConfigured) {
        super("REDIS", redis, properties, durableInvalidationConfigured);
    }
}
