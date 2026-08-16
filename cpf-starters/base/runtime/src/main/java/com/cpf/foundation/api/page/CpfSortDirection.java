package com.cpf.foundation.api.page;

import java.util.Locale;

/** CPF 공개 정렬 방향입니다. */
public enum CpfSortDirection {
    ASC, DESC;
    public static CpfSortDirection from(String value) {
        if (value == null || value.isBlank()) return ASC;
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "ASC", "ASCENDING" -> ASC;
            case "DESC", "DESCENDING" -> DESC;
            default -> throw new IllegalArgumentException("sort direction must be ASC or DESC");
        };
    }
}
