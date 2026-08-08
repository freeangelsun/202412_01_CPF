package com.cpf.core.common.broker;
/** Atomically moves a currently RECEIVED inbox message to the DLQ. */
public interface CpfBrokerFailureTransitionPort {
    default CpfBrokerResult moveToDlq(CpfBrokerEnvelope envelope,String reason){return moveToDlq(resolveConsumerIdentity(envelope),envelope,reason);}
    CpfBrokerResult moveToDlq(String consumerIdentity,CpfBrokerEnvelope envelope,String reason);
    private static String resolveConsumerIdentity(CpfBrokerEnvelope envelope){String v=envelope.consumerModule();return v==null||v.isBlank()?"default":v;}
}
