package com.cpf.data.persistence.api;
import com.cpf.data.persistence.api.page.CpfPage;
import com.cpf.data.persistence.api.page.CpfPageRequest;
import com.cpf.data.persistence.api.page.CpfSlice;
import com.cpf.data.persistence.api.page.CpfSort;
import java.util.List;
/** Spring Data PagingAndSortingRepository naming에 맞춘 CPF 검색 계약입니다. */
public interface CpfPagingAndSortingRepository<T,Q> {
    CpfPage<T> findAll(Q criteria, CpfPageRequest page, List<CpfSort> sorts);
    CpfSlice<T> findSlice(Q criteria, CpfPageRequest page, List<CpfSort> sorts);
}
