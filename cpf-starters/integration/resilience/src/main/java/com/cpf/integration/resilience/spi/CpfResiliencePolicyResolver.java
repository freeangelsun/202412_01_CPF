package com.cpf.integration.resilience.spi;

import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import java.util.Optional;

/** 실행 시 사용할 활성 resilience policy를 조회하는 최소 Runtime 계약입니다. */
@FunctionalInterface
public interface CpfResiliencePolicyResolver {
    Optional<CpfResiliencePolicy> findActive(String operationId);
}
