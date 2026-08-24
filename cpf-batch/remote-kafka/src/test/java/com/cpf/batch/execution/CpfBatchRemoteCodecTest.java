package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.springframework.messaging.Message;
import com.cpf.testkit.context.CpfContextTestSupport;
import com.cpf.testkit.context.CpfTestContextRuntime;
import com.cpf.batch.context.CpfBatchContext;
import com.cpf.batch.context.CpfBatchContextBundle;
import com.cpf.batch.context.CpfBatchLaunchMode;
import com.cpf.batch.execution.internal.context.CpfBatchRuntimeContexts;
import com.cpf.core.api.context.CpfContexts;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

class CpfBatchRemoteCodecTest {
    private static CpfTestContextRuntime runtime;
    private final CpfContextTestSupport contexts=new CpfContextTestSupport("BATCH-REMOTE", LocalDate.of(2026,7,31));
    @BeforeAll static void installContextRuntime(){runtime=CpfTestContextRuntime.install();}
    @AfterAll static void closeContextRuntime(){runtime.close();}
    private final Instant now=Instant.parse("2026-07-31T00:00:00Z");
    private final CpfBatchKafkaRemoteProperties properties=new CpfBatchKafkaRemoteProperties(
            "requests","replies","workers","manager-1","producer-1","qa","tenant-a",
            CpfBatchKafkaRemoteProperties.Role.ALL,Duration.ofSeconds(5),Duration.ofMinutes(5),65536,32768,32,128,100,3);
    private final CpfBatchRemoteCodec codec=new CpfBatchRemoteCodec(new ObjectMapper().findAndRegisterModules(),properties,Clock.fixed(now,ZoneOffset.UTC));

    @Test void samePayloadAndAttemptProduceStableIdentity() {
        StepExecutionRequest payload=new StepExecutionRequest("step",20L);
        var message=MessageBuilder.withPayload(payload).setHeader(CpfBatchRemoteCodec.ATTEMPT,2).build();
        var first=encode(message);var second=encode(message);
        assertThat(first.messageId()).isEqualTo(second.messageId());
        assertThat(first.payloadSha256()).hasSize(64);
        assertThat(first.environment()).isEqualTo("qa");
        assertThat(first.replyTopic()).isEqualTo("replies.manager-1");
    }

    @Test void retryAttemptKeepsStableIdentityAndCorrelation() {
        StepExecutionRequest payload=new StepExecutionRequest("step",20L);
        var first=encode(MessageBuilder.withPayload(payload).setHeader(CpfBatchRemoteCodec.ATTEMPT,1).setHeader(CpfBatchRemoteCodec.CORRELATION_ID,"execution-10").build());
        var second=encode(MessageBuilder.withPayload(payload).setHeader(CpfBatchRemoteCodec.ATTEMPT,2).setHeader(CpfBatchRemoteCodec.CORRELATION_ID,"execution-10").build());
        assertThat(first.messageId()).isEqualTo(second.messageId());
        assertThat(first.correlationId()).isEqualTo(second.correlationId());
    }

