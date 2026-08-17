package com.cpf.data.cache.rediscommon;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.data.cache.api.CpfCacheValue;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** Provider-neutral regression for Redis/Valkey cache semantics. */
class SpringDataRedisCpfCacheOperationTest {
    @SuppressWarnings("unchecked")
    @Test
    void putGetEvictAndMetricsUseTheSharedRuntime() {
        var template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(values);
        when(template.delete(anyString())).thenReturn(true);
        var properties = new CpfRedisLikeProviderProperties();
        properties.setEnabled(true);
        var port = new SpringDataRedisCpfCache("REDIS", template, properties, true);
        var key = new CpfCacheKey("member", "42", "tenant-a");
        var source = new CpfCacheValue(true, false, "hello".getBytes(), "text/plain", 7L, null);

        port.put(key, source, Duration.ofMinutes(2));
        var encoded = ArgumentCaptor.forClass(String.class);
        verify(values).set(eq("cpf:tenant-a:member:42"), encoded.capture(), eq(Duration.ofMinutes(2)));
        when(values.get("cpf:tenant-a:member:42")).thenReturn(encoded.getValue());

        var loaded = port.get(key);
        assertTrue(loaded.found());
        assertArrayEquals("hello".getBytes(), loaded.payload());
        assertEquals(7L, loaded.version());
        assertEquals("text/plain", loaded.contentType());
        assertTrue(port.evict(key));
        verify(template).convertAndSend(eq("cpf.cache.invalidate"), eq("cpf:tenant-a:member:42"));
        var metrics = port.metrics();
        assertEquals(1L, metrics.puts());
        assertEquals(1L, metrics.hits());
        assertEquals(1L, metrics.evictions());
        assertEquals(0L, metrics.errors());
    }

    @SuppressWarnings("unchecked")
    @Test
    void providerFailureIsNotSilentlyConvertedToCacheMiss() {
        var template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenThrow(new IllegalStateException("redis unavailable"));
        var properties = new CpfRedisLikeProviderProperties();
        properties.setEnabled(true);
        var port = new SpringDataRedisCpfCache("REDIS", template, properties, true);

        assertThrows(IllegalStateException.class,
                () -> port.get(new CpfCacheKey("member", "42", "tenant-a")));
        assertEquals(1L, port.metrics().errors());
        assertEquals(0L, port.metrics().misses(), "infrastructure failure must not become a false cache miss");
    }

    @SuppressWarnings("unchecked")
    @Test
    void missAndInvalidPayloadLimitsAreFailClosed() {
        var template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn((String) null);
        var properties = new CpfRedisLikeProviderProperties();
        properties.setEnabled(true);
        properties.setMaximumPayloadBytes(4);
        var port = new SpringDataRedisCpfCache("VALKEY", template, properties, true);
        var key = new CpfCacheKey("member", "42", "tenant-a");

        assertFalse(port.get(key).found());
        assertEquals(1L, port.metrics().misses());
        assertThrows(IllegalArgumentException.class,
                () -> port.put(key, new CpfCacheValue(true, false, new byte[5], "application/octet-stream", 1L, null), Duration.ofSeconds(30)));
        verify(values, never()).set(anyString(), anyString(), any(Duration.class));
    }
}
