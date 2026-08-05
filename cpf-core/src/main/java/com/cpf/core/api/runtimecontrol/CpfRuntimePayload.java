package com.cpf.core.api.runtimecontrol;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.TreeMap;

/**
 * Runtime 변경 Payload의 공개 Value Object입니다.
 *
 * <p>Public API에는 Jackson의 {@code JsonNode}나 구조가 불명확한 {@code Map<String,Object>}를
 * 노출하지 않습니다. 외부 HTTP JSON은 기존과 동일하게 JSON Object로 직렬화되며, 내부에는
 * field 순서가 고정된 canonical JSON 문자열만 보존합니다.</p>
 */
@JsonDeserialize(using = CpfRuntimePayload.PayloadDeserializer.class)
@JsonSerialize(using = CpfRuntimePayload.PayloadSerializer.class)
public final class CpfRuntimePayload {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EMPTY = "{}";
    private final String canonicalJson;

    private CpfRuntimePayload(String canonicalJson, boolean trustedCanonical) {
        this.canonicalJson = trustedCanonical ? canonicalJson : canonicalize(canonicalJson);
    }

    /** 빈 JSON Object Payload를 반환합니다. */
    public static CpfRuntimePayload empty() {
        return new CpfRuntimePayload(EMPTY, true);
    }

    /** JSON Object 문자열을 검증하고 canonical Payload로 변환합니다. */
    public static CpfRuntimePayload parse(String json) {
        if (json == null || json.isBlank()) return empty();
        return new CpfRuntimePayload(json, false);
    }

    /** Hash·DB 저장·전송에 사용하는 정규화 JSON입니다. */
    public String canonicalJson() {
        return canonicalJson;
    }

    public boolean isEmpty() {
        return EMPTY.equals(canonicalJson);
    }

    /** 최상위 Object에 field가 존재하는지 확인합니다. */
    public boolean contains(String fieldName) {
        return root().has(fieldName);
    }

