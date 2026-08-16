package com.cpf.data.cache.rediscommon;

import com.cpf.data.cache.api.CpfCacheHealth;
import com.cpf.data.cache.api.CpfCachePort;
import java.util.Objects;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * Required Redis-protocol provider startup gate.
 *
 * <p>Optional providers may start while unavailable and expose DOWN health. Required providers
 * fail application startup when the selected backend is unreachable or when durable invalidation
 * is not ready. Runtime disconnects remain observable through {@link CpfCachePort#health()} and
 * recover through the durable invalidation reconciler.</p>
 */
public final class CpfRedisLikeStartupValidator implements SmartInitializingSingleton {
    private final String providerName;
    private final CpfCachePort cache;
    private final CpfRedisLikeProviderProperties properties;

    public CpfRedisLikeStartupValidator(
            String providerName,
            CpfCachePort cache,
            CpfRedisLikeProviderProperties properties) {
        this.providerName = Objects.requireNonNull(providerName, "providerName");
        this.cache = Objects.requireNonNull(cache, "cache");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!properties.isEnabled() || !properties.isRequired()) {
            return;
        }
        CpfCacheHealth health = cache.health();
        if (!health.ready()) {
            throw new IllegalStateException(
                    "CPF required cache provider unavailable at startup: provider=" + providerName
                            + ", reasonCodes=" + health.reasonCodes());
        }
    }
}
