package com.cpf.core.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** null-safe immutable List와 chunk 처리를 제공하는 CPF Collection API입니다. */
public final class CpfLists {
    private CpfLists() {}
    /** null/empty Collection을 불변 빈 List로 정규화합니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @return null이 아닌 결과 목록
     */
    public static <T> List<T> emptyIfNull(Collection<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
    /** 입력 순서를 유지하며 중복을 제거한 불변 List를 만듭니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @return null이 아닌 결과 목록
     */
    public static <T> List<T> distinct(Collection<T> values) {
        return values == null ? List.of() : List.copyOf(new LinkedHashSet<>(values));
    }
    /** 목록을 지정 크기의 불변 chunk 목록으로 분할합니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @param chunkSize 1 이상의 chunk 크기
     * @return null이 아닌 결과 목록
     * @throws IllegalArgumentException chunkSize가 1 미만인 경우
     */
    public static <T> List<List<T>> partition(List<T> values, int chunkSize) {
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize는 1 이상이어야 합니다.");
        List<T> safe = values == null ? List.of() : values;
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < safe.size(); i += chunkSize) {
            result.add(List.copyOf(safe.subList(i, Math.min(i + chunkSize, safe.size()))));
        }
        return List.copyOf(result);
    }
    /** 첫 원소가 없으면 null을 반환합니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @return 계약에 따른 결과 값
     */
    public static <T> T firstOrNull(List<T> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }
    /** 마지막 원소가 없으면 null을 반환합니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @return 계약에 따른 결과 값
     */
    public static <T> T lastOrNull(List<T> values) { return values == null || values.isEmpty() ? null : values.getLast(); }
    /** 두 Collection을 순서대로 결합한 불변 List를 만듭니다.
     * @param first 앞쪽 Collection
     * @param second 뒤쪽 Collection
     * @return null이 아닌 결과 목록
     */
    public static <T> List<T> concat(Collection<? extends T> first, Collection<? extends T> second) {
        java.util.ArrayList<T> result = new java.util.ArrayList<>();
        if (first != null) result.addAll(first); if (second != null) result.addAll(second);
        return List.copyOf(result);
    }
    /** 메모리 목록을 0-base page/size 기준으로 안전하게 잘라냅니다.
     * @param values 입력 Collection/Map. 메서드에 따라 null을 빈 값으로 취급합니다.
     * @param page 0 이상의 page 번호
     * @param size 1 이상의 page/chunk 크기
     * @return null이 아닌 결과 목록
     * @throws IllegalArgumentException page가 음수이거나 size가 1 미만인 경우
     */
    public static <T> List<T> page(List<T> values, int page, int size) {
        if (page < 0 || size < 1) throw new IllegalArgumentException("page는 0 이상, size는 1 이상이어야 합니다.");
        List<T> safe = values == null ? List.of() : values; int from = Math.min(page * size, safe.size());
        return List.copyOf(safe.subList(from, Math.min(from + size, safe.size())));
    }
}
