package com.cpf.integration.resilience.spi;

import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import java.util.List;
import java.util.Optional;

/** Persistence SPI for revisioned operation policies and two-person approval. */
/** CpfResiliencePolicyStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfResiliencePolicyStore extends CpfResiliencePolicyResolver {
    Optional<CpfResiliencePolicy> findActive(String operationId);
    List<CpfResiliencePolicy> search(String operationIdContains, int offset, int limit);
    String request(CpfResiliencePolicy policy, String requesterId, String reason);
    CpfResiliencePolicy approve(String requestId, String approverId, String reason);
    void reject(String requestId, String approverId, String reason);
}
