package com.cpf.core.api.page;

import java.util.List;

/**
 * Offset Page 요청 표준입니다.
 * page는 0부터 시작하며 size는 운영 안전을 위해 기본 최대 500건으로 제한합니다.
 */
public record CpfPageRequest(int page, int size, List<CpfSort> sorts) {
    public static final int DEFAULT_SIZE = 50;
    public static final int DEFAULT_MAX_SIZE = 500;

    public CpfPageRequest {
        if (page < 0) throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        if (size <= 0 || size > DEFAULT_MAX_SIZE) {
            throw new IllegalArgumentException("size는 1~" + DEFAULT_MAX_SIZE + " 범위여야 합니다.");
        }
        sorts = sorts == null ? List.of() : List.copyOf(sorts);
    }

    public static CpfPageRequest of(int page, int size) { return new CpfPageRequest(page, size, List.of()); }
    public static CpfPageRequest first() { return of(0, DEFAULT_SIZE); }
    public long offset() { return (long) page * size; }
}