    /** 최상위 Object field 이름을 canonical 순서의 불변 Set으로 반환합니다. */
    public Set<String> fieldNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        root().properties().forEach(entry -> names.add(entry.getKey()));
        return java.util.Collections.unmodifiableSet(names);
    }

    /** 문자열 타입을 강제하며 누락·null이면 fallback을 반환합니다. */
    public String textStrict(String fieldName, String fallback) {
        JsonNode value = root().get(fieldName);
        if (value == null || value.isNull()) return fallback;
        if (!value.isTextual()) {
            throw new IllegalArgumentException(
                    "Runtime payload 문자열 field가 아닙니다: " + fieldName);
        }
        return value.textValue();
    }

    /** JSON boolean 타입을 강제하며 누락·null이면 fallback을 반환합니다. */
    public boolean booleanStrict(String fieldName, boolean fallback) {
        JsonNode value = root().get(fieldName);
        if (value == null || value.isNull()) return fallback;
        if (!value.isBoolean()) {
            throw new IllegalArgumentException(
                    "Runtime payload boolean field가 아닙니다: " + fieldName);
        }
        return value.booleanValue();
    }

    /** 문자열 field를 조회하며 누락·null이면 fallback을 반환합니다. */
    public String text(String fieldName, String fallback) {
        JsonNode value = root().get(fieldName);
        return value == null || value.isNull() ? fallback : value.asText();
    }

    /** 정수 field를 조회하며 누락·null이면 fallback을 반환합니다. */
    public long longValue(String fieldName, long fallback) {
        JsonNode value = root().get(fieldName);
        if (value == null || value.isNull()) return fallback;
        if (value.isIntegralNumber()) return value.longValue();
        try {
            return Long.parseLong(value.asText());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Runtime payload 정수 field가 올바르지 않습니다: " + fieldName, ex);
        }
    }

    /** boolean field를 조회하며 누락·null이면 fallback을 반환합니다. */
    public boolean booleanValue(String fieldName, boolean fallback) {
        JsonNode value = root().get(fieldName);
        if (value == null || value.isNull()) return fallback;
        if (value.isBoolean()) return value.booleanValue();
        String text = value.asText();
        if ("true".equalsIgnoreCase(text)) return true;
        if ("false".equalsIgnoreCase(text)) return false;
        throw new IllegalArgumentException("Runtime payload boolean field가 올바르지 않습니다: " + fieldName);
    }

    /** 문자열 배열 field를 불변 List로 조회합니다. */
    public List<String> stringList(String fieldName) {
        JsonNode value = root().get(fieldName);
        if (value == null || value.isNull()) return List.of();
        if (!value.isArray()) {
            throw new IllegalArgumentException("Runtime payload 문자열 배열 field가 아닙니다: " + fieldName);
        }
        List<String> result = new ArrayList<>();
        value.forEach(item -> {
            if (!item.isNull()) result.add(item.asText());
        });
        return List.copyOf(result);
    }

    /** Object 배열 field를 동일한 Value Object List로 조회합니다. */
    public List<CpfRuntimePayload> objectList(String fieldName) {
        JsonNode value = root().get(fieldName);
        if (value == null || value.isNull()) return List.of();
        if (!value.isArray()) {
            throw new IllegalArgumentException("Runtime payload Object 배열 field가 아닙니다: " + fieldName);
        }
        List<CpfRuntimePayload> result = new ArrayList<>();
        value.forEach(item -> {
            if (!item.isObject()) {
                throw new IllegalArgumentException(
                        "Runtime payload Object 배열 항목이 Object가 아닙니다: " + fieldName);
            }
            result.add(fromNode(item));
        });
        return List.copyOf(result);
    }

    /** Object field의 각 child Object를 key 순서가 고정된 불변 Map으로 조회합니다. */
    public Map<String, CpfRuntimePayload> objectMap(String fieldName) {
        JsonNode value = root().get(fieldName);
        if (value == null || value.isNull()) return Map.of();
        if (!value.isObject()) {
            throw new IllegalArgumentException("Runtime payload Object field가 아닙니다: " + fieldName);
        }
        Map<String, CpfRuntimePayload> result = new LinkedHashMap<>();
        value.properties().forEach(entry -> {
            if (!entry.getValue().isObject()) {
                throw new IllegalArgumentException(
                        "Runtime payload child가 Object가 아닙니다: " + fieldName + "." + entry.getKey());
            }
            result.put(entry.getKey(), fromNode(entry.getValue()));
        });
        return Map.copyOf(result);
    }

    @Override
    public String toString() {
        return canonicalJson;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CpfRuntimePayload payload && canonicalJson.equals(payload.canonicalJson);
    }

    @Override
    public int hashCode() {
        return canonicalJson.hashCode();
    }

    private static String canonicalize(String json) {
        try {
            JsonNode parsed = MAPPER.readTree(json);
            if (parsed == null || parsed.isNull()) return EMPTY;
            if (!parsed.isObject()) throw new IllegalArgumentException("Runtime payload는 JSON Object여야 합니다.");
            return MAPPER.writeValueAsString(sort(parsed));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Runtime payload JSON을 해석할 수 없습니다.", ex);
        }
    }

    private JsonNode root() {
        try {
            return MAPPER.readTree(canonicalJson);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("검증된 Runtime payload를 다시 해석할 수 없습니다.", ex);
        }
    }

    private static CpfRuntimePayload fromNode(JsonNode node) {
        try {
            return new CpfRuntimePayload(MAPPER.writeValueAsString(sort(node)), true);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Runtime payload child Object를 변환할 수 없습니다.", ex);
        }
    }

    private static JsonNode sort(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) return node;
        if (node.isArray()) {
            var array = JsonNodeFactory.instance.arrayNode();
            node.forEach(item -> array.add(sort(item)));
            return array;
        }
        TreeMap<String, JsonNode> ordered = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
        fields.forEachRemaining(entry -> ordered.put(entry.getKey(), sort(entry.getValue())));
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        ordered.forEach(object::set);
        return object;
    }

    /** HTTP JSON Object 형태를 유지하는 전용 Deserializer입니다. */
    public static final class PayloadDeserializer extends JsonDeserializer<CpfRuntimePayload> {
        @Override
        public CpfRuntimePayload deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode value = parser.readValueAsTree();
            if (value == null || value.isNull()) return empty();
            if (!value.isObject()) throw context.weirdStringException(value.toString(), CpfRuntimePayload.class,
                    "Runtime payload는 JSON Object여야 합니다.");
            return parse(MAPPER.writeValueAsString(value));
        }
    }

    /** HTTP 응답·Agent Delivery에서 JSON 문자열이 아니라 JSON Object를 출력합니다. */
    public static final class PayloadSerializer extends JsonSerializer<CpfRuntimePayload> {
        @Override
        public void serialize(CpfRuntimePayload value, JsonGenerator generator, SerializerProvider serializers)
                throws IOException {
            generator.writeTree(MAPPER.readTree(value == null ? EMPTY : value.canonicalJson));
        }
    }
}
