package com.cpf.messaging.reliability.api.jdbc.internal;

import com.cpf.messaging.reliability.api.jdbc.CpfBrokerPublisherWorker;
import com.cpf.messaging.spi.broker.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfBrokerPublisherWorkerMaskingTest {
    @Test
    void exceptionCredentialsAreMaskedBeforeUnknownIsPersisted() {
        Instant now = Instant.parse("2026-08-05T01:00:00Z");
        RecordingOutbox outbox = new RecordingOutbox(envelope(now));
        CpfBrokerPublisher publisher = envelope -> {
            throw new IllegalStateException(
                    "authorization=Bearer abc.def password=hunter2 token=token-value");
        };
        CpfBrokerPublisherWorker worker = new CpfBrokerPublisherWorker(
                outbox, outbox, publisher, Clock.fixed(now, ZoneOffset.UTC),
                Duration.ofSeconds(30));

        var result = worker.runOnce("worker-1", 10);

        assertThat(result.unknownCount()).isEqualTo(1);
        assertThat(outbox.unknown.detail())
                .doesNotContain("abc.def", "hunter2", "token-value")
                .contains("***");
        assertThat(outbox.nextReconcileAt).isEqualTo(now.plusSeconds(30));
    }

    private static CpfBrokerEnvelope envelope(Instant occurredAt) {
        return new CpfBrokerEnvelope(
                "tx", "seg", "producer", "consumer", "idem", occurredAt,
                new CpfBrokerMessage("msg", "topic", "key", new byte[] {1},
                        "application/octet-stream", Map.of()),
                Map.of());
    }

    private static final class RecordingOutbox
            implements CpfBrokerOutboxPort, CpfBrokerUnknownResultPort {
        private final CpfBrokerEnvelope envelope;
        private CpfBrokerResult unknown;
        private Instant nextReconcileAt;

        private RecordingOutbox(CpfBrokerEnvelope envelope) {
            this.envelope = envelope;
        }

        @Override
        public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
            return CpfBrokerResult.accepted("msg", "CPF_OUTBOX", "key");
        }

        @Override
        public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
            return List.of(envelope);
        }

        @Override
        public void markPublished(String messageId, CpfBrokerResult result) {
            throw new AssertionError("legacy unfenced completion must not be called");
        }

        @Override
        public boolean supportsFencedPublishMutation() {
            return true;
        }

        @Override
        public void markPublished(String workerId, String messageId, CpfBrokerResult result) {
            assertThat(workerId).isEqualTo("worker-1");
        }

        @Override
        public void markUnknown(String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
            throw new AssertionError("legacy unfenced UNKNOWN mutation must not be called");
        }

        @Override
        public boolean supportsFencedUnknownMutation() {
            return true;
        }

        @Override
        public void markUnknown(
                String workerId, String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
            assertThat(workerId).isEqualTo("worker-1");
            this.unknown = result;
            this.nextReconcileAt = nextReconcileAt;
        }

        @Override
        public List<CpfBrokerEnvelope> claimUnknown(String workerId, int limit) {
            return List.of();
        }

        @Override
        public void releaseUnknown(String messageId, String detail, Instant nextReconcileAt) {
        }
    }
}
