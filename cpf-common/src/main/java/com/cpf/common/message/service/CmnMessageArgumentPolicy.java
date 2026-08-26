package com.cpf.common.message.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** CMN_MESSAGE parameter schema/escaping/masking을 fail-closed로 적용합니다. */
@Component
final class CmnMessageArgumentPolicy {
    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i).*(password|passwd|secret|token|credential|authorization|cookie|rrn|ssn|card|account|pin).*"
    );
    private final ObjectMapper objectMapper;

    CmnMessageArgumentPolicy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Prepared prepare(Map<String, Object> arguments, String schemaJson, boolean escapeHtml, boolean maskArguments) {
        Map<String, Object> source = arguments == null ? Map.of() : arguments;
        JsonNode schema = null;
        if (schemaJson != null && !schemaJson.isBlank()) {
            try { schema = objectMapper.readTree(schemaJson); }
            catch (Exception ex) { return new Prepared(Map.of(), false, "INVALID_PARAMETER_SCHEMA"); }
            if (schema == null || !schema.isObject()) return new Prepared(Map.of(), false, "INVALID_PARAMETER_SCHEMA");
        }

        if (schema != null && schema.path("required").isArray()) {
            for (JsonNode item : schema.path("required")) {
                String key = item.asText();
                if (key.isBlank() || !source.containsKey(key) || source.get(key) == null) {
                    return new Prepared(Map.of(), false, "MISSING_REQUIRED_PARAMETER");
                }
            }
        }

        Map<String, Object> safe = new LinkedHashMap<>();
        JsonNode properties = schema == null ? null : schema.path("properties");
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) continue;
            JsonNode property = properties != null && properties.isObject() ? properties.path(key) : null;
            boolean schemaSensitive = property != null && property.path("sensitive").asBoolean(false);
            int maxLength = property != null && property.has("maxLength") ? property.path("maxLength").asInt(512) : 512;
            maxLength = Math.max(1, Math.min(maxLength, 4096));

            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            value = value.replace('\r', ' ').replace('\n', ' ');
            if (value.length() > maxLength) value = value.substring(0, maxLength);

            if (SENSITIVE_KEY.matcher(key).matches() || (maskArguments && schemaSensitive)) value = "***";
            if (escapeHtml) value = escape(value);
            safe.put(key, value);
        }
        return new Prepared(Map.copyOf(safe), true, "OK");
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    record Prepared(Map<String, Object> arguments, boolean valid, String reason) { }
}
