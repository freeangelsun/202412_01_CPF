package com.cpf.core.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** null-safe immutable List와 chunk 처리를 제공하는 CPF Collection API입니다. */
public final class CpfLists {
    private CpfLists() {}
    public static <T> List<T> emptyIfNull(Collection<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
    public static <T> List<T> distinct(Collection<T> values) {
        return values == null ? List.of() : List.copyOf(new LinkedHashSet<>(values));
    }
    public static <T> List<List<T>> partition(List<T> values, int chunkSize) {
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize는 1 이상이어야 합니다.");
        List<T> safe = values == null ? List.of() : values;
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < safe.size(); i += chunkSize) {
            result.add(List.copyOf(safe.subList(i, Math.min(i + chunkSize, safe.size()))));
        }
        return List.copyOf(result);
    }
    public static <T> T firstOrNull(List<T> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }
    public static <T> T lastOrNull(List<T> values) { return values == null || values.isEmpty() ? null : values.getLast(); }
    public static <T> List<T> concat(Collection<? extends T> first, Collection<? extends T> second) {
        java.util.ArrayList<T> result = new java.util.ArrayList<>();
        if (first != null) result.addAll(first); if (second != null) result.addAll(second);
        return List.copyOf(result);
    }
    public static <T> List<T> page(List<T> values, int page, int size) {
        if (page < 0 || size < 1) throw new IllegalArgumentException("page는 0 이상, size는 1 이상이어야 합니다.");
        List<T> safe = values == null ? List.of() : values; int from = Math.min(page * size, safe.size());
        return List.copyOf(safe.subList(from, Math.min(from + size, safe.size())));
    }
}
