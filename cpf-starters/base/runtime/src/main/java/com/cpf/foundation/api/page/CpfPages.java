package com.cpf.foundation.api.page;

import java.util.List;

/** CPF 공통 Paging 생성 Utility입니다. */
public final class CpfPages {
    private CpfPages() {
    }

    public static CpfPageRequest request(Integer page, Integer size) {
        return CpfPageRequest.of(page, size);
    }

    /** offsetPage 작업을 CPF 표준 계약에 따라 수행한다. */
    public static <T> CpfPage<T> offsetPage(List<T> source, CpfPageRequest request) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        int from = (int) Math.min(request.offset(), source.size());
        int to = Math.min(from + request.size(), source.size());
        return new CpfPage<>(source.subList(from, to), source.size(), request.page(), request.size());
    }
}
