package com.cpf.core.api.resilience;

import java.util.List;

/** Owner command/query API for searchable, approved and auditable policy changes. */
public interface CpfResiliencePolicyOperations {
    List<CpfResiliencePolicy> search(String operationIdContains, int page, int size);
    CpfResiliencePolicy find(String operationId);
    String requestChange(CpfResiliencePolicy policy, String requesterId, String reason);
    CpfResiliencePolicy approveChange(String requestId, String approverId, String reason);
    void rejectChange(String requestId, String approverId, String reason);
}
