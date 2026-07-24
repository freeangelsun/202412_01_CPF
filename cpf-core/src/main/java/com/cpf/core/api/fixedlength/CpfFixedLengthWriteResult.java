package com.cpf.core.api.fixedlength;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * byte 기준 고정길이 전문 생성 결과입니다.
 */
public final class CpfFixedLengthWriteResult {
    private final String message;
    private final byte[] bytes;
    private final Map<String, String> maskedFields;
    private final Map<String, List<Map<String, String>>> maskedGroups;

    public CpfFixedLengthWriteResult(
            String message,
            byte[] bytes,
            Map<String, String> maskedFields,
            Map<String, List<Map<String, String>>> maskedGroups) {
        this.message = message == null ? "" : message;
        this.bytes = bytes == null ? new byte[0] : bytes.clone();
        this.maskedFields = immutableMap(maskedFields);
        this.maskedGroups = immutableNestedMap(maskedGroups);
    }

    public String message() {
        return message;
    }

    public byte[] bytes() {
        return bytes.clone();
    }

    public int byteLength() {
        return bytes.length;
    }

    public Map<String, String> maskedFields() {
        return maskedFields;
    }

    public Map<String, List<Map<String, String>>> maskedGroups() {
        return maskedGroups;
    }

    private static <V> Map<String, V> immutableMap(Map<String, V> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Map.copyOf(new LinkedHashMap<>(source));
    }

    private static <V> Map<String, List<Map<String, V>>> immutableNestedMap(
            Map<String, List<Map<String, V>>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, List<Map<String, V>>> copy = new LinkedHashMap<>();
        source.forEach((name, rows) -> copy.put(
                name,
                rows == null
                        ? List.of()
                        : rows.stream().map(CpfFixedLengthWriteResult::immutableMap).toList()));
        return Map.copyOf(copy);
    }
}
