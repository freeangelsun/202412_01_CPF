package com.cpf.education.online;

import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.data.persistence.api.CpfPagingAndSortingRepository;
import com.cpf.data.persistence.api.page.CpfPage;
import com.cpf.data.persistence.api.page.CpfPageRequest;
import com.cpf.data.persistence.api.page.CpfSlice;
import com.cpf.data.persistence.api.page.CpfSort;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 온라인-02 조회·검색·Paging 거래: 하나의 Canonical 거래에서 Page/Slice/상세 조회 선택 기준을 보여줍니다. */
@CpfRestController
@RequestMapping("/edu/online/02-members")
public class Online02SearchPagingExample {
    private final ObjectProvider<CpfPagingAndSortingRepository<MemberView, MemberSearch>> repositories;

    public Online02SearchPagingExample(ObjectProvider<CpfPagingAndSortingRepository<MemberView, MemberSearch>> repositories) {
        this.repositories = repositories;
    }

    @GetMapping
    @Operation(operationId = "EDU-ONLINE-02", summary = "조회·검색·Paging 거래")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-02",
            name = "조회·검색·Paging 거래",
            description = "CPF Paging/Sort 계약으로 조건조회하고 COUNT 필요 여부에 따라 Page/Slice/상세를 선택한다.")
    /** search 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public SearchResult search(
            @RequestParam(defaultValue = "PAGE") SearchMode mode,
            @RequestParam(required = false) String memberId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        CpfPagingAndSortingRepository<MemberView, MemberSearch> repository = repository();
        MemberSearch criteria = new MemberSearch(memberId, name);
        CpfPageRequest request = new CpfPageRequest(page, size);
        List<CpfSort> sorts = List.of(CpfSort.asc("memberId"));
        return switch (mode) {
            case PAGE -> new SearchResult(mode, repository.findAll(criteria, request, sorts));
            case SLICE -> new SearchResult(mode, repository.findSlice(criteria, request, sorts));
            case DETAIL -> {
                if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("DETAIL mode requires memberId");
                CpfPage<MemberView> result = repository.findAll(criteria, new CpfPageRequest(0, 1), sorts);
                MemberView detail = result.content().stream().findFirst()
                        .orElseThrow(() -> new CpfNotFoundException("회원이 없습니다: " + memberId));
                yield new SearchResult(mode, detail);
            }
        };
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public enum SearchMode { PAGE, SLICE, DETAIL }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record SearchResult(SearchMode mode, Object data) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record MemberSearch(String memberId, String name) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record MemberView(String memberId, String name) { }

    private CpfPagingAndSortingRepository<MemberView, MemberSearch> repository() {
        CpfPagingAndSortingRepository<MemberView, MemberSearch> repository = repositories.getIfAvailable();
        if (repository == null) throw new IllegalStateException("CPF paging repository provider is not configured");
        return repository;
    }
}
