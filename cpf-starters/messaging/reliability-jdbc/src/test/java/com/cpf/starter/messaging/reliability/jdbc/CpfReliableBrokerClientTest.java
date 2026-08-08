package com.cpf.starter.messaging.reliability.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class CpfReliableBrokerClientTest {
    private static final Instant NOW = Instant.parse("2026-08-05T01:00:00Z");

    @Test
    void enqueuePersistsCompleteEnvelopeAndReturnsAccepted() throws Exception {
        RecordingOutbox outbox = new RecordingOutbox();
        CpfReliableBrokerClient client = new CpfReliableBrokerClient(
                outbox, Clock.fixed(NOW, ZoneOffset.UTC));
        CpfBrokerPublishRequest request = request();

        var result = client.enqueue(request);

        assertThat(result.status()).isEqualTo("ACCEPTED");
        assertThat(result.messageId()).isEqualTo("msg-1");
        assertThat(result.brokerName()).isEqualTo("CPF_OUTBOX");
        assertThat(outbox.saved.transactionId()).isEqualTo("tx-1");
        assertThat(outbox.saved.segmentId()).isEqualTo("seg-1");
        assertThat(outbox.saved.idempotencyKey()).isEqualTo("idem-1");
        assertThat(outbox.saved.occurredAt()).isEqualTo(NOW);
        assertThat(outbox.saved.message().payload())
                .containsExactly("payload".getBytes(StandardCharsets.UTF_8));
        assertThat(outbox.saved.message().headers()).containsEntry("trace", "abc");
        assertThat(outbox.saved.attributes()).containsEntry("tenant", "T1");

        Method method = CpfReliableBrokerClient.class.getMethod(
                "enqueue", CpfBrokerPublishRequest.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void rejectsReservedOrCollidingHeadersBeforeOutboxWrite() {
        RecordingOutbox outbox = new RecordingOutbox();
        CpfReliableBrokerClient client = new CpfReliableBrokerClient(
                outbox, Clock.fixed(NOW, ZoneOffset.UTC));
        CpfBrokerPublishRequest reserved = new CpfBrokerPublishRequest(
                "msg-1", "topic-1", "key-1", new byte[0], "application/octet-stream",
                "tx-1", "seg-1", "producer", "consumer", "idem-1",
                Map.of("CPF.Message-Id", "override"), Map.of());

        assertThatThrownBy(() -> client.enqueue(reserved))
                .isInstanceOf(SecurityException.class);
        assertThat(outbox.saved).isNull();

        Map<String, String> colliding = new java.util.LinkedHashMap<>();
        colliding.put("trace-parent", "a");
        colliding.put("trace.parent", "b");
        CpfBrokerPublishRequest duplicate = new CpfBrokerPublishRequest(
                "msg-2", "topic-1", "key-1", new byte[0], "application/octet-stream",
                "tx-2", "seg-1", "producer", "consumer", "idem-2",
                colliding, Map.of());

        assertThatThrownBy(() -> client.enqueue(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("normalize to the same provider name");
        assertThat(outbox.saved).isNull();
    }

    private static CpfBrokerPublishRequest request() {
        return new CpfBrokerPublishRequest(
                "msg-1", "topic-1", "key-1",
                "payload".getBytes(StandardCharsets.UTF_8), "text/plain",
                "tx-1", "seg-1", "producer", "consumer", "idem-1",
                Map.of("trace", "abc"), Map.of("tenant", "T1"));
    }

    private static final class RecordingOutbox implements CpfBrokerOutboxPort {
        private CpfBrokerEnvelope saved;

        @Override
        public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
            saved = envelope;
            return CpfBrokerResult.accepted(
                    envelope.message().messageId(), "CPF_OUTBOX", envelope.message().key());
        }

        @Override
        public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
            return List.of();
        }

        @Override
        public void markPublished(String messageId, CpfBrokerResult result) {
        }
    }
}
