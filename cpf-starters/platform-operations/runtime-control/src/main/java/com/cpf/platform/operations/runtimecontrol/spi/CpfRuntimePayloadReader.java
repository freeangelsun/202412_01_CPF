package com.cpf.platform.operations.runtimecontrol.spi;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.Map;

/** Capability-owned Runtime Applier가 공유하는 Public SPI Payload 접근기입니다. */
public final class CpfRuntimePayloadReader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CpfRuntimePayloadReader() {}

    public static JsonNode read(CpfRuntimePayload payload) {
        try {
            return MAPPER.readTree(payload == null ? "{}" : payload.canonicalJson());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("검증된 Runtime payload를 다시 읽을 수 없습니다.", ex);
        }
    }

    /** field 작업을 CPF 표준 계약에 따라 수행한다. */
    public static JsonNode field(CpfRuntimePayload payload, String field) {
        JsonNode value = read(payload).get(field);
        return value == null ? MAPPER.getNodeFactory().missingNode() : value;
    }

    public static boolean contains(CpfRuntimePayload payload, String field) {
        return read(payload).has(field);
    }

    /**
     * Owner Starter Runtime applier가 JSON library type을 전파하지 않고 값을 읽도록 합니다.
     *
     * <p>{@link CpfRuntimePayload} 공개 계약은 canonical JSON만 노출하고, 동적 JSON 변환은
     * 이 internal adapter 안으로 제한합니다.</p>
     */
    public static Object value(CpfRuntimePayload payload, String field) {
        JsonNode value = field(payload, field);
        return value.isMissingNode() || value.isNull() ? null : MAPPER.convertValue(value, Object.class);
    }

    public static Object valueOrDefault(CpfRuntimePayload payload, String field, Object defaultValue) {
        Object value = value(payload, field);
        return value == null ? defaultValue : value;
    }

    /** asMap 작업을 CPF 표준 계약에 따라 수행한다. */
    public static Map<String, Object> asMap(CpfRuntimePayload payload) {
        Map<String, Object> value = MAPPER.convertValue(
                read(payload),
                new TypeReference<Map<String, Object>>() {});
        return Collections.unmodifiableMap(value);
    }

    /** without 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfRuntimePayload without(CpfRuntimePayload payload, String field) {
        JsonNode value = read(payload);
        if (!(value instanceof ObjectNode object)) return CpfRuntimePayload.empty();
        ObjectNode copy = object.deepCopy();
        copy.remove(field);
        return CpfRuntimePayload.parse(copy.toString());
    }

    /** objectField 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfRuntimePayload objectField(CpfRuntimePayload payload, String field) {
        JsonNode value = field(payload, field);
        if (value.isMissingNode() || value.isNull()) return null;
        if (!value.isObject()) throw new IllegalArgumentException(field + "는 JSON Object여야 합니다.");
        return CpfRuntimePayload.parse(value.toString());
    }
}
