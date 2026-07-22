package cpf.acc.account.controller;

import cpf.cmn.api.account.AccountSummary;
import cpf.cmn.api.account.AccountSummaryFacade;
import cpf.acc.common.base.AccBaseController;
import cpf.pfw.common.execution.CpfSharedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CPF 내부 서비스 신원으로만 호출하는 ACC 공유 API입니다. */
@RestController
@RequestMapping("/internal/api/v1/accounts")
@Tag(name = "ACC 내부 공유 API", description = "외부 Gateway에 공개하지 않는 CPF 주제영역 간 계정 계약")
public class AccAccountSharedController extends AccBaseController {
    private final AccountSummaryFacade facade;

    public AccAccountSharedController(AccountSummaryFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/{accountId}/summary")
    @CpfSharedApi(
            id = "SACCAC0001", name = "ACC 계정 요약 공유", ownerDomain = "ACC",
            description = "MBR 등 CPF 내부 주제영역에 마스킹된 계정 요약을 제공합니다.",
            requiredPermission = "ACC_ACCOUNT_INTERNAL_READ",
            allowedCallers = "MBR")
    @Operation(operationId = "getAccAccountSummaryInternal", summary = "ACC 내부 계정 요약 조회")
    public ResponseEntity<AccountSummary> summary(@PathVariable long accountId) {
        return ResponseEntity.ok(facade.findSummary(accountId));
    }
}
