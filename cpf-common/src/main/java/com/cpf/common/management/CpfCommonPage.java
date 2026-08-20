package com.cpf.common.management;

import java.util.List;

/** Vendor-neutral Common 관리 Paging 결과입니다. */
public record CpfCommonPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public CpfCommonPage {
        content = List.copyOf(content);
        if (page < 0 || size <= 0 || totalElements < 0) throw new IllegalArgumentException("invalid page metadata");
    }
}