    @Test void rejectsPayloadHashTamperingAndWrongEnvironment() {
        CpfBatchRemoteEnvelope valid=encode(MessageBuilder.withPayload(new StepExecutionRequest("step",20L)).build());
        CpfBatchRemoteEnvelope tampered=new CpfBatchRemoteEnvelope(valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),valid.environment(),valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson()+" ",valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt());
        assertThatThrownBy(()->codec.decode(tampered)).isInstanceOf(SecurityException.class).hasMessageContaining("HASH");
        CpfBatchRemoteEnvelope foreign=new CpfBatchRemoteEnvelope(valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),"prod",valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt());
        assertThatThrownBy(()->codec.decode(foreign)).isInstanceOf(SecurityException.class).hasMessageContaining("ENVIRONMENT");
        CpfBatchRemoteEnvelope untrustedProducer=new CpfBatchRemoteEnvelope(valid.schemaVersion(),valid.messageId(),valid.correlationId(),"attacker",valid.environment(),valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.expiresAt());
        assertThatThrownBy(()->codec.decode(untrustedProducer)).isInstanceOf(SecurityException.class).hasMessageContaining("PRODUCER");
    }

    @Test void rejectsExpiredEnvelope() {
        CpfBatchRemoteEnvelope valid=encode(MessageBuilder.withPayload(new StepExecutionRequest("step",20L)).build());
        CpfBatchRemoteCodec future=new CpfBatchRemoteCodec(new ObjectMapper().findAndRegisterModules(),properties,Clock.fixed(now.plus(Duration.ofMinutes(10)),ZoneOffset.UTC));
        assertThatThrownBy(()->future.decode(valid)).isInstanceOf(SecurityException.class).hasMessageContaining("EXPIRED");
    }
    @Test void rejectsOversizedIdentifiersInvalidTopicsAndExcessiveAttempts() {
        CpfBatchRemoteEnvelope valid=encode(MessageBuilder.withPayload(new StepExecutionRequest("step",20L)).build());
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
        CpfBatchRemoteEnvelope valid=encode(MessageBuilder.withPayload(new StepExecutionRequest("step",20L)).build());
        CpfBatchRemoteEnvelope excessive=new CpfBatchRemoteEnvelope(
                valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),valid.environment(),
                valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),
                valid.payloadSha256(),valid.headers(),valid.createdAt(),valid.createdAt().plus(Duration.ofHours(1)));
        assertThatThrownBy(()->codec.decode(excessive)).isInstanceOf(SecurityException.class).hasMessageContaining("EXPIRED");
    }

    @Test void rejectsNonAllowlistedWireHeader() {
        CpfBatchRemoteEnvelope valid=encode(MessageBuilder.withPayload(new StepExecutionRequest("step",20L)).build());
        java.util.Map<String,Object> headers=new java.util.LinkedHashMap<>(valid.headers());
        headers.put("replyChannel","untrusted-channel");
        CpfBatchRemoteEnvelope injected=new CpfBatchRemoteEnvelope(
                valid.schemaVersion(),valid.messageId(),valid.correlationId(),valid.producerId(),valid.environment(),
                valid.tenantId(),valid.attempt(),valid.replyTopic(),valid.payloadType(),valid.payloadJson(),
                valid.payloadSha256(),headers,valid.createdAt(),valid.expiresAt());
        assertThatThrownBy(()->codec.decode(injected)).isInstanceOf(SecurityException.class)
                .hasMessageContaining("HEADER_DENIED:replyChannel");
    }

    @Test void roundTripsBoundedStepExecutionReplyAndAggregationHeaders() {
        JobExecution jobExecution=new JobExecution(11L,new JobInstance(7L,"job"),new JobParameters());
        StepExecution source=new StepExecution(23L,"worker:partition-1",jobExecution);
        source.setVersion(4);source.setStatus(BatchStatus.COMPLETED);
        source.setExitStatus(new ExitStatus("COMPLETED","worker result"));
        source.setReadCount(2);source.setWriteCount(1);source.setCommitCount(1);
        source.setRollbackCount(0);source.setReadSkipCount(1);source.setProcessSkipCount(2);
        source.setWriteSkipCount(3);source.setFilterCount(4);
        source.setCreateTime(LocalDateTime.parse("2026-07-31T00:00:00"));
        source.setStartTime(LocalDateTime.parse("2026-07-31T00:00:01"));
        source.setEndTime(LocalDateTime.parse("2026-07-31T00:00:02"));
        source.setLastUpdated(LocalDateTime.parse("2026-07-31T00:00:03"));
        Message<StepExecution> message=MessageBuilder.withPayload(source)
                .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID,"11:worker")
                .setHeader(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER,1)
                .setHeader(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE,6).build();

        Message<?> decoded=codec.decode(encode(message));

        assertThat(decoded.getPayload()).isInstanceOf(StepExecution.class);
        StepExecution actual=(StepExecution)decoded.getPayload();
        assertThat(actual.getId()).isEqualTo(23L);assertThat(actual.getVersion()).isEqualTo(4);
        assertThat(actual.getJobExecutionId()).isEqualTo(11L);assertThat(actual.getStepName()).isEqualTo("worker:partition-1");
        assertThat(actual.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(actual.getExitStatus()).isEqualTo(new ExitStatus("COMPLETED","worker result"));
        assertThat(actual.getReadCount()).isEqualTo(2);assertThat(actual.getWriteCount()).isEqualTo(1);
        assertThat(actual.getReadSkipCount()).isEqualTo(1);assertThat(actual.getProcessSkipCount()).isEqualTo(2);
        assertThat(actual.getWriteSkipCount()).isEqualTo(3);assertThat(actual.getFilterCount()).isEqualTo(4);
        assertThat(decoded.getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID)).isEqualTo("11:worker");
        assertThat(decoded.getHeaders().get(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER)).isEqualTo(1);
        assertThat(decoded.getHeaders().get(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE)).isEqualTo(6);
    }

    private CpfBatchRemoteEnvelope encode(Message<?> message) {
        try (AutoCloseable ignoredCore=contexts.bindRoot("batch-remote-test", null, "tester");
             AutoCloseable ignoredBatch=CpfBatchRuntimeContexts.bind(batchContext())) {
            return codec.encode(message);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private CpfBatchContextBundle batchContext() {
        var snapshot=CpfContexts.requireSnapshot();
        var batch=new CpfBatchContext("remote-test","Remote Test",1,"JI-1","JE-1","JE-1",
                "step","SE-1",null,null,CpfBatchLaunchMode.REMOTE_PARTITION,snapshot.transaction().businessDate(),
                0,1,"P-1",null,null,null,"worker-1","test",null,"CP-1",null,null,null,1L,Instant.now());
        return new CpfBatchContextBundle(snapshot,batch);
    }

}
