package com.cpf.data.cache.valkey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class ValkeyCpfCacheHealthTest {
    @Test void existingValkeyProviderKeepsFailureAndReconnectBehavior() {
        var factory=mock(RedisConnectionFactory.class); var bad=mock(RedisConnection.class); var good=mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(bad,good);
        when(bad.ping()).thenThrow(new IllegalStateException("down")); when(good.ping()).thenReturn("PONG");
        var template=mock(StringRedisTemplate.class); when(template.getConnectionFactory()).thenReturn(factory);
        var properties=new CpfValkeyProperties(); properties.setEnabled(true);
        var port=new ValkeyCpfCache(template,properties,true);
        var unavailable=port.health(); assertFalse(unavailable.ready()); assertEquals("VALKEY",unavailable.provider()); assertTrue(unavailable.reasonCodes().contains("VALKEY_UNAVAILABLE"));
        var recovered=port.health(); assertTrue(recovered.ready()); assertEquals("VALKEY",recovered.provider()); assertEquals(1L,port.metrics().reconnects());
    }
}
