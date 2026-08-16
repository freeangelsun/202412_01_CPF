package com.cpf.integration.resilience.runtime;

import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyResolver;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Durable/운영 정책을 먼저 보고 Annotation 기본 정책을 fallback으로 사용하는 합성 조회기입니다. */
public final class CpfCompositeResiliencePolicyResolver implements CpfResiliencePolicyResolver {
    private final List<CpfResiliencePolicyResolver> resolvers;
    public CpfCompositeResiliencePolicyResolver(List<CpfResiliencePolicyResolver> resolvers){this.resolvers=List.copyOf(resolvers);}
    @Override public Optional<CpfResiliencePolicy> findActive(String operationId){
        for(var r:resolvers){Optional<CpfResiliencePolicy> p=Objects.requireNonNull(r.findActive(operationId));if(p.isPresent())return p;}
        return Optional.empty();
    }
}
