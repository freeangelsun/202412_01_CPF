package com.cpf.data.cache.rediscommon;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** Redis와 Valkey 공개 Provider가 동시에 classpath에 들어오면 설정값과 무관하게 fail-closed 합니다. */
@AutoConfiguration
public class CpfRedisProtocolProviderCollisionAutoConfiguration {
    @Bean
    CpfRedisProtocolProviderClasspathGuard cpfRedisProtocolProviderClasspathGuard() {
        return new CpfRedisProtocolProviderClasspathGuard(Thread.currentThread().getContextClassLoader());
    }

    static final class CpfRedisProtocolProviderClasspathGuard {
        CpfRedisProtocolProviderClasspathGuard(ClassLoader loader) {
            ClassLoader effective = loader == null ? getClass().getClassLoader() : loader;
            boolean redis = present("com.cpf.data.cache.redis.CpfRedisCacheAutoConfiguration", effective);
            boolean valkey = present("com.cpf.data.cache.valkey.CpfValkeyAutoConfiguration", effective);
            if (redis && valkey) {
                throw new IllegalStateException(
                        "CPF cache provider collision: cpf-starter-cache-redis and cpf-starter-cache-valkey cannot coexist");
            }
        }

        private boolean present(String name, ClassLoader loader) {
            try {
                Class.forName(name, false, loader);
                return true;
            } catch (ClassNotFoundException ex) {
                return false;
            }
        }
    }
}
