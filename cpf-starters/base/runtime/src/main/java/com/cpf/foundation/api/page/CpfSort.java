package com.cpf.foundation.api.page;

/** allow-list 검증된 field를 운반하는 CPF 공개 정렬 value입니다. */
public record CpfSort(String field, CpfSortDirection direction) {
    public CpfSort {
        if (field == null || field.isBlank() || !field.trim().matches("[A-Za-z][A-Za-z0-9_]{0,63}")) {
            throw new IllegalArgumentException("sort field is invalid");
        }
        field = field.trim();
        direction = direction == null ? CpfSortDirection.ASC : direction;
    }
}
