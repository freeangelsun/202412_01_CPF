package com.cpf.reference.messaging;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceBrokerPublishEducationSampleTest {

    @Test
    void publishPlanUsesCpfBrokerEnvelope() {
        var sample = new ReferenceBrokerPublishEducationSample(request -> published(request.messageId()));
        var envelope = sample.publishPlan("T-1", "ID-1");

        assertThat(envelope.message().topic()).isEqualTo("com.cpf.reference.changed");
        assertThat(envelope.producerModule()).isEqualTo("REF");
        assertThat(envelope.consumerModule()).isEqualTo("REF");
        assertThat(envelope.transactionId()).isEqualTo("T-1");
        assertThat(envelope.idempotencyKey()).isEqualTo("ID-1");
    }

    @Test
    void publishCallsProviderNeutralBrokerClientWithFullTrackingContract() {
        AtomicReference<CpfBrokerPublishRequest> captured = new AtomicReference<>();
        var sample = new ReferenceBrokerPublishEducationSample(request -> {
            captured.set(request);
            return published(request.messageId());
        });

        CpfBrokerPublishResult result = sample.publish("T-1", "ID-1");

        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().messageId()).isEqualTo("REF-ID-1");
        assertThat(captured.get().topic()).isEqualTo("com.cpf.reference.changed");
        assertThat(captured.get().transactionId()).isEqualTo("T-1");
        assertThat(captured.get().segmentId()).isEqualTo("T-1-BROKER");
        assertThat(captured.get().producerModule()).isEqualTo("REF");
        assertThat(captured.get().consumerModule()).isEqualTo("REF");
        assertThat(captured.get().idempotencyKey()).isEqualTo("ID-1");
        assertThat(captured.get().headers()).containsEntry("x-cpf-transaction-id", "T-1");
        assertThat(captured.get().attributes()).containsEntry("sampleId", "REF Reference-MSG-001");
    }

    @Test
    void publishPreservesUnknownInsteadOfReportingFalseSuccess() {
        var sample = new ReferenceBrokerPublishEducationSample(request -> new CpfBrokerPublishResult(
                "UNKNOWN",
                request.messageId(),
                "test-broker",
                request.key(),
                Instant.parse("2026-08-04T00:00:00Z"),
                "acknowledgement not observed"));

        CpfBrokerPublishResult result = sample.publish("T-2", "ID-2");

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(result.detail()).contains("acknowledgement");
    }

    @Test
    void publishPropagatesProviderFailureForFrameworkErrorMapping() {
        var sample = new ReferenceBrokerPublishEducationSample(request -> {
            throw new IllegalStateException("broker unavailable");
        });

        assertThatThrownBy(() -> sample.publish("T-3", "ID-3"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("broker unavailable");
    }

    @Test
    void publishRejectsBlankTrackingBeforeAnySideEffect() {
        AtomicInteger calls = new AtomicInteger();
        var sample = new ReferenceBrokerPublishEducationSample(request -> {
            calls.incrementAndGet();
            return published(request.messageId());
        });

        assertThatIllegalArgumentException()
                .isThrownBy(() -> sample.publish(" ", "ID-4"))
                .withMessage("transactionId is required");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sample.publish("T-4", " "))
                .withMessage("idempotencyKey is required");
        assertThat(calls).hasValue(0);
    }

    private static CpfBrokerPublishResult published(String messageId) {
        return new CpfBrokerPublishResult(
                "PUBLISHED",
                messageId,
                "test-broker",
                "partition-1",
                Instant.parse("2026-08-04T00:00:00Z"),
                "accepted");
    }
}
