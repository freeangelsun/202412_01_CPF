package com.cpf.starter.cache;

import com.cpf.core.api.cache.CpfCacheKey;
import com.cpf.core.api.cache.CpfCachePort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Caffeine.class)
@EnableConfigurationProperties(CpfCaffeineCacheProperties.class)
public class CpfCacheAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    Cache<CpfCacheKey, CaffeineCpfCachePort.Entry> cpfCaffeineNativeCache(CpfCaffeineCacheProperties properties) {
        return Caffeine.newBuilder().maximumSize(properties.maximumSize()).recordStats().build();
    }
    @Bean @ConditionalOnMissingBean(CpfCachePort.class)
    CpfCachePort cpfCachePort(Cache<CpfCacheKey, CaffeineCpfCachePort.Entry> cache, CpfCaffeineCacheProperties properties) {
        return new CaffeineCpfCachePort(cache, properties.maximumPayloadBytes());
    }
}
