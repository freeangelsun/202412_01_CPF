package com.cpf.messaging.api;

/** Provider-neutral consumer callback. consumerGroup은 Context/구독 격리의 logical identity입니다. */
@FunctionalInterface
public interface CpfBrokerBridgeHandler {
    void handle(CpfBrokerBridgeMessage message);
    default String consumerGroup() { return ""; }
}
