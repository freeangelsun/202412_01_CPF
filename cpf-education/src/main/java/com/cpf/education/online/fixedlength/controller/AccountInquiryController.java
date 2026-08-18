package com.cpf.education.online.fixedlength.controller;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.fixedlength.dto.AccountInquiryRequest;
import com.cpf.education.online.fixedlength.service.AccountInquiryService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.integration.fixedlength.api.CpfFixedLengthParseResult;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/account-inquiry")
/** AccountInquiryController는 고정길이 전문 Public API와 기관 Outcome Adapter를 분리하는 Fixed-Length Golden Path입니다. */
public final class AccountInquiryController {
 private final AccountInquiryService service; public AccountInquiryController(AccountInquiryService service){this.service=service;}
 @PostMapping @Operation(operationId="EDU_ACCOUNT_FIXED_INQUIRY",summary="고정길이 기관 계좌조회")
 @CpfOnlineTransaction(operationId="EDU_ACCOUNT_FIXED_INQUIRY",name="고정길이 기관 계좌조회",description="Controller→Service→Client→Outcome Adapter로 전문 호출의 4상태 Outcome을 보존한다.")
 public CpfResult<CpfFixedLengthParseResult> call(@RequestBody AccountInquiryRequest request){return service.inquire(request);}
}
