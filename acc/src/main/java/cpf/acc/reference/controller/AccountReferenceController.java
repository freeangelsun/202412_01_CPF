package cpf.acc.controller;

import cpf.acc.dto.AccountSearchRequest;
import cpf.acc.facade.AccountFacade;
import cpf.acc.validation.AccountSearchValidator;
import cpf.pfw.common.base.BaseController;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Account 조회 API를 제공합니다.
 *
 * <p>업무 Controller는 요청 검증과 Swagger 계약을 담당하고,
 * 실제 업무 처리는 Facade와 Service에 위임합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/acc")
@RequiredArgsConstructor
@Tag(name = "ACC 업무", description = "Account 주제영역 조회 API")
public class AccountController extends BaseController {
    private final AccountFacade facade;
    private final AccountSearchValidator validator;

    @GetMapping
    @CpfOnlineTransaction(id = "OACCQY0001", name = "AccountSearch", ownerDomain = "ACC")
    @Operation(
            operationId = "searchAccount",
            summary = "Account 목록 조회",
            description = "검색어, 페이징, 정렬 whitelist를 적용해 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> search(AccountSearchRequest request) {
        validator.validate(request);
        return ok(facade.search(request));
    }
}