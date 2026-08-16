package com.cpf.integration.resilience.api;

import java.util.List;

/** Owner command/query API for searchable, approved and auditable policy changes. */
/** CpfResiliencePolicyOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfResiliencePolicyOperations {
    List<CpfResiliencePolicy> search(String operationIdContains, int page, int size);
    CpfResiliencePolicy find(String operationId);
    String requestChange(CpfResiliencePolicy policy, String requesterId, String reason);
    CpfResiliencePolicy approveChange(String requestId, String approverId, String reason);
    void rejectChange(String requestId, String approverId, String reason);
}
