package com.cpf.data.cache.redis;
import com.cpf.data.cache.rediscommon.SpringDataRedisCpfCachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
/** Redis-specific CPF cache provider; shared behavior lives in the internal Spring Data Redis foundation leaf. */
public final class RedisCpfCachePort extends SpringDataRedisCpfCachePort {
    public RedisCpfCachePort(StringRedisTemplate redis, CpfRedisCacheProperties properties, boolean durableInvalidationConfigured) {
        super("REDIS", redis, properties, durableInvalidationConfigured);
    }
}
