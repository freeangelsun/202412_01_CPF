package com.cpf.batch.execution;

import java.time.Duration;
import java.util.Objects;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

/** Kafka Broker ACK를 확인한 뒤에만 send 성공을 반환하는 MessageChannel입니다. */
public final class CpfKafkaOutboundMessageChannel implements MessageChannel {
    private final KafkaTemplate<String, String> kafka;
    private final CpfBatchRemoteCodec codec;
    private final String topic;
    private final Duration timeout;

    public CpfKafkaOutboundMessageChannel(
            KafkaTemplate<String, String> kafka,
            CpfBatchRemoteCodec codec,
            String topic,
            Duration timeout) {
        this.kafka = kafka; this.codec = codec;
        this.topic = Objects.requireNonNull(topic); this.timeout = timeout;
    }

    @Override public boolean send(Message<?> message) { return send(message, timeout.toMillis()); }

    @Override
    public boolean send(Message<?> message, long timeoutMillis) {
        CpfBatchRemoteEnvelope envelope = codec.encode(message);
        String json = codec.encodeJson(envelope);
        try {
            kafka.send(topic, envelope.messageId(), json)
                    .get(Math.max(1L, timeoutMillis), java.util.concurrent.TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("BATCH_REMOTE_KAFKA_INTERRUPTED", interrupted);
        } catch (Exception failure) {
            throw new IllegalStateException("BATCH_REMOTE_KAFKA_ACK_FAILED", failure);
        }
    }
}
