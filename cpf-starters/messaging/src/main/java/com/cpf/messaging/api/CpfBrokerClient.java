package com.cpf.messaging.api;

/**
 * Provider-neutral low-level broker publish contract.
 * Golden Path is CpfBrokerBridgePort; this contract preserves binary/native-adapter parity
 * without leaking Kafka/Rabbit/JMS/IBM MQ APIs to business code.
 */
@FunctionalInterface
/** CpfBrokerClient 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfBrokerClient {
    CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request);
}
