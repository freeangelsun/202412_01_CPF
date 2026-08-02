package com.cpf.starter.cache;

import com.cpf.common.cache.CpfCacheAsideService;
import com.cpf.common.cache.CpfLocalCacheProvider;
import com.cpf.core.api.cache.CpfCacheKey;
import com.cpf.core.api.cache.CpfCachePort;
import com.cpf.core.api.cache.CpfDistributedLockPort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Caffeine/Local L1 Cache의 단일 Runtime Owner입니다. */
@AutoConfiguration
@ConditionalOnClass(CpfCachePort.class)
@EnableConfigurationProperties(CpfCaffeineCacheProperties.class)
public class CpfCacheAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "cpf.cache", name = "provider", havingValue = "LOCAL")
    @ConditionalOnMissingBean(value = {CpfCachePort.class, CpfDistributedLockPort.class})
    CpfLocalCacheProvider cpfLocalCacheProvider() { return new CpfLocalCacheProvider(); }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.cache", name = "provider", havingValue = "CAFFEINE", matchIfMissing = true)
    @ConditionalOnMissingBean
    Cache<CpfCacheKey, CaffeineCpfCachePort.Entry> cpfCaffeineNativeCache(CpfCaffeineCacheProperties properties) {
        return Caffeine.newBuilder().maximumSize(properties.maximumSize()).recordStats().build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.cache", name = "provider", havingValue = "CAFFEINE", matchIfMissing = true)
    @ConditionalOnMissingBean(CpfCachePort.class)
    CpfCachePort cpfCachePort(Cache<CpfCacheKey, CaffeineCpfCachePort.Entry> cache,
                              CpfCaffeineCacheProperties properties) {
        return new CaffeineCpfCachePort(cache, properties.maximumPayloadBytes());
    }

    @Bean
    @ConditionalOnMissingBean(CpfCacheAsideService.class)
    CpfCacheAsideService cpfCacheAsideService(CpfCachePort cache, CpfDistributedLockPort locks) {
        return new CpfCacheAsideService(cache, locks);
    }
}
