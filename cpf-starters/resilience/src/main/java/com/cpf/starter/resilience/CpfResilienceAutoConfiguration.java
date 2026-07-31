package com.cpf.starter.resilience;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(CircuitBreakerFactory.class)
public class CpfResilienceAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfResilienceExecutor cpfResilienceExecutor(CircuitBreakerFactory<?, ?> factory) { return new CpfResilienceExecutor(factory); }
}
