package com.cpf.starter.messaging.reliability;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerMessage;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import com.cpf.core.common.broker.CpfBrokerUnknownResultPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfBrokerUnknownResultReconcilerTest {
    private static final Instant NOW = Instant.parse("2026-08-02T00:00:00Z");

    @Test
    void definiteFailureLeavesUnknownAndUsesRetryStateMachine() {
        var port = new Port();
        var reconciler = new CpfBrokerUnknownResultReconciler(
                port,
                List.of(envelope -> CpfBrokerResult.failed("m1", "BROKER", "rejected")),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(30));

        var result = reconciler.runOnce("worker", 10);

        assertThat(result.resolvedFailure()).isEqualTo(1);
        assertThat(port.marked).isTrue();
        assertThat(port.released).isFalse();
    }

    @Test
    void probeFailureKeepsUnknownWithoutRepublish() {
        var port = new Port();
        var reconciler = new CpfBrokerUnknownResultReconciler(
                port,
                List.of(envelope -> {
                    throw new IllegalStateException("provider unavailable");
                }),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(30));

        var result = reconciler.runOnce("worker", 10);

        assertThat(result.pending()).isEqualTo(1);
        assertThat(port.released).isTrue();
        assertThat(port.marked).isFalse();
    }

    private static final class Port
            implements CpfBrokerOutboxPort, CpfBrokerUnknownResultPort {
        private boolean marked;
        private boolean released;

        @Override
        public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
            return CpfBrokerResult.accepted("m1", "OUTBOX", null);
        }

        @Override
        public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
            return List.of();
        }

        @Override
        public void markPublished(String messageId, CpfBrokerResult result) {
            marked = true;
        }

        @Override
        public void markUnknown(
                String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
        }

        @Override
        public List<CpfBrokerEnvelope> claimUnknown(String workerId, int limit) {
            return List.of(envelope());
        }

        @Override
        public void releaseUnknown(
                String messageId, String detail, Instant nextReconcileAt) {
            released = true;
        }

        private static CpfBrokerEnvelope envelope() {
            return new CpfBrokerEnvelope(
                    "tx", "seg", "P", "C", "idem", NOW,
                    new CpfBrokerMessage(
                            "m1", "topic", "key", new byte[0],
                            "application/json", Map.of()),
                    Map.of());
        }
    }
}
