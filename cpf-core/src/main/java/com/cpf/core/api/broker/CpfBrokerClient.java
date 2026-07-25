package com.cpf.core.api.broker;

/** Generated Domain이 특정 Kafka/MQ/Outbox 구현을 모르고 이벤트를 등록하는 공개 계약입니다. */
public interface CpfBrokerClient {
    CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request);
}
