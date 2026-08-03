package com.cpf.starter.integration.resilience.internal;

import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

/** Internal Spring Cloud CircuitBreaker adapter; no OSS type escapes the package. */
final class CpfSpringCloudCircuitBreakerAdapter {
    private final CircuitBreakerFactory<?, ?> factory;
    CpfSpringCloudCircuitBreakerAdapter(CircuitBreakerFactory<?, ?> factory) { this.factory=Objects.requireNonNull(factory); }
    <T> T run(String operationId, Supplier<T> action) { return factory.create(operationId).run(action); }
}
