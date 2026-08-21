package com.cpf.education.online.externalrest.controller;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.externalrest.dto.InstitutionInquiryResponse;
import com.cpf.education.online.externalrest.service.InstitutionInquiryService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
@CpfController @RequestMapping("/edu/online/institution-inquiry")
/** InstitutionInquiryController는 기관 REST 응답을 CPF 공통 Outcome으로 변환하는 External REST Golden Path입니다. */
public class InstitutionInquiryController {
 private final InstitutionInquiryService service; public InstitutionInquiryController(InstitutionInquiryService service){this.service=service;}
 @GetMapping @Operation(operationId="EDU_INSTITUTION_INQUIRY",summary="외부기관 REST 조회")
 @CpfOnlineTransaction(operationId="EDU_INSTITUTION_INQUIRY",name="외부기관 조회",description="Service→Client→기관 Outcome Adapter→CpfRestClient 흐름으로 CPF Result를 반환한다.")
 public CpfResult<InstitutionInquiryResponse> get(@RequestParam String accountId){return service.inquire(accountId);}
}
