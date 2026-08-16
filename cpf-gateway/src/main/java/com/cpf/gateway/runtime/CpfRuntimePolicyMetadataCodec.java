package com.cpf.gateway.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Runtime Policy metadata의 저장 형식을 단일 Versioned JSON 계약으로 관리합니다.
 *
 * <p>신규 Event는 {@code CPF-RUNTIME-POLICY-METADATA/V1:} Prefix와 JSON Object로 저장합니다.
 * 기존 배포본에서 생성된 line 기반 값은 읽기 전용 호환 경로로 엄격하게 해석하며, 손상된 값은
 * 빈 Metadata로 보정하지 않고 즉시 실패합니다.</p>
 */
public final class CpfRuntimePolicyMetadataCodec {
    static final String V1_PREFIX = "CPF-RUNTIME-POLICY-METADATA/V1:";
    private static final TypeReference<LinkedHashMap<String, String>> STRING_MAP =
            new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public CpfRuntimePolicyMetadataCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy();
    }

    public String encode(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return V1_PREFIX + "{}";
        }
        TreeMap<String, String> ordered = new TreeMap<>();
        metadata.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Runtime policy metadata key must not be blank");
            }
            if (value == null) {
                throw new IllegalArgumentException("Runtime policy metadata value must not be null: key=" + key);
            }
            ordered.put(key, value);
        });
        try {
            return V1_PREFIX + objectMapper.writeValueAsString(ordered);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Runtime policy metadata JSON encoding failed", exception);
        }
    }

    public Map<String, String> decode(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return Map.of();
        }
        if (storedValue.startsWith(V1_PREFIX)) {
            return decodeJson(storedValue.substring(V1_PREFIX.length()));
        }
        return decodeLegacy(storedValue);
    }

    private Map<String, String> decodeJson(String json) {
        try {
            LinkedHashMap<String, String> decoded = objectMapper.readValue(json, STRING_MAP);
            validate(decoded);
            return Map.copyOf(decoded);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Runtime policy metadata V1 JSON is invalid", exception);
        }
    }

    private static Map<String, String> decodeLegacy(String value) {
        LinkedHashMap<String, String> decoded = new LinkedHashMap<>();
        for (String line : value.split("\\R", -1)) {
            if (line.isBlank()) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new IllegalStateException("Legacy runtime policy metadata line is malformed");
            }
            String key = unescapeLegacy(line.substring(0, separator));
            String item = unescapeLegacy(line.substring(separator + 1));
            if (key.isBlank() || decoded.putIfAbsent(key, item) != null) {
                throw new IllegalStateException("Legacy runtime policy metadata has blank/duplicate key: " + key);
            }
        }
        validate(decoded);
        return Map.copyOf(decoded);
    }

    private static String unescapeLegacy(String value) {
        StringBuilder decoded = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '%') {
                decoded.append(current);
                continue;
            }
            if (index + 2 >= value.length()) {
                throw new IllegalStateException("Legacy runtime policy metadata has truncated escape");
            }
            String code = value.substring(index + 1, index + 3).toUpperCase(java.util.Locale.ROOT);
            switch (code) {
                case "25" -> decoded.append('%');
                case "0A" -> decoded.append('\n');
                case "3D" -> decoded.append('=');
                default -> throw new IllegalStateException(
                        "Legacy runtime policy metadata contains unsupported escape: %" + code);
            }
            index += 2;
        }
        return decoded.toString();
    }

    private static void validate(Map<String, String> decoded) {
        decoded.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalStateException("Runtime policy metadata contains a blank key");
            }
            if (value == null) {
                throw new IllegalStateException("Runtime policy metadata contains a null value: key=" + key);
            }
        });
    }
}
