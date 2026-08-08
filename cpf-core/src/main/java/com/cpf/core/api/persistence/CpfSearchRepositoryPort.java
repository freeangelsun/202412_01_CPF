package com.cpf.core.api.persistence;

import com.cpf.core.api.page.CpfCursor;
import com.cpf.core.api.page.CpfCursorPage;
import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.page.CpfSlice;
import com.cpf.core.api.page.CpfSort;
import java.util.List;

/** Domain typed 검색조건을 기존 CPF Paging/Sort 계약과 조합하는 조회 확장점입니다. */
public interface CpfSearchRepositoryPort<T, Q> {
    CpfPage<T> page(Q criteria, CpfPageRequest page, List<CpfSort> sorts);
    CpfSlice<T> slice(Q criteria, CpfPageRequest page, List<CpfSort> sorts);
    CpfCursorPage<T> cursor(Q criteria, CpfCursor after, int size, List<CpfSort> sorts);
}
