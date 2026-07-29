package com.cpf.core.api.tabular;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Template Registry가 관리하는 versioned tabular schema입니다. */
public record CpfTabularSchema(String templateCode, int version, List<CpfTabularColumn> columns,
                               int maxRows, int maxCellLength) {
    public CpfTabularSchema {
        templateCode = Objects.requireNonNull(templateCode, "templateCode는 필수입니다.").trim();
        if (!templateCode.matches("[A-Z][A-Z0-9_.-]{1,79}")) {
            throw new IllegalArgumentException("templateCode 형식이 올바르지 않습니다.");
        }
        columns = columns == null ? List.of() : List.copyOf(columns);
        if (version <= 0 || columns.isEmpty() || maxRows <= 0 || maxCellLength <= 0) {
            throw new IllegalArgumentException("Tabular schema version/columns/limit은 필수입니다.");
        }
        if (columns.size() > 2048) throw new IllegalArgumentException("Tabular Column 수가 허용 범위를 초과했습니다.");
        var names = new HashSet<String>();
        var labels = new HashSet<String>();
        for (CpfTabularColumn column : columns) {
            if (column == null) throw new IllegalArgumentException("Tabular Column은 null일 수 없습니다.");
            if (!names.add(column.name())) throw new IllegalArgumentException("중복 Column: " + column.name());
            if (!labels.add(column.label())) throw new IllegalArgumentException("중복 Column label: " + column.label());
        }
    }
}
