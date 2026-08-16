package com.cpf.messaging.api;

import java.util.List;
import java.util.Map;

/** Kafka/RabbitMQ/JMS/IBM MQ가 구현하는 Provider-neutral Messaging Port입니다. */
public interface CpfBrokerBridgePort {
    CpfBrokerBridgeResult publish(String destination, String key, Object payload, Map<String,String> additionalHeaders);
    void subscribe(String destination, CpfBrokerBridgeHandler handler);
    List<CpfBrokerBridgeMessage> findRecent(String destination, int limit);
}
