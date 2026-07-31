package com.cpf.starter.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaCpfBrokerClientTest {
    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void returnsPublishedOnlyAfterBrokerAck() throws Exception {
        KafkaTemplate<String, byte[]> template = template();
        SendResult<String, byte[]> sendResult = mock(SendResult.class);
        RecordMetadata metadata = mock(RecordMetadata.class);
        when(metadata.partition()).thenReturn(2);
        when(metadata.offset()).thenReturn(9L);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(template.send(any())).thenReturn(CompletableFuture.completedFuture(sendResult));

        var result = client(template, Duration.ofMillis(50)).enqueue(request());

        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(result.partitionKey()).isEqualTo("2");
        assertThat(result.detail()).isEqualTo("offset=9");
    }

    @Test
    void timeoutIsUnknownWithoutPollutingInterruptFlag() {
        KafkaTemplate<String, byte[]> template = template();
        when(template.send(any())).thenReturn(new CompletableFuture<>());

        assertThatThrownBy(() -> client(template, Duration.ofMillis(1)).enqueue(request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UNKNOWN");
        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    void interruptedWaitRestoresInterruptFlag() {
        KafkaTemplate<String, byte[]> template = template();
        when(template.send(any())).thenReturn(new CompletableFuture<>());
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> client(template, Duration.ofSeconds(1)).enqueue(request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UNKNOWN");
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private static KafkaTemplate<String, byte[]> template() {
        return mock(KafkaTemplate.class);
    }

    private static KafkaCpfBrokerClient client(KafkaTemplate<String, byte[]> template, Duration timeout) {
        return new KafkaCpfBrokerClient(template, new CpfKafkaProperties(timeout, 1024, true));
    }

    private static CpfBrokerPublishRequest request() {
        return new CpfBrokerPublishRequest(
                "message-1",
                "cpf.events",
                "partition-key",
                new byte[] {1},
                "application/octet-stream",
                "transaction-1",
                null,
                "producer",
                "consumer",
                "idempotency-1",
                Map.of(),
                Map.of());
    }
}
