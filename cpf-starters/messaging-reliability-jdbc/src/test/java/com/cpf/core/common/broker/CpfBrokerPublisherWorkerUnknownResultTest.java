package com.cpf.core.common.broker;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfBrokerPublisherWorkerUnknownResultTest {
    private static final Instant NOW = Instant.parse("2026-08-02T00:00:00Z");

    @Test
    void nullResultUsesUnknownPathWithoutFailureRetry() {
        var port = new Port();
        var worker = new CpfBrokerPublisherWorker(
                port, port, envelope -> null,
                Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofSeconds(10));

        var result = worker.runOnce("w", 1);

        assertThat(result.unknownCount()).isEqualTo(1);
        assertThat(port.unknown).isTrue();
        assertThat(port.published).isFalse();
    }

    @Test
    void definiteFailureUsesDurableRetryStateMachine() {
        var port = new Port();
        var worker = new CpfBrokerPublisherWorker(
                port, port,
                envelope -> CpfBrokerResult.failed("m1", "BROKER", "rejected"),
                Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofSeconds(10));

        var result = worker.runOnce("w", 1);

        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(port.published).isTrue();
        assertThat(port.unknown).isFalse();
    }

    private static final class Port
            implements CpfBrokerOutboxPort, CpfBrokerUnknownResultPort {
        private boolean unknown;
        private boolean published;

        @Override
        public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
            return CpfBrokerResult.accepted(
                    envelope.message().messageId(), "OUTBOX", null);
        }

        @Override
        public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
            return List.of(envelope());
        }

        @Override
        public void markPublished(String messageId, CpfBrokerResult result) {
            published = true;
        }

        @Override
        public void markUnknown(
                String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
            unknown = true;
        }

        @Override
        public List<CpfBrokerEnvelope> claimUnknown(String workerId, int limit) {
            return List.of();
        }

        @Override
        public void releaseUnknown(
                String messageId, String detail, Instant nextReconcileAt) {
        }

        private static CpfBrokerEnvelope envelope() {
            return new CpfBrokerEnvelope(
                    "tx", "seg", "PRD", "CON", "idem", NOW,
                    new CpfBrokerMessage(
                            "m1", "topic", "k", new byte[0],
                            "application/json", Map.of()),
                    Map.of());
        }
    }
}
