package com.cpf.core.api.broker;

import java.util.Arrays;
import java.util.Map;

/** Transactional outbox에 저장할 공개 broker 요청입니다. */
public record CpfBrokerPublishRequest(String messageId, String topic, String key, byte[] payload, String contentType,
        String transactionId, String segmentId, String producerModule, String consumerModule, String idempotencyKey,
        Map<String,String> headers, Map<String,String> attributes) {
    public CpfBrokerPublishRequest {
        if(messageId==null||messageId.isBlank()) throw new IllegalArgumentException("messageId는 필수입니다.");
        if(topic==null||topic.isBlank()) throw new IllegalArgumentException("topic은 필수입니다.");
        key = key==null||key.isBlank()?messageId:key;
        payload = payload==null?new byte[0]:Arrays.copyOf(payload,payload.length);
        contentType=contentType==null||contentType.isBlank()?"application/octet-stream":contentType;
        headers=headers==null?Map.of():Map.copyOf(headers); attributes=attributes==null?Map.of():Map.copyOf(attributes);
    }
    @Override public byte[] payload(){ return Arrays.copyOf(payload,payload.length); }
}
