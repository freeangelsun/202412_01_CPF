package com.cpf.core.common.broker;

import java.util.Arrays;
import java.util.Map;

/**
 * CPF broker capability에서 사용하는 원천 메시지입니다.
 *
 * <p>Kafka, MQ, Redis Stream 같은 실제 broker adapter는 이 계약을 기준으로
 * payload와 header를 해석합니다. 업무 모듈은 특정 broker API에 직접 의존하지 않고
 * 이 표준 메시지를 통해 송수신 규격을 고정합니다.</p>
 */
public record CpfBrokerMessage(
        String messageId,
        String topic,
        String key,
        byte[] payload,
        String contentType,
        Map<String, String> headers) {

    public CpfBrokerMessage {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId는 필수입니다.");
        }
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic은 필수입니다.");
        }
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        contentType = contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    @Override
    public byte[] payload() {
        return Arrays.copyOf(payload, payload.length);
    }
}
