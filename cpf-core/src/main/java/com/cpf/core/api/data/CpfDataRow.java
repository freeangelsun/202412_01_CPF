package com.cpf.core.api.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 동적 운영 조회 결과를 외부에 raw Map으로 노출하지 않기 위한 명명된 행 계약입니다.
 *
 * <p>내부 저장소는 순서가 보장되는 Map이지만 Public API와 OpenAPI 경계에서는
 * {@code CpfDataRow}라는 명시적 타입으로 노출됩니다. Jackson은 Map 파생 타입으로
 * 직렬화하므로 기존 JSON 필드 구조를 유지합니다.</p>
 */
public final class CpfDataRow extends CpfDataRowStorage {
    public CpfDataRow() {
        super();
    }

    /** Map 또는 다른 CpfDataRow를 방어 복사합니다. */
    public CpfDataRow(Object source) {
        super();
        copyFrom(source);
    }

    public static CpfDataRow copyOf(Object source) {
        return source instanceof CpfDataRow row ? new CpfDataRow(row) : new CpfDataRow(source);
    }

    public static List<CpfDataRow> copyRows(Object source) {
        if (!(source instanceof Collection<?> collection)) {
            return List.of();
        }
        return collection.stream().map(CpfDataRow::copyOf).toList();
    }

    public static CpfDataRow of(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return new CpfDataRow();
        }
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("CpfDataRow key/value 수는 짝수여야 합니다.");
        }
        CpfDataRow row = new CpfDataRow();
        for (int index = 0; index < keyValues.length; index += 2) {
            Object value = keyValues[index + 1];
            if (value != null) {
                row.put(String.valueOf(keyValues[index]), value);
            }
        }
        return row;
    }

    public CpfDataRow with(String key, Object value) {
        if (value != null) {
            put(key, value);
        }
        return this;
    }

    private void copyFrom(Object source) {
        if (source == null) {
            return;
        }
        if (!(source instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("CpfDataRow source는 Map이어야 합니다: " + source.getClass().getName());
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
    }
}

/** Public 타입 선언에 raw Map을 노출하지 않기 위한 package-private 저장 구현입니다. */
abstract class CpfDataRowStorage extends LinkedHashMap<String, Object> {
    CpfDataRowStorage() {
        super();
    }
}
