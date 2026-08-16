package com.cpf.data.persistence.api;

import java.util.List;
import java.util.Objects;

/** Repository allow-list와 결합하여 사용하는 단일 검색 조건입니다. */
public record CpfFilterCriterion(String field, CpfFilterOperator operator, List<Object> values) {
    public CpfFilterCriterion {
        field = Objects.requireNonNull(field, "field").trim();
        if (field.isEmpty()) throw new IllegalArgumentException("검색 field는 비어 있을 수 없습니다.");
        operator = Objects.requireNonNull(operator, "operator");
        values = values == null ? List.of() : List.copyOf(values);
        if ((operator == CpfFilterOperator.IS_NULL || operator == CpfFilterOperator.IS_NOT_NULL) && !values.isEmpty())
            throw new IllegalArgumentException("NULL 연산자는 값을 가질 수 없습니다.");
        if (operator == CpfFilterOperator.IN && values.isEmpty()) throw new IllegalArgumentException("IN 조건은 값이 필요합니다.");
        if (operator != CpfFilterOperator.IS_NULL && operator != CpfFilterOperator.IS_NOT_NULL && operator != CpfFilterOperator.IN && values.size() != 1)
            throw new IllegalArgumentException("비교 조건은 정확히 한 값을 가져야 합니다.");
    }
    /** eq 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfFilterCriterion eq(String field, Object value) { return new CpfFilterCriterion(field, CpfFilterOperator.EQ, List.of(value)); }
}
