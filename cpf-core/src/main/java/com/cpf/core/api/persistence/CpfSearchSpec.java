package com.cpf.core.api.persistence;

import java.util.List;

/** 공통 검색 조건 묶음입니다. 조건 결합은 기본 AND이며 복잡 Query는 Domain native query를 사용합니다. */
public record CpfSearchSpec(List<CpfFilterCriterion> criteria) {
    public static final int MAX_CRITERIA = 32;
    public CpfSearchSpec {
        criteria = criteria == null ? List.of() : List.copyOf(criteria);
        if (criteria.size() > MAX_CRITERIA) throw new IllegalArgumentException("검색 조건은 최대 " + MAX_CRITERIA + "개입니다.");
    }
    public static CpfSearchSpec empty() { return new CpfSearchSpec(List.of()); }
}
