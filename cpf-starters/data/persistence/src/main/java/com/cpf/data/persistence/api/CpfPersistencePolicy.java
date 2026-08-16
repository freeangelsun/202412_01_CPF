package com.cpf.data.persistence.api;

import com.cpf.data.persistence.api.page.CpfPageRequest;
import com.cpf.data.persistence.api.page.CpfSort;
import com.cpf.data.persistence.api.page.CpfSortPolicy;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** SQL injection과 과도한 조회를 막기 위한 공통 Persistence 경계 정책입니다. */
public final class CpfPersistencePolicy {
    public static final int DEFAULT_MAX_BULK_SIZE = 1000;
    private CpfPersistencePolicy() { }

    /** 정렬 검증은 공통 Paging 정책을 단일 정본으로 사용합니다. */
    public static List<CpfSort> requireAllowedSorts(List<CpfSort> requested, Collection<String> allowedFields) {
        return requireAllowedSorts(requested, allowedFields, CpfSortPolicy.DEFAULT_MAX_COLUMNS);
    }

    public static List<CpfSort> requireAllowedSorts(List<CpfSort> requested, Collection<String> allowedFields, int maxColumns) {
        return CpfSortPolicy.validate(requested, new LinkedHashSet<>(Objects.requireNonNull(allowedFields, "allowedFields")), maxColumns);
    }

    /** requireAllowedFilters 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfSearchSpec requireAllowedFilters(CpfSearchSpec spec, Collection<String> allowedFields) {
        CpfSearchSpec safe = spec == null ? CpfSearchSpec.empty() : spec;
        Set<String> allowed = new LinkedHashSet<>(Objects.requireNonNull(allowedFields, "allowedFields"));
        for (CpfFilterCriterion criterion : safe.criteria()) {
            if (!allowed.contains(criterion.field())) throw new IllegalArgumentException("허용되지 않은 검색 field: " + criterion.field());
        }
        return safe;
    }

    /** requireBoundedPage 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfPageRequest requireBoundedPage(CpfPageRequest request) {
        return Objects.requireNonNull(request, "pageRequest"); // CpfPageRequest constructor가 1..200을 강제합니다.
    }

    public static int requireBulkSize(int size, int max) {
        if (max < 1) throw new IllegalArgumentException("max bulk size는 1 이상이어야 합니다.");
        if (size < 0 || size > max) throw new IllegalArgumentException("bulk size는 0~" + max + " 범위여야 합니다.");
        return size;
    }
}
