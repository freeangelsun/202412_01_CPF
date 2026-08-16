package com.cpf.integration.realtime;

import java.time.Instant;
import java.util.Objects;

/**
 * 브라우저로 전달되는 실시간 이벤트의 provider-neutral envelope입니다.
 * sequence는 replay cursor이며 eventId는 producer가 재전송해도 바뀌지 않는 stable id입니다.
 */
public record CpfRealtimeEvent(
        long sequence,
        String eventId,
        String channel,
        String topic,
        String tenantId,
        String subjectId,
        String transactionId,
        String payload,
        Instant occurredAt) {
    public CpfRealtimeEvent {
        if (sequence < 0) throw new IllegalArgumentException("sequence");
        eventId = require(eventId, "eventId", 160);
        channel = require(channel, "channel", 80);
        topic = require(topic, "topic", 160);
        tenantId = require(tenantId, "tenantId", 120);
        subjectId = require(subjectId, "subjectId", 160);
        transactionId = transactionId == null ? "" : require(transactionId, "transactionId", 80);
        payload = Objects.requireNonNull(payload, "payload");
        if (payload.length() > 262_144) throw new IllegalArgumentException("payload too large");
        occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public CpfRealtimeEvent withSequence(long nextSequence) {
        return new CpfRealtimeEvent(nextSequence, eventId, channel, topic, tenantId, subjectId,
                transactionId, payload, occurredAt);
    }

    private static String require(String value, String name, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name);
        String normalized = value.trim();
        if (normalized.length() > max) throw new IllegalArgumentException(name + " too long");
        return normalized;
    }
}
