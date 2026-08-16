package com.cpf.data.cache.rediscommon;

/** Fail-closed guard for mutually exclusive Redis-protocol cache providers. */
public final class CpfRedisProtocolProviderSelection {
    private CpfRedisProtocolProviderSelection() { }
    public static void requireExclusive(boolean redisEnabled, boolean valkeyEnabled) {
        if (redisEnabled && valkeyEnabled) {
            throw new IllegalStateException("Redis and Valkey cache providers cannot be enabled at the same time");
        }
    }
}
