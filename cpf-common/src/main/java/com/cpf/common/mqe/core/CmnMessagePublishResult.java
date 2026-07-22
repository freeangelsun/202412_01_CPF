package com.cpf.common.mqe.core;

/** 메시지 발행 성공 여부와 거래 추적 식별자를 포함한 표준 결과입니다. */
public record CmnMessagePublishResult(
        boolean success,
        String broker,
        String destination,
        String key,
        String transactionId,
        String message) {
}

