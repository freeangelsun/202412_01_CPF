package com.cpf.core.api.broker;

import java.time.Instant;

/** 공개 broker enqueue/publish 결과입니다. */
public record CpfBrokerPublishResult(String status, String messageId, String brokerName, String partitionKey, Instant processedAt, String detail) {
    public CpfBrokerPublishResult { status=status==null||status.isBlank()?"UNKNOWN":status; processedAt=processedAt==null?Instant.now():processedAt; }
}
