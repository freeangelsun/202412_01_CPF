package com.cpf.platform.operations.api.state;

/** Public state transition boundary for long-running operations. */
/** CpfStateOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfStateOperations {
    CpfStateTransitionResult start(String stateKey, String operationId, String actor, String reason);

    CpfStateTransitionResult transition(CpfStateTransitionRequest request);

    CpfStateQueryResult query(String stateKey);

    CpfStateSearchResult search(CpfStateSearchRequest request);
}
