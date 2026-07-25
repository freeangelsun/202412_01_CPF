package com.cpf.core.api.page;

import java.util.List;

/** CPF Offset Page 응답 표준입니다. */
public record CpfPage<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        long totalPages,
        boolean hasNext,
        boolean hasPrevious) {

    public CpfPage {
        items = items == null ? List.of() : List.copyOf(items);
        if (page < 0 || size <= 0 || totalElements < 0 || totalPages < 0) {
            throw new IllegalArgumentException("Page metadata가 올바르지 않습니다.");
        }
    }

    public static <T> CpfPage<T> of(List<T> items, CpfPageRequest request, long totalElements) {
        long pages = totalElements == 0 ? 0 : ((totalElements - 1) / request.size()) + 1;
        return new CpfPage<>(
                items, request.page(), request.size(), totalElements, pages,
                request.page() + 1 < pages, request.page() > 0);
    }
}
