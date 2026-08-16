package com.cpf.data.persistence.api.page;

import java.util.List;

/** Keyset/Cursor Page 응답 표준입니다. */
public record CpfCursorPage<T>(List<T> items, CpfCursor nextCursor, boolean hasNext) {
    public CpfCursorPage {
        items = items == null ? List.of() : List.copyOf(items);
        if (hasNext && nextCursor == null) throw new IllegalArgumentException("hasNext=true이면 nextCursor가 필요합니다.");
    }

    /** 마지막 cursor page를 생성합니다.
     * @param items null이면 빈 목록으로 정규화할 조회 결과
     * @return hasNext=false, nextCursor=null인 마지막 page
     */
    public static <T> CpfCursorPage<T> last(List<T> items) {
        return new CpfCursorPage<>(items, null, false);
    }
}
