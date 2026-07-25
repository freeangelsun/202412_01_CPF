package com.cpf.core.api.page;

import java.util.Objects;

/**
 * CPF 공개 정렬 계약입니다.
 *
 * <p>field는 Repository가 제공하는 allow-list와 반드시 대조해서 사용해야 하며
 * 요청 문자열을 SQL ORDER BY에 직접 이어 붙이면 안 됩니다.</p>
 */
public record CpfSort(String field, CpfSortDirection direction) {
    public CpfSort {
        field = Objects.requireNonNull(field, "field").trim();
        if (field.isEmpty()) throw new IllegalArgumentException("정렬 field는 비어 있을 수 없습니다.");
        direction = direction == null ? CpfSortDirection.ASC : direction;
    }

    public static CpfSort asc(String field) { return new CpfSort(field, CpfSortDirection.ASC); }
    public static CpfSort desc(String field) { return new CpfSort(field, CpfSortDirection.DESC); }
}
