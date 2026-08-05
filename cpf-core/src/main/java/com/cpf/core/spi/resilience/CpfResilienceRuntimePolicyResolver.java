package com.cpf.core.spi.resilience;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.api.resilience.CpfResilienceRuntimePolicy;

/** Resolves runtime timeout, retry-budget and queue limits without changing the persisted policy schema. */
@FunctionalInterface
public interface CpfResilienceRuntimePolicyResolver {
    CpfResilienceRuntimePolicy resolve(CpfResiliencePolicy policy, CpfResilienceCallContext context);

    static CpfResilienceRuntimePolicyResolver legacyCompatible() {
        return (policy, context) -> CpfResilienceRuntimePolicy.legacyCompatible(policy);
    }
}
