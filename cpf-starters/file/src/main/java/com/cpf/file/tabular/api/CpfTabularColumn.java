package com.cpf.file.tabular.api;

import java.util.Objects;

/** Upload/Download Template의 버전된 Column 계약입니다. */
public record CpfTabularColumn(String name, String label, Type type, boolean required,
                               int maxLength, boolean sensitive) {
    public enum Type { STRING, INTEGER, DECIMAL, BOOLEAN, DATE, DATETIME }
    public CpfTabularColumn {
        name = Objects.requireNonNull(name, "name은 필수입니다.").trim();
        if (name.isEmpty() || !name.matches("[A-Za-z][A-Za-z0-9_.-]{0,127}")) {
            throw new IllegalArgumentException("Column name 형식이 올바르지 않습니다.");
        }
        label = label == null || label.isBlank() ? name : label.trim();
        type = type == null ? Type.STRING : type;
        if (label.length() > 200) throw new IllegalArgumentException("Column label 허용 길이를 초과했습니다.");
        if (maxLength < 0) throw new IllegalArgumentException("maxLength는 음수일 수 없습니다.");
    }
}
