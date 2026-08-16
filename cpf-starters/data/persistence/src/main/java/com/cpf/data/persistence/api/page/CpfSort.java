package com.cpf.data.persistence.api.page;

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

    /** 오름차순 정렬 계약을 생성합니다.
     * @param field Repository allow-list와 대조할 정렬 필드
     * @return ASC 정렬 값
     * @throws NullPointerException field가 null인 경우
     * @throws IllegalArgumentException field가 blank인 경우
     */
    public static CpfSort asc(String field) { return new CpfSort(field, CpfSortDirection.ASC); }
    /** 내림차순 정렬 계약을 생성합니다.
     * @param field Repository allow-list와 대조할 정렬 필드
     * @return DESC 정렬 값
     * @throws NullPointerException field가 null인 경우
     * @throws IllegalArgumentException field가 blank인 경우
     */
    public static CpfSort desc(String field) { return new CpfSort(field, CpfSortDirection.DESC); }
}
