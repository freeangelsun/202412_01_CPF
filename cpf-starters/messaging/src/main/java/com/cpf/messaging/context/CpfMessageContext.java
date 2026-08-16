package com.cpf.messaging.context;

import java.time.Instant;

/**
 * Messaging Owner가 소유하는 메시지 전송 메타데이터입니다.
 * Core Context에 generic component로 삽입하지 않고 message bundle에서만 함께 전달합니다.
 */
public record CpfMessageContext(
        String transport,
        String messageId,
        String eventId,
        String eventType,
        String destination,
        String producer,
        String consumerGroup,
        String partition,
        String offset,
        String orderingKey,
        int deliveryAttempt,
        boolean redelivery,
        String schemaId,
        String schemaVersion,
        Instant producedAt,
        Instant consumedAt,
        String replyTo,
        String deadLetterReason) {
}
