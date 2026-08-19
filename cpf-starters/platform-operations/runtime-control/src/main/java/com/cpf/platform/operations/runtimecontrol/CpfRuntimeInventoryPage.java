package com.cpf.platform.operations.runtimecontrol;

import java.util.List;

/** Stable, server-side paged projection of Runtime Instance and Capability inventory. */
public record CpfRuntimeInventoryPage(
        List<CpfRuntimeInventorySnapshot> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public CpfRuntimeInventoryPage {
        items = items == null ? List.of() : List.copyOf(items);
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1) throw new IllegalArgumentException("size must be > 0");
        if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
        int expectedPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1L) / size);
        if (totalPages != expectedPages) throw new IllegalArgumentException("totalPages mismatch");
        if (hasNext != ((long) (page + 1) * size < totalElements)) throw new IllegalArgumentException("hasNext mismatch");
    }

    public static CpfRuntimeInventoryPage of(List<CpfRuntimeInventorySnapshot> items, int page, int size, long totalElements) {
        int pages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1L) / size);
        return new CpfRuntimeInventoryPage(items, page, size, totalElements, pages, (long) (page + 1) * size < totalElements);
    }
}
