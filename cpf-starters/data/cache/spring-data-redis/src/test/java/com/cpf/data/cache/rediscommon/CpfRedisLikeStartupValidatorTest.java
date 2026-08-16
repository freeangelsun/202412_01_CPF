package com.cpf.data.cache.rediscommon;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.data.cache.api.CpfCacheHealth;
import com.cpf.data.cache.api.CpfCachePort;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CpfRedisLikeStartupValidatorTest {
    @Test
    void requiredProviderFailsStartupWhenUnavailable() {
        CpfCachePort cache = mock(CpfCachePort.class);
        when(cache.health()).thenReturn(new CpfCacheHealth(
                false, "REDIS", "DISTRIBUTED_L2", false, true, 0L,
                List.of("REDIS_UNAVAILABLE"), Instant.now()));
        CpfRedisLikeProviderProperties properties = new CpfRedisLikeProviderProperties();
        properties.setEnabled(true);
        properties.setRequired(true);
        var validator = new CpfRedisLikeStartupValidator("REDIS", cache, properties);
        assertThrows(IllegalStateException.class, validator::afterSingletonsInstantiated);
    }

    @Test
    void optionalProviderMayStartUnavailable() {
        CpfCachePort cache = mock(CpfCachePort.class);
        CpfRedisLikeProviderProperties properties = new CpfRedisLikeProviderProperties();
        properties.setEnabled(true);
        properties.setRequired(false);
        var validator = new CpfRedisLikeStartupValidator("REDIS", cache, properties);
        assertDoesNotThrow(validator::afterSingletonsInstantiated);
    }
}
