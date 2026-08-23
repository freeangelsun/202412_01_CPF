package com.cpf.reliability.runtime;

import com.cpf.reliability.api.CpfIdempotencyFingerprintResolver;
import com.cpf.reliability.api.CpfIdempotencyResultCodec;
import com.cpf.reliability.api.CpfIdempotencyStore;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Durable Store 유무와 무관하게 Annotation Consumer를 설치하고 호출 시 fail-closed를 보장합니다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfIdempotencyProperties.class)
public class CpfIdempotencyAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfIdempotencyFingerprintResolver cpfIdempotencyFingerprintResolver() { return new CpfDefaultIdempotencyFingerprintResolver(); }

    @Bean @ConditionalOnMissingBean
    CpfIdempotencyResultCodec cpfIdempotencyResultCodec() { return new CpfScalarIdempotencyResultCodec(); }

    @Bean @ConditionalOnMissingBean
    CpfIdempotencyCoordinator cpfIdempotencyCoordinator(CpfIdempotencyProperties properties,
            ObjectProvider<CpfIdempotencyStore> store, CpfIdempotencyFingerprintResolver fingerprintResolver,
            CpfIdempotencyResultCodec resultCodec, ObjectProvider<Clock> clocks) {
        Clock clock = clocks.getIfUnique(Clock::systemUTC);
        return new CpfIdempotencyCoordinator(properties, store.getIfAvailable(), fingerprintResolver, resultCodec, clock);
    }

    @Bean @ConditionalOnMissingBean
    CpfIdempotencyAspect cpfIdempotencyAspect(CpfIdempotencyCoordinator coordinator) { return new CpfIdempotencyAspect(coordinator); }
}
