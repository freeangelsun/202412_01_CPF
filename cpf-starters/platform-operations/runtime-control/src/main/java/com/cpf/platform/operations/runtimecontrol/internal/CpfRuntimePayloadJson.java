package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Runtime Control 내부 구현에서만 사용하는 Payload JSON 접근기입니다. */
public final class CpfRuntimePayloadJson {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CpfRuntimePayloadJson() {}

    public static JsonNode read(CpfRuntimePayload payload) {
        try {
            return MAPPER.readTree(payload == null ? "{}" : payload.canonicalJson());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("검증된 Runtime payload를 다시 읽을 수 없습니다.", ex);
        }
    }

    public static JsonNode field(CpfRuntimePayload payload, String field) {
        JsonNode value = read(payload).get(field);
        return value == null ? MAPPER.getNodeFactory().missingNode() : value;
    }

    public static boolean contains(CpfRuntimePayload payload, String field) {
        return read(payload).has(field);
    }

    public static CpfRuntimePayload without(CpfRuntimePayload payload, String field) {
        JsonNode value = read(payload);
        if (!(value instanceof ObjectNode object)) return CpfRuntimePayload.empty();
        ObjectNode copy = object.deepCopy();
        copy.remove(field);
        return CpfRuntimePayload.parse(copy.toString());
    }

    public static CpfRuntimePayload objectField(CpfRuntimePayload payload, String field) {
        JsonNode value = field(payload, field);
        if (value.isMissingNode() || value.isNull()) return null;
        if (!value.isObject()) throw new IllegalArgumentException(field + "는 JSON Object여야 합니다.");
        return CpfRuntimePayload.parse(value.toString());
    }
}
