package com.cpf.batch.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.integration.chunk.ChunkRequest;
import org.springframework.batch.integration.chunk.ChunkResponse;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/** 허용된 Spring Batch Remote Payload만 명시적 Class Map으로 JSON 직렬화합니다. */
public final class CpfBatchRemoteCodec {
    private final ObjectMapper mapper;
    private final Map<String, JavaType> allowedTypes;

    public CpfBatchRemoteCodec(ObjectMapper mapper) {
        this.mapper = mapper;
        this.allowedTypes = Map.of(
                StepExecutionRequest.class.getName(), mapper.constructType(StepExecutionRequest.class),
                ChunkRequest.class.getName(), mapper.constructType(ChunkRequest.class),
                ChunkResponse.class.getName(), mapper.constructType(ChunkResponse.class),
                StepExecution.class.getName(), mapper.constructType(StepExecution.class));
    }

    public CpfBatchRemoteEnvelope encode(Message<?> message) {
        Object payload = message.getPayload();
        String type = payload.getClass().getName();
        requireAllowed(type);
        try {
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            message.getHeaders().forEach((name, value) -> {
                if (value instanceof String || value instanceof Number || value instanceof Boolean) headers.put(name, value);
            });
            return new CpfBatchRemoteEnvelope(UUID.randomUUID().toString(), type,
                    mapper.writeValueAsString(payload), Map.copyOf(headers), Instant.now());
        } catch (JsonProcessingException failure) {
            throw new IllegalArgumentException("BATCH_REMOTE_PAYLOAD_SERIALIZATION_FAILED", failure);
        }
    }

    public String encodeJson(CpfBatchRemoteEnvelope envelope) {
        try { return mapper.writeValueAsString(envelope); }
        catch (JsonProcessingException failure) { throw new IllegalArgumentException("BATCH_REMOTE_ENVELOPE_SERIALIZATION_FAILED", failure); }
    }

    public Message<?> decodeJson(String json) {
        try { return decode(mapper.readValue(json, CpfBatchRemoteEnvelope.class)); }
        catch (JsonProcessingException failure) { throw new IllegalArgumentException("BATCH_REMOTE_ENVELOPE_DESERIALIZATION_FAILED", failure); }
    }

    public Message<?> decode(CpfBatchRemoteEnvelope envelope) {
        JavaType type = requireAllowed(envelope.payloadType());
        try {
            Object payload = mapper.readValue(envelope.payloadJson(), type);
            MessageBuilder<Object> builder = MessageBuilder.withPayload(payload);
            envelope.headers().forEach((name, value) -> {
                if (value instanceof String || value instanceof Number || value instanceof Boolean) builder.setHeaderIfAbsent(name, value);
            });
            builder.setHeader("cpfBatchRemoteMessageId", envelope.messageId());
            return builder.build();
        } catch (JsonProcessingException failure) {
            throw new IllegalArgumentException("BATCH_REMOTE_PAYLOAD_DESERIALIZATION_FAILED", failure);
        }
    }

    private JavaType requireAllowed(String type) {
        JavaType javaType = allowedTypes.get(type);
        if (javaType == null) throw new SecurityException("BATCH_REMOTE_PAYLOAD_TYPE_DENIED:" + type);
        return javaType;
    }
}
