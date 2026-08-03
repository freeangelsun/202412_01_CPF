package com.cpf.starter.integration.resilience.internal;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.Objects;
import java.util.function.Supplier;

/** Internal Resilience4j adapter used behind the CPF contract. */
final class CpfResilience4jCircuitStateAdapter {
    private final CircuitBreakerRegistry registry;
    CpfResilience4jCircuitStateAdapter(CircuitBreakerRegistry registry) { this.registry=Objects.requireNonNull(registry); }
    <T> T execute(String operationId, Supplier<T> action) { return registry.circuitBreaker(operationId).executeSupplier(action); }
}
