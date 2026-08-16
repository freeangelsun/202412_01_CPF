package com.cpf.data.cache.caffeine;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.cache.caffeine")
/** CpfCaffeineCacheProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCaffeineCacheProperties(long maximumSize, long maximumPayloadBytes) {
    public CpfCaffeineCacheProperties {
        maximumSize = maximumSize <= 0 ? 10_000 : maximumSize;
        maximumPayloadBytes = maximumPayloadBytes <= 0 ? 1_048_576 : maximumPayloadBytes;
        if (maximumSize > 10_000_000) throw new IllegalArgumentException("Caffeine maximumSize is too large.");
        if (maximumPayloadBytes > 16_777_216) throw new IllegalArgumentException("Cache payload limit is too large.");
    }
}
