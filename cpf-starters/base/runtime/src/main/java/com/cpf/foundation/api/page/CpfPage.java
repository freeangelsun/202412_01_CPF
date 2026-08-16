package com.cpf.foundation.api.page;

import java.util.List;

/** Framework 공통 Paging 결과 Value입니다. */
public record CpfPage<T>(List<T> content, long totalElements, int page, int size) {
    public CpfPage {
        content = List.copyOf(content);
        if (totalElements < 0 || page < 0 || size <= 0) throw new IllegalArgumentException("invalid page metadata");
    }
    public int totalPages() { return (int) ((totalElements + size - 1) / size); }
    public boolean hasNext() { return page + 1 < totalPages(); }
    /** hasPrevious 작업을 CPF 표준 계약에 따라 수행한다. */
    public boolean hasPrevious() { return page > 0; }
}
