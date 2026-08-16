package com.cpf.messaging.api;

/** Kafka/JMS/MQ 등 실제 adapter가 concurrency/prefetch/pause를 적용하는 확장 SPI입니다. */
public interface CpfBrokerConsumerControlPort {
    void apply(CpfBrokerConsumerControl control);
}
