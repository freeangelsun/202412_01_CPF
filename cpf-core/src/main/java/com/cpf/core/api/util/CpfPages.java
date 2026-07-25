package com.cpf.core.api.util;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.page.CpfSlice;
import java.util.List;

/** CPF 표준 Page/Slice 생성 편의 API입니다. */
public final class CpfPages {
    private CpfPages() {}
    public static CpfPageRequest request(int page, int size) { return CpfPageRequest.of(page, size); }
    public static <T> CpfPage<T> page(List<T> items, CpfPageRequest request, long total) { return CpfPage.of(items, request, total); }
    /** 메모리/EDU에서 offset page를 안전하게 잘라내는 편의 API입니다. DB에서는 LIMIT/OFFSET을 사용하십시오. */
    public static <T> CpfPage<T> offsetPage(List<T> all, CpfPageRequest request) {
        List<T> source = all == null ? List.of() : List.copyOf(all);
        int from = Math.min(source.size(), Math.toIntExact(request.offset()));
        int to = Math.min(source.size(), from + request.size());
        return CpfPage.of(source.subList(from, to), request, source.size());
    }
    public static <T> CpfSlice<T> slice(List<T> rowsWithLookAhead, int page, int size) { return CpfSlice.fromLookAhead(rowsWithLookAhead, page, size); }
}
