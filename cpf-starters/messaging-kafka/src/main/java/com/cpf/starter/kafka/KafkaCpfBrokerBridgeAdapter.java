package com.cpf.starter.kafka;

import com.cpf.core.api.broker.CpfBrokerBridgeHandler;
import com.cpf.core.api.broker.CpfBrokerBridgeMessage;
import com.cpf.core.api.broker.CpfBrokerBridgePort;
import com.cpf.core.api.broker.CpfBrokerBridgeResult;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.util.CpfHeaders;
import com.cpf.core.api.workflow.CpfWorkflow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;

/** Kafka Profile의 {@link CpfBrokerBridgePort} Product Adapter입니다. */
public final class KafkaCpfBrokerBridgeAdapter implements CpfBrokerBridgePort {
    private static final int RECENT_LIMIT = 200;

    private final KafkaTemplate<String, byte[]> kafka;
    private final CpfKafkaProperties properties;
    private final ObjectMapper mapper;
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recent = new ConcurrentLinkedDeque<>();

    public KafkaCpfBrokerBridgeAdapter(
            KafkaTemplate<String, byte[]> kafka,
            CpfKafkaProperties properties,
            ObjectMapper mapper) {
        this.kafka = kafka;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public CpfBrokerBridgeResult publish(
            String destination,
            String key,
            Object payload,
            Map<String, String> additionalHeaders) {
        String topic = required(destination, "destination");
        String resolvedKey = hasText(key) ? key : CpfTransactionContext.transactionId();
        Map<String, String> headers = propagationHeaders(additionalHeaders);
        CpfBrokerBridgeMessage message = new CpfBrokerBridgeMessage(
                "KAFKA", topic, resolvedKey, payload, headers, Instant.now());
        byte[] body;
        try {
            body = mapper.writeValueAsBytes(message);
        } catch (JsonProcessingException failure) {
            throw new IllegalArgumentException("Kafka bridge payload cannot be serialized", failure);
        }
        if (body.length > properties.maximumPayloadBytes()) {
            throw new IllegalArgumentException("Kafka bridge payload exceeds maximumPayloadBytes");
        }

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, resolvedKey, body);
        headers.forEach((name, value) -> addHeader(record, name, value));
        try {
            var result = kafka.send(record)
                    .get(properties.acknowledgementTimeout().toMillis(), TimeUnit.MILLISECONDS);
            remember(message);
            return new CpfBrokerBridgeResult(
                    true,
                    "KAFKA",
                    topic,
                    resolvedKey,
                    headers.get(CpfHeaders.transactionId()),
                    "partition=" + result.getRecordMetadata().partition()
                            + ", offset=" + result.getRecordMetadata().offset());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka publish was interrupted; result is UNKNOWN", interrupted);
        } catch (Exception failure) {
            throw new IllegalStateException("Kafka broker acknowledgement was not confirmed; result is UNKNOWN", failure);
        }
    }

    @Override
    public void subscribe(String destination, CpfBrokerBridgeHandler handler) {
        throw new UnsupportedOperationException(
                "Kafka subscription requires an explicit @KafkaListener or listener-container Consumer owner");
    }

    @Override
    public List<CpfBrokerBridgeMessage> findRecent(String destination, int limit) {
        String topic = hasText(destination) ? destination : null;
        int bounded = Math.max(1, Math.min(limit <= 0 ? 50 : limit, RECENT_LIMIT));
        return recent.stream()
                .filter(message -> topic == null || topic.equals(message.destination()))
                .sorted(Comparator.comparing(CpfBrokerBridgeMessage::createdAt).reversed())
                .limit(bounded)
                .toList();
    }

    private Map<String, String> propagationHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.putAll(CpfTransactionContext.propagationHeaders());
        headers.putAll(CpfWorkflow.propagationHeaders());
        if (additionalHeaders != null) {
            additionalHeaders.forEach((name, value) -> {
                if (hasText(name) && value != null) headers.put(name, value);
            });
        }
        return Map.copyOf(headers);
    }

    private static void addHeader(ProducerRecord<String, byte[]> record, String name, String value) {
        if (!hasText(value)) return;
        if (!name.matches("[A-Za-z0-9._-]{1,128}")) {
            throw new IllegalArgumentException("Invalid Kafka header name: " + name);
        }
        record.headers().add(new RecordHeader(name, value.getBytes(StandardCharsets.UTF_8)));
    }

    private void remember(CpfBrokerBridgeMessage message) {
        recent.addFirst(message);
        while (recent.size() > RECENT_LIMIT) recent.pollLast();
    }

    private static String required(String value, String field) {
        if (!hasText(value)) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
