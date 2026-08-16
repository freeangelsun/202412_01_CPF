package com.cpf.data.cache.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisCpfCachePortHealthTest {
    @Test void connectionFailureAndReconnectAreObservable() {
        var factory=mock(RedisConnectionFactory.class); var bad=mock(RedisConnection.class); var good=mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(bad,good);
        when(bad.ping()).thenThrow(new IllegalStateException("down")); when(good.ping()).thenReturn("PONG");
        var template=mock(StringRedisTemplate.class); when(template.getConnectionFactory()).thenReturn(factory);
        var properties=new CpfRedisCacheProperties(); properties.setEnabled(true);
        var port=new RedisCpfCachePort(template,properties,true);
        var unavailable=port.health(); assertFalse(unavailable.ready()); assertEquals("REDIS",unavailable.provider()); assertTrue(unavailable.reasonCodes().contains("REDIS_UNAVAILABLE"));
        var recovered=port.health(); assertTrue(recovered.ready()); assertEquals("REDIS",recovered.provider()); assertEquals(1L,port.metrics().reconnects());
    }
}
