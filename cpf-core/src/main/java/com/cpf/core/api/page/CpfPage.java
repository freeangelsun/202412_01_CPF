package com.cpf.core.api.page;

import java.util.List;

/** CPF 공통 서버 Paging 응답 계약. */
public record CpfPage<T>(List<T> content, long totalElements, int page, int size) {
    public CpfPage {
        content = content == null ? List.of() : List.copyOf(content);
        if (totalElements < 0) throw new IllegalArgumentException("totalElements는 0 이상이어야 합니다.");
        if (page < 0) throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        if (size < 1) throw new IllegalArgumentException("size는 1 이상이어야 합니다.");
    }

    public long totalPages() {
        return totalElements == 0 ? 0 : (totalElements + size - 1) / size;
    }

    public boolean hasNext() {
        return ((long) page + 1L) * size < totalElements;
    }

    /** JSON/API 소비자가 사용하는 표준 목록 이름입니다. */
    public List<T> items() {
        return content;
    }

    /** 첫 Page가 아닌지 반환합니다. */
    public boolean hasPrevious() {
        return page > 0;
    }

    /** 표준 Page 요청과 조회 결과를 응답 계약으로 조립합니다. */
    public static <T> CpfPage<T> of(List<T> content, CpfPageRequest request, long totalElements) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return new CpfPage<>(content, totalElements, request.page(), request.size());
    }
}
