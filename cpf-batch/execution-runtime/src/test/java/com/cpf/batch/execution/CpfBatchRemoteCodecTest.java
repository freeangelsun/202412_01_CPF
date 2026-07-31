package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.messaging.support.MessageBuilder;

class CpfBatchRemoteCodecTest {
    private final Instant now=Instant.parse("2026-07-31T00:00:00Z");
    private final CpfBatchKafkaRemoteProperties properties=new CpfBatchKafkaRemoteProperties(
            "requests","replies","workers","manager-1","producer-1","qa","tenant-a",
            CpfBatchKafkaRemoteProperties.Role.ALL,Duration.ofSeconds(5),Duration.ofMinutes(5),65536,32768,16,128,100,3);
    private final CpfBatchRemoteCodec codec=new CpfBatchRemoteCodec(new ObjectMapper().findAndRegisterModules(),properties,Clock.fixed(now,ZoneOffset.UTC));

    @Test void samePayloadAndAttemptProduceStableIdentity() {
        StepExecutionRequest payload=new StepExecutionRequest("step",10L,20L);
        var message=MessageBuilder.withPayload(payload).setHeader(CpfBatchRemoteCodec.ATTEMPT,2).build();
        var first=codec.encode(message);var second=codec.encode(message);
        assertThat(first.messageId()).isEqualTo(second.messageId());
        assertThat(first.payloadSha256()).hasSize(64);
        assertThat(first.environment()).isEqualTo("qa");
        assertThat(first.replyTopic()).isEqualTo("replies.manager-1");
    }

    @Test void retryAttemptKeepsStableIdentityAndCorrelation() {
        StepExecutionRequest payload=new StepExecutionRequest("step",10L,20L);
        var first=codec.encode(MessageBuilder.withPayload(payload).setHeader(CpfBatchRemoteCodec.ATTEMPT,1).setHeader(CpfBatchRemoteCodec.CORRELATION_ID,"execution-10").build());
        var second=codec.encode(MessageBuilder.withPayload(payload).setHeader(CpfBatchRemoteCodec.ATTEMPT,2).setHeader(CpfBatchRemoteCodec.CORRELATION_ID,"execution-10").build());
        assertThat(first.messageId()).isEqualTo(second.messageId());
        assertThat(first.correlationId()).isEqualTo(second.correlationId());
    }

    @Test void rejectsPayloadHashTamperingAndWrongEnvironment() {
        CpfBatchRemoteEnvelope valid=codec.encode(MessageBuilder.withPayload(new StepExecutionRequest("step",10L,20L)).build());
        CpfBatchRemoteEnvelope tampered=new CpfBatchRemoteEnvelope(valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),valid.environment(),valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson()+" ",valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt());
        assertThatThrownBy(()->codec.decode(tampered)).isInstanceOf(SecurityException.class).hasMessageContaining("HASH");
        CpfBatchRemoteEnvelope foreign=new CpfBatchRemoteEnvelope(valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),"prod",valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt());
        assertThatThrownBy(()->codec.decode(foreign)).isInstanceOf(SecurityException.class).hasMessageContaining("ENVIRONMENT");
        CpfBatchRemoteEnvelope untrustedProducer=new CpfBatchRemoteEnvelope(valid.schemaVersion(),valid.messageId(),valid.correlationId(),"attacker",valid.environment(),valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt());
        assertThatThrownBy(()->codec.decode(untrustedProducer)).isInstanceOf(SecurityException.class).hasMessageContaining("PRODUCER");
    }

    @Test void rejectsExpiredEnvelope() {
        CpfBatchRemoteEnvelope valid=codec.encode(MessageBuilder.withPayload(new StepExecutionRequest("step",10L,20L)).build());
        CpfBatchRemoteCodec future=new CpfBatchRemoteCodec(new ObjectMapper().findAndRegisterModules(),properties,Clock.fixed(now.plus(Duration.ofMinutes(10)),ZoneOffset.UTC));
        assertThatThrownBy(()->future.decode(valid)).isInstanceOf(SecurityException.class).hasMessageContaining("EXPIRED");
    }
    @Test void rejectsOversizedIdentifiersInvalidTopicsAndExcessiveAttempts() {
        CpfBatchRemoteEnvelope valid=codec.encode(MessageBuilder.withPayload(new StepExecutionRequest("step",10L,20L)).build());
        String oversized="x".repeat(129);
        assertThatThrownBy(()->codec.decode(new CpfBatchRemoteEnvelope(
                valid.schemaVersion(),oversized,valid.correlationId(),valid.producerId(),valid.environment(),
                valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),
                valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt())))
                .isInstanceOf(SecurityException.class).hasMessageContaining("MESSAGE_ID");
        assertThatThrownBy(()->codec.decode(new CpfBatchRemoteEnvelope(
                valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),valid.environment(),
                valid.tenantId(),1001,"replies..manager",valid.payloadType(),valid.payloadJson(),
                valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt())))
                .isInstanceOf(SecurityException.class);
    }

    @Test void rejectsLifetimeLongerThanConfiguredTtl() {
        CpfBatchRemoteEnvelope valid=codec.encode(MessageBuilder.withPayload(new StepExecutionRequest("step",10L,20L)).build());
        CpfBatchRemoteEnvelope excessive=new CpfBatchRemoteEnvelope(
                valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),valid.environment(),
                valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),
                valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.createdAt().plus(Duration.ofHours(1)));
        assertThatThrownBy(()->codec.decode(excessive)).isInstanceOf(SecurityException.class).hasMessageContaining("EXPIRED");
    }

}
