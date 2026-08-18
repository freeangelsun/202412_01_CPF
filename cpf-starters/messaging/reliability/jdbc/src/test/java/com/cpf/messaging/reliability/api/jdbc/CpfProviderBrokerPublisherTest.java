package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.spi.CpfNamedBrokerClient;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.spi.broker.CpfBrokerEnvelope;
import com.cpf.messaging.spi.broker.CpfBrokerMessage;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import com.cpf.messaging.context.CpfMessageHeaderNames;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.core.api.context.CpfContexts;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfProviderBrokerPublisherTest {
    private static final Instant NOW = Instant.parse("2026-08-05T01:00:00Z");

    @Test
    void workerPublisherMapsCompleteEnvelopeToProviderRequest() {
        RecordingClient provider = new RecordingClient();
        CpfMessagingTemplateRouter router = new CpfMessagingTemplateRouter(List.of(
                new CpfNamedBrokerClient("default", "rabbitmq", true, provider)));
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                router, Clock.fixed(NOW, ZoneOffset.UTC), contextSupport());

        var result = publisher.publish(envelope());

        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(result.messageId()).isEqualTo("msg-1");
        assertThat(provider.seenTransactionId).isEqualTo("tx-1");
        assertThat(provider.seenSegmentId).isNotBlank();
        assertThat(provider.request.idempotencyKey()).isEqualTo("idem-1");
        assertThat(provider.request.payload())
                .containsExactly("payload".getBytes(StandardCharsets.UTF_8));
        assertThat(provider.request.headers()).containsEntry("trace", "abc");
    }

    @Test
    void mismatchedProviderCorrelationBecomesUnknown() {
        CpfMessagingTemplate provider = request -> new CpfBrokerPublishResult(
                "PUBLISHED", "another-message", "rabbitmq", "p0", NOW, null);
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfMessagingTemplateRouter(List.of(
                        new CpfNamedBrokerClient("default", "rabbitmq", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC), contextSupport());

        var result = publisher.publish(envelope());

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(result.messageId()).isEqualTo("msg-1");
        assertThat(result.detail()).contains("correlation mismatch");
    }

    @Test
    void providerFailureDetailIsMaskedBeforePersistence() {
        CpfMessagingTemplate provider = request -> new CpfBrokerPublishResult(
                "FAILED", request.messageId(), "rabbitmq", null, NOW,
                "Authorization=Bearer abc.def password=hunter2 token=tok-1");
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfMessagingTemplateRouter(List.of(
                        new CpfNamedBrokerClient("default", "rabbitmq", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC), contextSupport());

        var result = publisher.publish(envelope());

        assertThat(result.detail()).doesNotContain("abc.def", "hunter2", "tok-1");
        assertThat(result.detail()).contains("***");
    }


    @Test
    void providerExceptionAfterInvocationBecomesSanitizedUnknown() {
        CpfMessagingTemplate provider = request -> {
            throw new IllegalStateException("Authorization=Bearer abc.def password=hunter2");
        };
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfMessagingTemplateRouter(List.of(
                        new CpfNamedBrokerClient("default", "kafka", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC), contextSupport());

        var result = publisher.publish(envelope());

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(result.detail()).contains("no definitive result", "***")
                .doesNotContain("abc.def", "hunter2");
    }

    @Test
    void unsupportedProviderStatusFailsClosedAsUnknown() {
        CpfMessagingTemplate provider = request -> new CpfBrokerPublishResult(
                "QUEUED_SOMEWHERE", request.messageId(), "custom", null, NOW, null);
        CpfProviderBrokerPublisher publisher = new CpfProviderBrokerPublisher(
                new CpfMessagingTemplateRouter(List.of(
                        new CpfNamedBrokerClient("default", "custom", true, provider))),
                Clock.fixed(NOW, ZoneOffset.UTC), contextSupport());

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
                        contextHeaders()),
                Map.of("tenant", "T1"));
    }

    private static Map<String, String> contextHeaders() {
        return Map.of(
                CpfMessageHeaderNames.TRANSACTION_ID, "tx-1",
                CpfMessageHeaderNames.ROOT_TRANSACTION_ID, "tx-1",
                CpfMessageHeaderNames.BUSINESS_DATE, LocalDate.of(2026, 8, 5).toString(),
                CpfMessageHeaderNames.PARENT_EXECUTION_ID, "execution-1",
                CpfMessageHeaderNames.ROOT_EXECUTION_ID, "execution-1",
                CpfMessageHeaderNames.PARENT_SEGMENT_ID, "seg-1",
                "trace", "abc");
    }

    private static CpfMessageBridgeContextSupport contextSupport() {
        CpfExecutionIdGenerator ids = new CpfExecutionIdGenerator() {
            private int value;
            @Override public String newExecutionId() { return "worker-execution-" + (++value); }
            @Override public String newSegmentId() { return "worker-segment-" + value; }
        };
        return new CpfMessageBridgeContextSupport(ids, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static final class RecordingClient implements CpfMessagingTemplate {
        private CpfBrokerPublishRequest request;
        private String seenTransactionId;
        private String seenSegmentId;

        @Override
        public CpfBrokerPublishResult send(CpfBrokerPublishRequest request) {
            this.request = request;
            this.seenTransactionId = CpfContexts.transactionId();
            this.seenSegmentId = CpfContexts.requireCurrent().execution().segmentId();
            return new CpfBrokerPublishResult(
                    "PUBLISHED", request.messageId(), "rabbitmq", "p0", NOW, null);
        }
    }
}
