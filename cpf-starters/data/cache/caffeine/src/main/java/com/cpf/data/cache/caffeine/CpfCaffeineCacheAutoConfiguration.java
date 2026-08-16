package com.cpf.data.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

/** Developer-facing Caffeine cache provider. */
@AutoConfiguration
@ConditionalOnProperty(prefix = "cpf.data.cache.caffeine", name = "enabled", havingValue = "true")
public class CpfCaffeineCacheAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    CacheManager cpfCaffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(10)));
        return manager;
    }
}
