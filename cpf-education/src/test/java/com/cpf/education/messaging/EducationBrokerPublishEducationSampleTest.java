package com.cpf.education.messaging;

import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EducationBrokerPublishEducationSampleTest {

    @Test
    void publishPlanUsesPublicBrokerRequestOnly() {
        var sample = new EducationBrokerPublishEducationSample(request -> published(request.messageId()));
        CpfBrokerPublishRequest request = sample.publishPlan("T-1", "ID-1");

        assertThat(request.topic()).isEqualTo("com.cpf.education.changed");
        assertThat(request.producerModule()).isEqualTo("EDU");
        assertThat(request.consumerModule()).isEqualTo("EDU");
        assertThat(request.transactionId()).isEqualTo("T-1");
        assertThat(request.idempotencyKey()).isEqualTo("ID-1");
        assertThat(request.headers()).containsEntry("x-cpf-transaction-id", "T-1");
    }

    @Test
    void publishCallsProviderNeutralBrokerClientWithFullTrackingContract() {
        AtomicReference<CpfBrokerPublishRequest> captured = new AtomicReference<>();
        var sample = new EducationBrokerPublishEducationSample(request -> {
            captured.set(request);
            return published(request.messageId());
        });

        CpfBrokerPublishResult result = sample.publish("T-1", "ID-1");

        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().messageId()).isEqualTo("EDU-ID-1");
        assertThat(captured.get().topic()).isEqualTo("com.cpf.education.changed");
        assertThat(captured.get().transactionId()).isEqualTo("T-1");
        assertThat(captured.get().segmentId()).isEqualTo("T-1-BROKER");
        assertThat(captured.get().producerModule()).isEqualTo("EDU");
        assertThat(captured.get().consumerModule()).isEqualTo("EDU");
        assertThat(captured.get().idempotencyKey()).isEqualTo("ID-1");
        assertThat(captured.get().headers()).containsEntry("x-cpf-transaction-id", "T-1");
        assertThat(captured.get().attributes()).containsEntry("sampleId", "EDU-MSG-001");
    }

    @Test
    void publishPreservesUnknownInsteadOfReportingFalseSuccess() {
        var sample = new EducationBrokerPublishEducationSample(request -> new CpfBrokerPublishResult(
                "UNKNOWN", request.messageId(), "test-broker", request.key(),
                Instant.parse("2026-08-04T00:00:00Z"), "acknowledgement not observed"));

        CpfBrokerPublishResult result = sample.publish("T-2", "ID-2");

        assertThat(result.unknown()).isTrue();
        assertThat(result.detail()).contains("acknowledgement");
    }

    @Test
    void publishPropagatesProviderFailureForFrameworkErrorMapping() {
        var sample = new EducationBrokerPublishEducationSample(request -> {
            throw new IllegalStateException("broker unavailable");
        });

        assertThatThrownBy(() -> sample.publish("T-3", "ID-3"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("broker unavailable");
    }

    @Test
    void publishRejectsBlankTrackingBeforeAnySideEffect() {
        AtomicInteger calls = new AtomicInteger();
        var sample = new EducationBrokerPublishEducationSample(request -> {
            calls.incrementAndGet();
            return published(request.messageId());
        });

        assertThatIllegalArgumentException().isThrownBy(() -> sample.publish(" ", "ID-4"));
        assertThatIllegalArgumentException().isThrownBy(() -> sample.publish("T-4", " "));
        assertThat(calls).hasValue(0);
    }

    private static CpfBrokerPublishResult published(String messageId) {
        return new CpfBrokerPublishResult(
                "PUBLISHED", messageId, "test-broker", "partition-1",
                Instant.parse("2026-08-04T00:00:00Z"), "accepted");
    }
}
