package com.cpf.account.reference.dto;

import java.util.Set;

/**
 * 생성기 기준 조회 기능의 검색 조건입니다.
 *
 * <p>정렬 컬럼은 허용 목록으로 제한하여 SQL 삽입 공격을 차단합니다.</p>
 */
public record AccountReferenceSearchRequest(
        String keyword,
        String sortBy,
        String sortDirection,
        Integer page,
        Integer size) {
    private static final Set<String> SORT_COLUMNS = Set.of("created_at", "updated_at", "acc_id");

    public AccountReferenceSearchRequest normalized() {
        String normalizedSortBy = sortBy != null && SORT_COLUMNS.contains(sortBy) ? sortBy : "created_at";
        String normalizedDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return new AccountReferenceSearchRequest(
                keyword, normalizedSortBy, normalizedDirection, normalizedPage, normalizedSize);
    }

    public int offset() {
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return normalizedPage * normalizedSize;
    }
}
