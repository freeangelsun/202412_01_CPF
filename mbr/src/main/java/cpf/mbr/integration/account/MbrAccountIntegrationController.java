package cpf.mbr.integration.account;

import cpf.cmn.api.account.AccountSummary;
import cpf.cmn.api.account.AccountSummaryFacade;
import cpf.mbr.common.base.MbrBaseController;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** MBR→ACC Remote Facade와 표준 실행 ID 전파를 검증하는 대표 통합 거래입니다. */
@RestController
@RequestMapping("/mbr/api/v1/account-integration")
@Tag(name = "MBR ACC 통합", description = "주제영역 경계를 지키는 MBR→ACC 서비스 호출 예제")
public class MbrAccountIntegrationController extends MbrBaseController {
    private final AccountSummaryFacade accountSummaryFacade;

    public MbrAccountIntegrationController(AccountSummaryFacade accountSummaryFacade) {
        this.accountSummaryFacade = accountSummaryFacade;
    }

    @GetMapping("/{accountId}")
    @CpfOnlineTransaction(
            id = "OMBRAC0001", name = "MBR ACC 계정 요약 연계", ownerDomain = "MBR",
            description = "MBR 온라인 거래에서 ACC 내부 공유 API를 Facade Contract로 호출합니다.",
            requiredPermission = "MBR_ACCOUNT_LINK_READ")
    @Operation(operationId = "getMbrLinkedAccountSummary", summary = "MBR에서 ACC 계정 요약 조회")
    public ResponseEntity<AccountSummary> find(@PathVariable long accountId) {
        return ResponseEntity.ok(accountSummaryFacade.findSummary(accountId));
    }
}
