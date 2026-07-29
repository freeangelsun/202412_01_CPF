package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Runtime Control Plane request/payload의 안정적인 SHA-256 fingerprint를 생성합니다. */
public final class CpfRuntimeCanonicalHash {
    private CpfRuntimeCanonicalHash() {}

    public static String sha256(Object value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(canonical(value).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Runtime canonical hash 생성에 실패했습니다.", ex);
        }
    }

    static String canonical(Object value) {
        if (value == null) return "null";
        if (value instanceof CpfRuntimePayload payload) return payload.canonicalJson();
        if (value instanceof JsonNode node) return canonicalJsonNode(node);
        if (value instanceof String s) return '"' + escape(s) + '"';
        if (value instanceof Number || value instanceof Boolean) return String.valueOf(value);
        if (value instanceof Map<?, ?> map) {
            List<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
            entries.sort(Comparator.comparing(e -> String.valueOf(e.getKey())));
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : entries) {
                if (!first) out.append(',');
                first = false;
                out.append('"').append(escape(String.valueOf(entry.getKey()))).append('"')
                        .append(':').append(canonical(entry.getValue()));
            }
            return out.append('}').toString();
        }
        if (value instanceof Collection<?> collection) {
            StringBuilder out = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) out.append(',');
                first = false;
                out.append(canonical(item));
            }
            return out.append(']').toString();
        }
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            List<Object> items = new ArrayList<>(length);
            for (int i = 0; i < length; i++) items.add(java.lang.reflect.Array.get(value, i));
            return canonical(items);
        }
        if (value.getClass().isRecord()) {
            java.util.LinkedHashMap<String, Object> record = new java.util.LinkedHashMap<>();
            for (java.lang.reflect.RecordComponent component : value.getClass().getRecordComponents()) {
                try {
                    record.put(component.getName(), component.getAccessor().invoke(value));
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException("Record canonicalization 실패: " + value.getClass().getName(), ex);
                }
            }
            return canonical(record);
        }
        return canonical(String.valueOf(value));
    }

    private static String canonicalJsonNode(JsonNode node) {
        if (node == null || node.isNull()) return "null";
        if (node.isTextual()) return '"' + escape(node.textValue()) + '"';
        if (node.isNumber() || node.isBoolean()) return node.asText();
        if (node.isArray()) {
            StringBuilder out = new StringBuilder("[");
            boolean first = true;
            for (JsonNode item : node) {
                if (!first) out.append(',');
                first = false;
                out.append(canonicalJsonNode(item));
            }
            return out.append(']').toString();
        }
        java.util.TreeMap<String, JsonNode> fields = new java.util.TreeMap<>();
        node.fields().forEachRemaining(entry -> fields.put(entry.getKey(), entry.getValue()));
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (var entry : fields.entrySet()) {
            if (!first) out.append(',');
            first = false;
            out.append('"').append(escape(entry.getKey())).append('"').append(':')
                    .append(canonicalJsonNode(entry.getValue()));
        }
        return out.append('}').toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
