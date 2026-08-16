package com.cpf.foundation.api.page;

import java.util.List;

/** Cursor/stream 조회용 경량 CPF 공개 Slice 계약입니다. */
public record CpfSlice<T>(List<T> content, long offset, int size, boolean hasNext) {
    public CpfSlice {
        content = content == null ? List.of() : List.copyOf(content);
        if (offset < 0) throw new IllegalArgumentException("offset must be >= 0");
        if (size < 1 || size > 500) throw new IllegalArgumentException("size must be between 1 and 500");
    }
}
