package com.cpf.integration.resilience.spi;

import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.api.CpfResilienceRuntimePolicy;

/** Resolves runtime timeout, retry-budget and queue limits without changing the persisted policy schema. */
@FunctionalInterface
/** CpfResilienceRuntimePolicyResolver 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfResilienceRuntimePolicyResolver {
    CpfResilienceRuntimePolicy resolve(CpfResiliencePolicy policy, CpfResilienceCallContext context);

    static CpfResilienceRuntimePolicyResolver legacyCompatible() {
        return (policy, context) -> CpfResilienceRuntimePolicy.legacyCompatible(policy);
    }
}
