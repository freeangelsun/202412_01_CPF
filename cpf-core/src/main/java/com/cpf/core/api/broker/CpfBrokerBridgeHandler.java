package com.cpf.core.api.broker;

/** Broker bridge가 수신한 메시지를 처리하는 공개 callback 계약입니다. */
@FunctionalInterface
public interface CpfBrokerBridgeHandler {
    void handle(CpfBrokerBridgeMessage message);
}
