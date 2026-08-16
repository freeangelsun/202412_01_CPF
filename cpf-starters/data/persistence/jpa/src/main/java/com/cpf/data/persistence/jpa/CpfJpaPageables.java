package com.cpf.data.persistence.jpa;

import com.cpf.data.persistence.api.page.CpfPage;
import com.cpf.data.persistence.api.page.CpfPageRequest;
import com.cpf.data.persistence.api.page.CpfSort;
import com.cpf.data.persistence.api.page.CpfSortDirection;
import com.cpf.data.persistence.api.CpfPersistencePolicy;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** CPF Paging/Sort를 Spring Data 타입으로 안전하게 변환합니다. */
public final class CpfJpaPageables {
    private CpfJpaPageables() { }
    public static PageRequest toPageable(CpfPageRequest page, List<CpfSort> sorts, Collection<String> allowedSortFields) {
        CpfPageRequest safe = CpfPersistencePolicy.requireBoundedPage(page);
        List<CpfSort> checked = CpfPersistencePolicy.requireAllowedSorts(sorts, allowedSortFields);
        Sort springSort = Sort.by(checked.stream().map(s -> new Sort.Order(
                s.direction() == CpfSortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC, s.field())).toList());
        return PageRequest.of(safe.page(), safe.size(), springSort);
    }
    public static <T> CpfPage<T> fromSpring(Page<T> page) {
        return new CpfPage<>(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
