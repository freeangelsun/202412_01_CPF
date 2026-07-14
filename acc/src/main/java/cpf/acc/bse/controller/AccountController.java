package cpf.acc.bse.controller;

import cpf.acc.bse.entity.AccAccount;
import cpf.acc.bse.service.AccAccountService;
import cpf.acc.bse.service.MbrMemberClientService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.workflow.CpfWorkflow;
import cpf.pfw.common.workflow.CpfWorkflowFailurePolicy;
import cpf.pfw.common.workflow.CpfWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@Validated
@Tag(name = "ACC-BSE Account", description = "ACC account sample APIs and MBR service-call sample")
public class AccountController {

    private final AccAccountService accAccountService;
    private final MbrMemberClientService mbrMemberClientService;

    public AccountController(
            AccAccountService accAccountService,
            MbrMemberClientService mbrMemberClientService) {
        this.accAccountService = accAccountService;
        this.mbrMemberClientService = mbrMemberClientService;
    }

    @GetMapping
    @CpfTransaction(id = "ACC01BSE0001", name = "ACCAccountList")
    @Operation(operationId = "accountGetAllAccounts", summary = "ACC account list", description = "Reads sample accounts from the ACC database.")
    public ResponseEntity<Map<String, Object>> getAllAccounts() {
        List<AccAccount> accounts = accAccountService.getAllAccounts();

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "ACC accounts fetched successfully");
        response.put("data", accounts);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mbr/detail")
    @CpfTransaction(id = "ACC08BSE0001", name = "MBRMemberDetail")
    @CpfWorkflow(id = "ACC08BSE9001", name = "MBRMemberDetailWorkflow")
    @CpfWorkflowStep(name = "CallMBRMemberDetail", failurePolicy = CpfWorkflowFailurePolicy.VERIFY)
    @Operation(operationId = "accountGetMbrMemberDetail", summary = "MBR member detail", description = "Calls MBR through PFW CpfWebClient and propagates CPF headers.")
    public ResponseEntity<Map<String, Object>> getMbrMemberDetail(
            @RequestParam(name = "memberId")
            @Positive(message = "memberId must be positive")
            Integer memberId) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "MBR member detail fetched successfully");
        response.put("data", mbrMemberClientService.getMemberDetail(memberId));

        return ResponseEntity.ok(response);
    }
}
