package com.cpf.starter.resilience;

import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

/** Retry/CircuitBreaker를 자체 상태기계로 재구현하지 않는 얇은 CPF 정책 경계입니다. */
public final class CpfResilienceExecutor {
    private final CircuitBreakerFactory<?, ?> factory;
    public CpfResilienceExecutor(CircuitBreakerFactory<?, ?> factory) { this.factory = factory; }
    public <T> T run(String policyId, Supplier<T> action, Function<Throwable, T> fallback) {
        if (policyId == null || !policyId.matches("[A-Za-z0-9._-]{1,128}")) throw new IllegalArgumentException("Invalid resilience policyId.");
        return factory.create(policyId).run(action, fallback);
    }
}
