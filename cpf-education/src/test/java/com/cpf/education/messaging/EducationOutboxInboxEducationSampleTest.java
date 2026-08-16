package com.cpf.education.messaging;

import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.api.CpfBrokerPublishResultProbe;
import com.cpf.messaging.api.CpfBrokerUnknownResultPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EducationOutboxInboxEducationSampleTest {

    @Test
    void publishUsesPublicReliabilityBoundary() {
        AtomicReference<CpfBrokerPublishRequest> captured = new AtomicReference<>();
        EducationOutboxInboxEducationSample sample = new EducationOutboxInboxEducationSample(
                request -> {
                    captured.set(request);
                    return result("ACCEPTED", request.messageId(), "queued");
                },
                new StubUnknownPort());
        CpfBrokerPublishRequest request = request("TX-1", "IDEM-1");

        CpfBrokerPublishResult accepted = sample.publish(request);

        assertThat(accepted.status()).isEqualTo("ACCEPTED");
        assertThat(captured.get()).isEqualTo(request);
    }

    @Test
    void unknownResultIsReconciledThroughPublicPort() {
        StubUnknownPort port = new StubUnknownPort();
        EducationOutboxInboxEducationSample sample = new EducationOutboxInboxEducationSample(
                request -> result("UNKNOWN", request.messageId(), "response lost"), port);
        CpfBrokerPublishRequest request = request("TX-2", "IDEM-2");

        CpfBrokerPublishResult reconciled = sample.reconcile(request, "operator-1", "confirm provider result");

        assertThat(reconciled.status()).isEqualTo("PUBLISHED");
        assertThat(port.probe).isEqualTo(new CpfBrokerPublishResultProbe("IDEM-2", "TX-2", "edu.topic"));
        assertThat(port.operatorId).isEqualTo("operator-1");
        assertThat(port.reason).isEqualTo("confirm provider result");
    }

    private static CpfBrokerPublishRequest request(String transactionId, String idempotencyKey) {
        return new CpfBrokerPublishRequest(
                "MSG-" + idempotencyKey, "edu.topic", idempotencyKey, new byte[]{1}, "application/octet-stream",
                transactionId, transactionId + "-SEG", "EDU", "EDU", idempotencyKey,
                java.util.Map.of(), java.util.Map.of());
    }

    private static CpfBrokerPublishResult result(String status, String messageId, String detail) {
        return new CpfBrokerPublishResult(
                status, messageId, "test-broker", "partition-1",
                Instant.parse("2026-08-04T00:00:00Z"), detail);
    }

    private static final class StubUnknownPort implements CpfBrokerUnknownResultPort {
        private CpfBrokerPublishResultProbe probe;
        private String operatorId;
        private String reason;

        @Override
        public CpfBrokerPublishResult probe(CpfBrokerPublishResultProbe probe) {
            this.probe = probe;
            return result("UNKNOWN", "MSG-UNKNOWN", "still unknown");
        }

        @Override
        public CpfBrokerPublishResult reconcile(CpfBrokerPublishResultProbe probe, String operatorId, String reason) {
            this.probe = probe;
            this.operatorId = operatorId;
            this.reason = reason;
            return result("PUBLISHED", "MSG-RECONCILED", "confirmed");
        }
    }
}
