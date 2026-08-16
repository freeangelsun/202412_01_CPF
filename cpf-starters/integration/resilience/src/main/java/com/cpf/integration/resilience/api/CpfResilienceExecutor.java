package com.cpf.integration.resilience.api;

import java.util.function.Supplier;

/** Topology-independent execution boundary used by Gateway, HTTP and TCP consumers. */
/** CpfResilienceExecutor 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfResilienceExecutor {
    <T> CpfResilienceOutcome<T> execute(CpfResilienceCallContext context, Supplier<T> action);
    <T> CpfResilienceOutcome<T> reconcile(CpfResilienceCallContext context, Supplier<T> probe);
}
