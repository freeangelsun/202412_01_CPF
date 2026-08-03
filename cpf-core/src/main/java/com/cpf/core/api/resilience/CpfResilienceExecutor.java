package com.cpf.core.api.resilience;

import java.util.function.Supplier;

/** Topology-independent execution boundary used by Gateway, HTTP and TCP consumers. */
public interface CpfResilienceExecutor {
    <T> CpfResilienceOutcome<T> execute(CpfResilienceCallContext context, Supplier<T> action);
    <T> CpfResilienceOutcome<T> reconcile(CpfResilienceCallContext context, Supplier<T> probe);
}
