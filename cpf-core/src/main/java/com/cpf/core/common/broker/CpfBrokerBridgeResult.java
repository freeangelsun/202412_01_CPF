package com.cpf.core.common.broker;

/**
 * broker bridge 발행 결과입니다.
 */
public record CpfBrokerBridgeResult(
        boolean success,
        String broker,
        String destination,
        String key,
        String transactionId,
        String detail) {
}
