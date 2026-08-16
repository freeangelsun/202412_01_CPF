package com.cpf.messaging.api;

/** Queries and reconciles broker UNKNOWN_RESULT without provider-specific types. */
/** CpfBrokerUnknownResultPort 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfBrokerUnknownResultPort {
    CpfBrokerPublishResult probe(CpfBrokerPublishResultProbe probe);
    CpfBrokerPublishResult reconcile(CpfBrokerPublishResultProbe probe, String operatorId, String reason);
}
