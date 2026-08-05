package com.cpf.core.api.state;

/** Public state transition boundary for long-running operations. */
public interface CpfStateOperations {
    CpfStateTransitionResult start(String stateKey, String operationId, String actor, String reason);

    CpfStateTransitionResult transition(CpfStateTransitionRequest request);

    CpfStateQueryResult query(String stateKey);

    CpfStateSearchResult search(CpfStateSearchRequest request);
}
