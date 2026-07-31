package com.cpf.starter.kafka;

import com.cpf.core.api.broker.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;

/** Kafka broker ACK를 확인한 뒤에만 PUBLISHED를 반환하는 Product Adapter입니다. */
public final class KafkaCpfBrokerClient implements CpfBrokerClient {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final CpfKafkaProperties properties;

    public KafkaCpfBrokerClient(KafkaTemplate<String, byte[]> kafkaTemplate, CpfKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        if (request.payload().length > properties.maximumPayloadBytes()) {
            throw new IllegalArgumentException("Kafka message payload exceeds CPF maximumPayloadBytes.");
        }
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(request.topic(), request.key(), request.payload());
        addHeader(record, "cpf-message-id", request.messageId());
        addHeader(record, "cpf-transaction-id", request.transactionId());
        addHeader(record, "cpf-idempotency-key", request.idempotencyKey());
        for (Map.Entry<String, String> header : request.headers().entrySet()) addHeader(record, header.getKey(), header.getValue());
        try {
            var result = kafkaTemplate.send(record).get(properties.acknowledgementTimeout().toMillis(), TimeUnit.MILLISECONDS);
            return new CpfBrokerPublishResult("PUBLISHED", request.messageId(), "KAFKA",
                    Integer.toString(result.getRecordMetadata().partition()), Instant.now(),
                    "offset=" + result.getRecordMetadata().offset());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka broker acknowledgement was not confirmed; result is UNKNOWN.", e);
        }
    }

    private static void addHeader(ProducerRecord<String, byte[]> record, String name, String value) {
        if (value == null || value.isBlank()) return;
        if (!name.matches("[A-Za-z0-9._-]{1,128}")) throw new IllegalArgumentException("Invalid Kafka header name: " + name);
        record.headers().add(new RecordHeader(name, value.getBytes(StandardCharsets.UTF_8)));
    }
}
