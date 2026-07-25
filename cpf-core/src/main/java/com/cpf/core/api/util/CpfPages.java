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
    public static <T> CpfSlice<T> slice(List<T> rowsWithLookAhead, int page, int size) { return CpfSlice.fromLookAhead(rowsWithLookAhead, page, size); }
}
