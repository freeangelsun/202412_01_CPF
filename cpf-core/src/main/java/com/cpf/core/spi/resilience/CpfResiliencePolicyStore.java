package com.cpf.core.spi.resilience;

import com.cpf.core.api.resilience.CpfResiliencePolicy;
import java.util.List;
import java.util.Optional;

/** Persistence SPI for revisioned operation policies and two-person approval. */
public interface CpfResiliencePolicyStore {
    Optional<CpfResiliencePolicy> findActive(String operationId);
    List<CpfResiliencePolicy> search(String operationIdContains, int offset, int limit);
    String request(CpfResiliencePolicy policy, String requesterId, String reason);
    CpfResiliencePolicy approve(String requestId, String approverId, String reason);
    void reject(String requestId, String approverId, String reason);
}
