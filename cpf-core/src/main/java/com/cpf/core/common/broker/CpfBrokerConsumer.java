package com.cpf.core.common.broker;

/**
 * CPF broker 수신 port입니다.
 */
public interface CpfBrokerConsumer {

    CpfBrokerResult consume(CpfBrokerEnvelope envelope);
}
