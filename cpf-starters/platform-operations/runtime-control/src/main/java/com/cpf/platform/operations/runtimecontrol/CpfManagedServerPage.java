package com.cpf.platform.operations.runtimecontrol;

import java.util.List;

/** Stable, server-side paged view of the Central Managed Server registry. */
public record CpfManagedServerPage(
        List<CpfManagedServerSnapshot> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public CpfManagedServerPage {
        items = items == null ? List.of() : List.copyOf(items);
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1) throw new IllegalArgumentException("size must be > 0");
        if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
        int expectedPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1L) / size);
        if (totalPages != expectedPages) throw new IllegalArgumentException("totalPages mismatch");
        if (hasNext != ((long) (page + 1) * size < totalElements)) throw new IllegalArgumentException("hasNext mismatch");
    }

    public static CpfManagedServerPage of(List<CpfManagedServerSnapshot> items, int page, int size, long totalElements) {
        int pages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1L) / size);
        return new CpfManagedServerPage(items, page, size, totalElements, pages, (long) (page + 1) * size < totalElements);
    }
}
