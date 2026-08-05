package com.cpf.core.api.broker;

/**
 * Provider-neutral public broker SPI. Generated Domains enqueue an event without importing
 * Kafka, RabbitMQ, JMS, IBM MQ, or the internal reliability implementation.
 */
public interface CpfBrokerClient {
    CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request);
}
