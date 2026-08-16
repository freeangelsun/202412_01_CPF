package com.cpf.messaging.spi.broker;

/** 현재 RECEIVED Inbox Message를 DLQ로 원자 전이하는 SPI입니다. */
public interface CpfBrokerFailureTransitionPort {
    CpfBrokerResult moveToDlq(CpfBrokerEnvelope envelope,String reason);
    default CpfBrokerResult moveToDlq(String consumerIdentity,CpfBrokerEnvelope envelope,String reason) {
        return moveToDlq(envelope,reason);
    }
}
