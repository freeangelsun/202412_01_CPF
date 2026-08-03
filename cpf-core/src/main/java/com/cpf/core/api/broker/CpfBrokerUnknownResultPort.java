package com.cpf.core.api.broker;

/** Queries and reconciles broker UNKNOWN_RESULT without provider-specific types. */
public interface CpfBrokerUnknownResultPort {
    CpfBrokerPublishResult probe(CpfBrokerPublishResultProbe probe);
    CpfBrokerPublishResult reconcile(CpfBrokerPublishResultProbe probe, String operatorId, String reason);
}
