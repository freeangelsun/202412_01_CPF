package com.cpf.member.sampleitem.dto;

import com.cpf.member.common.contract.MemberRequest;
import com.cpf.core.api.base.CpfQuery;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.page.CpfSort;
import com.cpf.core.api.page.CpfSortDirection;
import java.util.Set;

/**
 * Member 조회 조건입니다.
 *
 * <p>정렬 컬럼은 whitelist로 제한해 SQL Injection을 차단합니다.</p>
 */
public record MemberSearchRequest(
        String keyword,
        String sortBy,
        String sortDirection,
        Integer page,
        Integer size) implements MemberRequest, CpfQuery {
    private static final Set<String> SORT_COLUMNS = Set.of("created_at", "updated_at", "sample_item_id", "item_name");

    public MemberSearchRequest normalized() {
        String normalizedSortBy = sortBy != null && SORT_COLUMNS.contains(sortBy) ? sortBy : "created_at";
        String normalizedDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return new MemberSearchRequest(
                keyword, normalizedSortBy, normalizedDirection, normalizedPage, normalizedSize);
    }

    /** CPF 표준 Page 요청으로 변환합니다. Repository/EDU가 별도 Paging DTO를 만들지 않습니다. */
    public CpfPageRequest pageRequest() {
        MemberSearchRequest n = normalized();
        return new CpfPageRequest(n.page(), n.size());
    }

    /** 정규화된 allow-list field와 방향을 CPF 공개 정렬 계약으로 변환합니다. */
    public CpfSort sort() {
        MemberSearchRequest n = normalized();
        return new CpfSort(n.sortBy(), CpfSortDirection.from(n.sortDirection()));
    }

    public int offset() {
        return Math.toIntExact(pageRequest().offset());
    }
}