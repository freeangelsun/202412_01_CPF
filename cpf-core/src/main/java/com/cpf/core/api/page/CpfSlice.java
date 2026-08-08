package com.cpf.core.api.page;

import java.util.List;

/** 전체 건수 COUNT가 필요 없는 Slice 응답 표준입니다. */
public record CpfSlice<T>(List<T> items, int page, int size, boolean hasNext) {
    public CpfSlice {
        items = items == null ? List.of() : List.copyOf(items);
        if (page < 0 || size <= 0) throw new IllegalArgumentException("Slice metadata가 올바르지 않습니다.");
    }

    /** Repository는 size+1건을 조회한 뒤 이 메서드로 안전하게 Slice를 만들 수 있습니다.
     * @param rows look-ahead 1건을 포함할 수 있는 조회 결과. null이면 빈 목록
     * @param page 0 이상의 페이지 번호
     * @param size 1 이상의 노출 크기
     * @return size를 넘는 행을 숨기고 hasNext를 계산한 Slice
     * @throws IllegalArgumentException page 또는 size가 유효하지 않은 경우
     */
    public static <T> CpfSlice<T> fromLookAhead(List<T> rows, int page, int size) {
        List<T> safe = rows == null ? List.of() : rows;
        boolean next = safe.size() > size;
        List<T> visible = next ? safe.subList(0, size) : safe;
        return new CpfSlice<>(visible, page, size, next);
    }
}
