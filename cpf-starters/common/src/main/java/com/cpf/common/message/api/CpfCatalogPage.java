package com.cpf.common.message.api;

import java.util.List;

/** Common 관리 API의 vendor-neutral paging 결과입니다. */
public record CpfCatalogPage<T>(List<T> content, int page, int size, long totalElements) {
    public CpfCatalogPage {
        content = content == null ? List.of() : List.copyOf(content);
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 500) throw new IllegalArgumentException("size must be between 1 and 500");
        if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
    }
    /** totalPages 작업을 CPF 표준 계약에 따라 수행한다. */
    public int totalPages() {
        return totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
    }
}
