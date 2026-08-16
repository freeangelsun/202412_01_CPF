package com.cpf.integration.resilience.runtime;

import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyResolver;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Annotation에서 파생된 immutable 기본 정책을 보관하는 Runtime registry입니다. 운영 Store보다 낮은 우선순위입니다. */
public final class CpfAnnotationResiliencePolicyRegistry implements CpfResiliencePolicyResolver {
    private final ConcurrentHashMap<String,CpfResiliencePolicy> policies=new ConcurrentHashMap<>();
    public void register(CpfResiliencePolicy policy){
        policies.compute(policy.operationId(),(k,old)->{
            if(old!=null && !old.equals(policy)) throw new IllegalStateException("Conflicting CPF resilience annotation policy: "+k);
            return policy;
        });
    }
    @Override public Optional<CpfResiliencePolicy> findActive(String operationId){return Optional.ofNullable(policies.get(operationId));}
}
