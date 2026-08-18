package com.cpf.messaging.reliability.api.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import com.cpf.messaging.spi.broker.CpfBrokerEnvelope;
import com.cpf.messaging.spi.broker.CpfBrokerOutboxPort;
import com.cpf.messaging.spi.broker.CpfBrokerResult;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.transaction.annotation.Transactional;

class CpfReliableBrokerClientTest {
    private AutoCloseable contextScope;
    private CpfMessageBridgeContextSupport contextSupport;
    @BeforeEach void bindContext() {
        Clock clock=Clock.fixed(NOW,ZoneOffset.UTC);
        CpfExecutionIdGenerator ids=new CpfExecutionIdGenerator(){private int n;public String newExecutionId(){return "EX-"+(++n);}public String newSegmentId(){return "seg-1";}};
        CpfContextExecutionFactory factory=new CpfContextExecutionFactory(() -> "tx-1",ids,() -> LocalDate.of(2026,8,5),clock);
        contextScope=CpfContexts.bind(CpfContextSnapshot.capture(factory.newRoot(null,"messaging.test",null,null,NOW.plusSeconds(60)),NOW));
        contextSupport=new CpfMessageBridgeContextSupport(ids,clock);
    }
    @AfterEach void clearContext() throws Exception { if(contextScope!=null) contextScope.close(); }

    private static final Instant NOW = Instant.parse("2026-08-05T01:00:00Z");

    @Test
    void enqueuePersistsCompleteEnvelopeAndReturnsAccepted() throws Exception {
        RecordingOutbox outbox = new RecordingOutbox();
        CpfReliableBrokerClient client = new CpfReliableBrokerClient(
                outbox, Clock.fixed(NOW, ZoneOffset.UTC), contextSupport);
        CpfBrokerPublishRequest request = request();

        var result = client.send(request);

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
                "send", CpfBrokerPublishRequest.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void rejectsReservedOrCollidingHeadersBeforeOutboxWrite() {
        RecordingOutbox outbox = new RecordingOutbox();
        CpfReliableBrokerClient client = new CpfReliableBrokerClient(
                outbox, Clock.fixed(NOW, ZoneOffset.UTC), contextSupport);
        CpfBrokerPublishRequest reserved = new CpfBrokerPublishRequest(
                "msg-1", "topic-1", "key-1", new byte[0], "application/octet-stream", "producer", "consumer", "idem-1",
                Map.of("CPF.Message-Id", "override"), Map.of());

        assertThatThrownBy(() -> client.send(reserved))
                .isInstanceOf(SecurityException.class);
        assertThat(outbox.saved).isNull();

        Map<String, String> colliding = new java.util.LinkedHashMap<>();
        colliding.put("trace-parent", "a");
        colliding.put("trace.parent", "b");
        CpfBrokerPublishRequest duplicate = new CpfBrokerPublishRequest(
                "msg-2", "topic-1", "key-1", new byte[0], "application/octet-stream", "producer", "consumer", "idem-2",
                colliding, Map.of());

        assertThatThrownBy(() -> client.send(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("normalize to the same provider name");
        assertThat(outbox.saved).isNull();
    }

    private static CpfBrokerPublishRequest request() {
        return new CpfBrokerPublishRequest(
                "msg-1", "topic-1", "key-1",
                "payload".getBytes(StandardCharsets.UTF_8), "text/plain", "producer", "consumer", "idem-1",
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
