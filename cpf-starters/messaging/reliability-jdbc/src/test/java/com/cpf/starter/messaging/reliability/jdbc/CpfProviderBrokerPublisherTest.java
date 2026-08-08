package com.cpf.starter.messaging.reliability.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerMessage;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfProviderBrokerPublisherTest {
    private static final Instant NOW = Instant.parse("2026-08-05T01:00:00Z");

    @Test
    void workerPublisherMapsCompleteEnvelopeToProviderRequest() {
        RecordingClient provider = new RecordingClient();
        CpfBrokerClientRouter router = new CpfBrokerClientRouter(List.of(
                new CpfNamedBrokerClient("default", "rabbitmq", true, provider)));
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                router, Clock.fixed(NOW, ZoneOffset.UTC));

        var result = publisher.publish(envelope());

        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(result.messageId()).isEqualTo("msg-1");
        assertThat(provider.request.transactionId()).isEqualTo("tx-1");
        assertThat(provider.request.idempotencyKey()).isEqualTo("idem-1");
        assertThat(provider.request.payload())
                .containsExactly("payload".getBytes(StandardCharsets.UTF_8));
        assertThat(provider.request.headers()).containsEntry("trace", "abc");
    }

    @Test
    void mismatchedProviderCorrelationBecomesUnknown() {
        CpfBrokerClient provider = request -> new CpfBrokerPublishResult(
                "PUBLISHED", "another-message", "rabbitmq", "p0", NOW, null);
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfBrokerClientRouter(List.of(
                        new CpfNamedBrokerClient("default", "rabbitmq", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC));

        var result = publisher.publish(envelope());

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(result.messageId()).isEqualTo("msg-1");
        assertThat(result.detail()).contains("correlation mismatch");
    }

    @Test
    void providerFailureDetailIsMaskedBeforePersistence() {
        CpfBrokerClient provider = request -> new CpfBrokerPublishResult(
                "FAILED", request.messageId(), "rabbitmq", null, NOW,
                "Authorization=Bearer abc.def password=hunter2 token=tok-1");
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfBrokerClientRouter(List.of(
                        new CpfNamedBrokerClient("default", "rabbitmq", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC));

        var result = publisher.publish(envelope());

        assertThat(result.detail()).doesNotContain("abc.def", "hunter2", "tok-1");
        assertThat(result.detail()).contains("***");
    }


    @Test
    void providerExceptionAfterInvocationBecomesSanitizedUnknown() {
        CpfBrokerClient provider = request -> {
            throw new IllegalStateException("Authorization=Bearer abc.def password=hunter2");
        };
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfBrokerClientRouter(List.of(
                        new CpfNamedBrokerClient("default", "kafka", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC));

        var result = publisher.publish(envelope());

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(result.detail()).contains("no definitive result", "***")
                .doesNotContain("abc.def", "hunter2");
    }

    @Test
    void unsupportedProviderStatusFailsClosedAsUnknown() {
        CpfBrokerClient provider = request -> new CpfBrokerPublishResult(
                "QUEUED_SOMEWHERE", request.messageId(), "custom", null, NOW, null);
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfBrokerClientRouter(List.of(
                        new CpfNamedBrokerClient("default", "custom", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC));

        var result = publisher.publish(envelope());

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(result.detail()).contains("unsupported status");
    }

    private static CpfBrokerEnvelope envelope() {
        return new CpfBrokerEnvelope(
                "tx-1", "seg-1", "producer", "consumer", "idem-1", NOW,
                new CpfBrokerMessage(
                        "msg-1", "topic-1", "key-1",
                        "payload".getBytes(StandardCharsets.UTF_8), "text/plain",
                        Map.of("trace", "abc")),
                Map.of("tenant", "T1"));
    }

    private static final class RecordingClient implements CpfBrokerClient {
        private CpfBrokerPublishRequest request;

        @Override
        public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
            this.request = request;
            return new CpfBrokerPublishResult(
                    "PUBLISHED", request.messageId(), "rabbitmq", "p0", NOW, null);
        }
    }
}
