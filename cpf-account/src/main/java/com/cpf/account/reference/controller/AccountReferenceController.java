package com.cpf.account.reference.controller;

import com.cpf.account.reference.dto.AccountReferenceSearchRequest;
import com.cpf.account.reference.facade.AccountReferenceFacade;
import com.cpf.account.reference.validation.AccountReferenceSearchValidator;
import com.cpf.account.common.base.AccBaseController;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 생성기 기본 골격의 참조 데이터 조회 API를 제공합니다.
 *
 * <p>업무 Controller는 요청 검증과 Swagger 계약을 담당하고,
 * 실제 업무 처리는 Facade와 Service에 위임합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/acc/reference")
@RequiredArgsConstructor
@Tag(name = "ACC 생성 참조", description = "생성기 기본 골격과 대표 업무 기능의 경계를 확인하는 참조 API")
public class AccountReferenceController extends AccBaseController {
    private final AccountReferenceFacade facade;
    private final AccountReferenceSearchValidator validator;

    @GetMapping
    @CpfOnlineTransaction(id = "OACCQY0001", name = "ACC 참조 목록 조회", ownerDomain = "ACC")
    @Operation(
            operationId = "searchAccReference",
            summary = "ACC 생성 참조 목록 조회",
            description = "검색어, 페이징, 정렬 whitelist를 적용해 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> search(AccountReferenceSearchRequest request) {
        validator.validate(request);
        return ok(facade.search(request));
    }
}
