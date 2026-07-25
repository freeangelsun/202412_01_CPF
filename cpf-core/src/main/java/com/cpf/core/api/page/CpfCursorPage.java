package com.cpf.core.api.page;

import java.util.List;

/** Keyset/Cursor Page 응답 표준입니다. */
public record CpfCursorPage<T>(List<T> items, CpfCursor nextCursor, boolean hasNext) {
    public CpfCursorPage {
        items = items == null ? List.of() : List.copyOf(items);
        if (hasNext && nextCursor == null) throw new IllegalArgumentException("hasNext=true이면 nextCursor가 필요합니다.");
    }

    public static <T> CpfCursorPage<T> last(List<T> items) {
        return new CpfCursorPage<>(items, null, false);
    }
}
