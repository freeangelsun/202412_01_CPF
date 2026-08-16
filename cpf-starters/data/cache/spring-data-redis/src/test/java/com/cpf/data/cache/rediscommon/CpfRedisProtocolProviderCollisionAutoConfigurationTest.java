package com.cpf.data.cache.rediscommon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfRedisProtocolProviderCollisionAutoConfigurationTest {
    @Test
    void bothProvidersOnClasspathMustFailClosed() {
        assertThrows(IllegalStateException.class, () ->
                new CpfRedisProtocolProviderCollisionAutoConfiguration.CpfRedisProtocolProviderClasspathGuard(
                        getClass().getClassLoader()));
    }

    @Test
    void redisOnlyIsAllowed() {
        ClassLoader loader = hiding("com.cpf.data.cache.valkey.CpfValkeyAutoConfiguration");
        assertDoesNotThrow(() -> new CpfRedisProtocolProviderCollisionAutoConfiguration.CpfRedisProtocolProviderClasspathGuard(loader));
    }

    @Test
    void valkeyOnlyIsAllowed() {
        ClassLoader loader = hiding("com.cpf.data.cache.redis.CpfRedisCacheAutoConfiguration");
        assertDoesNotThrow(() -> new CpfRedisProtocolProviderCollisionAutoConfiguration.CpfRedisProtocolProviderClasspathGuard(loader));
    }

    private ClassLoader hiding(String hidden) {
        return new ClassLoader(getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(hidden)) throw new ClassNotFoundException(name);
                return super.loadClass(name, resolve);
            }
        };
    }
}
