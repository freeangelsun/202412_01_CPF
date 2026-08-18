package com.cpf.core.api.tracking;

import java.util.Collection;

/**
 * Subject 후보를 transactionId와 연결하는 topology-independent 추적 계약입니다.
 * 업무 Controller/Service가 직접 호출하는 Golden Path가 아니라 Framework Boundary Consumer가 사용합니다.
 */
public interface CpfSubjectTrackingOperations {
    /** Subject가 없어도 Pipeline 자체는 호출될 수 있으며 빈 후보는 정상 비회원 거래를 의미합니다. */
    void collect(String transactionId, Collection<CpfSubjectCandidate> candidates);

    /** 거래 도중 신뢰 가능한 Identity가 새로 확정된 경우 같은 transactionId에 보강합니다. */
    default void enrich(String transactionId, CpfSubjectCandidate candidate) {
        collect(transactionId, java.util.List.of(candidate));
    }
}
