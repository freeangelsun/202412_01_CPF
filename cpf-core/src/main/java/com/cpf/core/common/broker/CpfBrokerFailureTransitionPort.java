package com.cpf.core.common.broker;

/** Atomically moves a currently RECEIVED inbox message to the DLQ. */
public interface CpfBrokerFailureTransitionPort {
    CpfBrokerResult moveToDlq(CpfBrokerEnvelope envelope, String reason);
}
