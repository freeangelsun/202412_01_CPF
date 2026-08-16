package com.cpf.file.tabular.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 순서를 보존하는 행 데이터입니다. */
public record CpfTabularRow(long rowNumber, Map<String, String> values) {
    public CpfTabularRow {
        if (rowNumber <= 0) throw new IllegalArgumentException("rowNumber는 1 이상이어야 합니다.");
        LinkedHashMap<String, String> ordered = new LinkedHashMap<>();
        if (values != null) {
            values.forEach((key, value) -> {
                if (key == null || key.isBlank()) throw new IllegalArgumentException("행 Column name은 필수입니다.");
                ordered.put(key, value == null ? "" : value);
            });
        }
        values = Collections.unmodifiableMap(ordered);
    }
}
