package com.cpf.core.api.page;

import java.util.List;

/** CPF 공통 서버 Paging 응답 계약. */
public record CpfPage<T>(List<T> content, long totalElements, int page, int size) {
    public CpfPage {
        content = content == null ? List.of() : List.copyOf(content);
        if (totalElements < 0) throw new IllegalArgumentException("totalElements는 0 이상이어야 합니다.");
        if (page < 0) throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        if (size < 1) throw new IllegalArgumentException("size는 1 이상이어야 합니다.");
    }

    public long totalPages() {
        return totalElements == 0 ? 0 : (totalElements + size - 1) / size;
    }

    public boolean hasNext() {
        return ((long) page + 1L) * size < totalElements;
    }
}
