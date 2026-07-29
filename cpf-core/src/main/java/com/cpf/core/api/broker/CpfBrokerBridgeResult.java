package com.cpf.core.api.broker;

/** 공개 broker bridge 발행 결과입니다. */
public record CpfBrokerBridgeResult(
        boolean success,
        String broker,
        String destination,
        String key,
        String transactionId,
        String detail) {
}
