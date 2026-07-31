package com.cpf.starter.cache;

import com.cpf.core.api.cache.CpfCacheKey;
import com.cpf.core.api.cache.CpfCachePort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * CPF Cache Runtime의 단일 활성화 Owner입니다.
 * LOCAL/REDIS 공통 구성과 CAFFEINE 선택 Provider를 Starter 경계에서 활성화합니다.
 */
@AutoConfiguration
@ConditionalOnClass(CpfCachePort.class)
@EnableConfigurationProperties(CpfCaffeineCacheProperties.class)
@Import(com.cpf.common.cache.CpfCacheAutoConfiguration.class)
public class CpfCacheAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "cpf.cache", name = "provider", havingValue = "CAFFEINE")
    @ConditionalOnMissingBean
    Cache<CpfCacheKey, CaffeineCpfCachePort.Entry> cpfCaffeineNativeCache(
            CpfCaffeineCacheProperties properties) {
        return Caffeine.newBuilder()
                .maximumSize(properties.maximumSize())
                .recordStats()
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.cache", name = "provider", havingValue = "CAFFEINE")
    @ConditionalOnMissingBean(CpfCachePort.class)
    CpfCachePort cpfCachePort(
            Cache<CpfCacheKey, CaffeineCpfCachePort.Entry> cache,
            CpfCaffeineCacheProperties properties) {
        return new CaffeineCpfCachePort(cache, properties.maximumPayloadBytes());
    }
}
