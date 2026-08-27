package com.cpf.messaging.kafka;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.messaging.api.CpfBrokerPublishRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaCpfMessagingTemplateTest {
    private AutoCloseable cpfContextScope;
    @BeforeEach void bindCpfContext() {
        Clock clock=Clock.fixed(Instant.parse("2026-08-18T00:00:00Z"),ZoneOffset.UTC);
        CpfExecutionIdGenerator ids=new CpfExecutionIdGenerator() { private int n; public String newExecutionId(){return "EX-"+(++n);} public String newSegmentId(){return "segment-1";} };
        CpfContextExecutionFactory factory=new CpfContextExecutionFactory(() -> "transaction-1",ids,() -> LocalDate.of(2026,8,18),clock);
        cpfContextScope=CpfContexts.bind(CpfContextSnapshot.capture(factory.newRoot(null,"messaging.test",null,null,clock.instant().plusSeconds(60)),clock.instant()));
    }
    @AfterEach void clearCpfContext() throws Exception { if(cpfContextScope!=null) cpfContextScope.close(); Thread.interrupted(); }


    @Test void returnsPublishedOnlyAfterBrokerAckAndPropagatesCompleteCpfMetadata(){
        KafkaTemplate<String,byte[]> template=template();@SuppressWarnings("unchecked") SendResult<String,byte[]> sendResult=mock(SendResult.class);RecordMetadata metadata=mock(RecordMetadata.class);
        when(metadata.partition()).thenReturn(2);when(metadata.offset()).thenReturn(9L);when(sendResult.getRecordMetadata()).thenReturn(metadata);
        AtomicReference<ProducerRecord<String,byte[]>> captured = new AtomicReference<>();
        when(template.send(org.mockito.ArgumentMatchers.<org.apache.kafka.clients.producer.ProducerRecord<String,byte[]>>any())).thenAnswer(invocation -> {
            captured.set(invocation.getArgument(0));
            return CompletableFuture.completedFuture(sendResult);
        });
        var result=client(template,Duration.ofMillis(50)).send(request(Map.of("X-A","v")));
        assertThat(result.status()).isEqualTo("PUBLISHED");
        verify(template).send(org.mockito.ArgumentMatchers.<org.apache.kafka.clients.producer.ProducerRecord<String,byte[]>>any());
        ProducerRecord<String,byte[]> sent=java.util.Objects.requireNonNull(captured.get());
        assertThat(value(sent,"cpf-message-id")).isEqualTo("message-1");
        assertThat(value(sent,"cpf-transaction-id")).isEqualTo("transaction-1");
        assertThat(value(sent,"cpf-idempotency-key")).isEqualTo("idempotency-1");
        assertThat(value(sent,"cpf-content-type")).isEqualTo("application/octet-stream");
        assertThat(value(sent,"cpf-segment-id")).isEqualTo("segment-1");
        assertThat(value(sent,"cpf-producer-module")).isEqualTo("producer");
        assertThat(value(sent,"cpf-consumer-module")).isEqualTo("consumer");
        assertThat(value(sent,"X-A")).isEqualTo("v");
    }

    @Test void reservedOrInvalidHeaderFailsBeforeProviderCall(){
        KafkaTemplate<String,byte[]> template=template();
        assertThatThrownBy(()->client(template,Duration.ofSeconds(1)).send(request(Map.of("CPF-TRANSACTION-ID","evil"))))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(()->client(template,Duration.ofSeconds(1)).send(request(Map.of("bad header","v"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void missingIdempotencyFailsBeforeProviderCall(){
        KafkaTemplate<String,byte[]> template=template();
        CpfBrokerPublishRequest invalid=new CpfBrokerPublishRequest("m","t","k",new byte[0],"x","p","c",null,Map.of(),Map.of());
        assertThatThrownBy(()->client(template,Duration.ofSeconds(1)).send(invalid)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("idempotencyKey");
    }

    @Test void synchronousProviderFailureAfterInvocationIsUnknown(){
        KafkaTemplate<String,byte[]> template=template();
        when(template.send(org.mockito.ArgumentMatchers.<org.apache.kafka.clients.producer.ProducerRecord<String,byte[]>>any()))
                .thenThrow(new IllegalStateException("producer state unavailable"));

        assertThatThrownBy(()->client(template,Duration.ofSeconds(1)).send(request(Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UNKNOWN")
                .hasMessageContaining("reconciled");
    }

    @Test void serializationFailureIsDefiniteBeforeWrite(){
        KafkaTemplate<String,byte[]> template=template();
        when(template.send(org.mockito.ArgumentMatchers.<org.apache.kafka.clients.producer.ProducerRecord<String,byte[]>>any()))
                .thenThrow(new SerializationException("serializer rejected payload"));

        assertThatThrownBy(()->client(template,Duration.ofSeconds(1)).send(request(Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before provider write")
                .hasMessageContaining("FAILED")
                .hasMessageNotContaining("UNKNOWN");
    }

    @Test void timeoutIsUnknownWithoutPollutingInterruptFlag(){KafkaTemplate<String,byte[]> t=template();when(t.send(org.mockito.ArgumentMatchers.<org.apache.kafka.clients.producer.ProducerRecord<String,byte[]>>any())).thenReturn(new CompletableFuture<>());assertThatThrownBy(()->client(t,Duration.ofMillis(1)).send(request(Map.of()))).isInstanceOf(IllegalStateException.class).hasMessageContaining("UNKNOWN");assertThat(Thread.currentThread().isInterrupted()).isFalse();}
    @Test void interruptedWaitRestoresInterruptFlag(){KafkaTemplate<String,byte[]> t=template();when(t.send(org.mockito.ArgumentMatchers.<org.apache.kafka.clients.producer.ProducerRecord<String,byte[]>>any())).thenReturn(new CompletableFuture<>());Thread.currentThread().interrupt();assertThatThrownBy(()->client(t,Duration.ofSeconds(1)).send(request(Map.of()))).isInstanceOf(IllegalStateException.class).hasMessageContaining("UNKNOWN");assertThat(Thread.currentThread().isInterrupted()).isTrue();}

    @SuppressWarnings("unchecked") private static KafkaTemplate<String,byte[]> template(){return mock(KafkaTemplate.class);}
    private static KafkaCpfMessagingTemplate client(KafkaTemplate<String,byte[]> t,Duration d){return new KafkaCpfMessagingTemplate(t,new CpfKafkaProperties(d,1024,true));}
    private static CpfBrokerPublishRequest request(Map<String,String> headers){return new CpfBrokerPublishRequest("message-1","cpf.events","partition-key",new byte[]{1},"application/octet-stream","producer","consumer","idempotency-1",headers,Map.of());}
    private static String value(ProducerRecord<String,byte[]> r,String n){var h=r.headers().lastHeader(n);return h==null?null:new String(h.value(),StandardCharsets.UTF_8);}
}
