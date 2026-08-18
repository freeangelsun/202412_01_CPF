package com.cpf.education.online.domaincall.controller;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.domaincall.dto.CreditCheckResponse;
import com.cpf.education.online.domaincall.service.MemberCreditService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
@CpfRestController
@RequestMapping("/edu/online/member-credit")
/** MemberCreditController는 현재 Operation과 Target Operation을 분리하고 CPF Domain Client를 사용하는 내부 Domain 호출 Golden Path입니다. */
public class MemberCreditController {
    private final MemberCreditService service;
    /** MemberCreditController 동작은 현재 Operation과 Target Operation을 분리하고 CPF Domain Client를 사용하는 내부 Domain 호출 Golden Path에서 필요한 공개 동작을 수행합니다. */
    public MemberCreditController(MemberCreditService service) { this.service = service; }
    @PostMapping
    @Operation(operationId="EDU_MEMBER_CREDIT_CHECK", summary="CPF Domain 간 호출")
    @CpfOnlineTransaction(operationId="EDU_MEMBER_CREDIT_CHECK", name="회원 신용조회", description="현재 MBR operation과 EXS_CREDIT_CHECK target operation을 분리하고 Context를 Framework가 자동 전파한다.")
    /** check 동작은 현재 Operation과 Target Operation을 분리하고 CPF Domain Client를 사용하는 내부 Domain 호출 Golden Path에서 필요한 공개 동작을 수행합니다. */
    public CpfResult<CreditCheckResponse> check(@RequestParam String memberId) { return service.check(memberId); }
}
