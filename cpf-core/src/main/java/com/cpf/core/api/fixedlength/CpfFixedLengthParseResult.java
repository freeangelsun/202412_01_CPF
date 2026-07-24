package com.cpf.core.api.fixedlength;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * byte 기준 고정길이 전문 parse 결과입니다.
 */
public record CpfFixedLengthParseResult(
        String rawMessage,
        int byteLength,
        Map<String, String> fields,
        Map<String, Object> typedFields,
        Map<String, String> maskedFields,
        Map<String, List<Map<String, String>>> groups,
        Map<String, List<Map<String, Object>>> typedGroups,
        Map<String, List<Map<String, String>>> maskedGroups,
        List<CpfFixedLengthError> errors) {

    public CpfFixedLengthParseResult {
        rawMessage = rawMessage == null ? "" : rawMessage;
        if (byteLength < 0) {
            throw new IllegalArgumentException("전문 byte 길이는 0 이상이어야 합니다.");
        }
        fields = immutableMap(fields);
        typedFields = immutableMap(typedFields);
        maskedFields = immutableMap(maskedFields);
        groups = immutableNestedMap(groups);
        typedGroups = immutableNestedMap(typedGroups);
        maskedGroups = immutableNestedMap(maskedGroups);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public boolean valid() {
        return errors.isEmpty();
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
                        : rows.stream().map(CpfFixedLengthParseResult::immutableMap).toList()));
        return Map.copyOf(copy);
    }
}
