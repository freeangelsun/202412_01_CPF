package com.cpf.messaging.reliability.api.jdbc.internal;

import com.cpf.messaging.spi.broker.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfBrokerOutboxIdentityTest {
    @Test
    void acceptsSameBusinessRequestWhenRetryTimestampChanges() {
        assertThat(CpfBrokerOutboxIdentity.same(
                envelope(Instant.parse("2026-08-05T10:00:00Z"), "payload", "idem-1"),
                envelope(Instant.parse("2026-08-05T10:00:30Z"), "payload", "idem-1")))
                .isTrue();
    }

    @Test
    void rejectsPayloadOrIdempotencyChangesForSameMessageId() {
        CpfBrokerEnvelope original = envelope(
                Instant.parse("2026-08-05T10:00:00Z"), "payload", "idem-1");
        assertThat(CpfBrokerOutboxIdentity.same(original,
                envelope(Instant.parse("2026-08-05T10:00:01Z"), "changed", "idem-1")))
                .isFalse();
        assertThat(CpfBrokerOutboxIdentity.same(original,
                envelope(Instant.parse("2026-08-05T10:00:01Z"), "payload", "idem-2")))
                .isFalse();
    }

    private static CpfBrokerEnvelope envelope(Instant occurredAt, String payload, String idempotencyKey) {
        return new CpfBrokerEnvelope(
                "tx-1", "segment-1", "producer", "consumer", idempotencyKey, occurredAt,
                new CpfBrokerMessage(
                        "message-1", "topic-1", "key-1",
                        payload.getBytes(StandardCharsets.UTF_8), "text/plain",
                        Map.of("traceparent", "00-abc-def-01")),
                Map.of("tenant", "T1"));
    }
}
