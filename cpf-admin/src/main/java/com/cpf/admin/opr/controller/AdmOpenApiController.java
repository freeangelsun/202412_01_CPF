package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.web.api.openapi.CpfOpenApiOperations;
import com.cpf.web.api.openapi.CpfOpenApiSnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import com.cpf.web.api.CpfController;
import org.springframework.web.server.ResponseStatusException;

/** Web Profile의 OpenAPI 운영 Contract만 소비하는 ADM Owner API입니다. */
@CpfController
@RequestMapping("/adm/api/openapi")
@Tag(name = "ADM-OpenAPI", description = "OpenAPI Web MVC 상태와 감사된 재대사")
public final class AdmOpenApiController extends AdmBaseController {
    private final CpfOpenApiOperations operations;
    private final AdmAuditLogService audit;
    public AdmOpenApiController(CpfOpenApiOperations operations, AdmAuditLogService audit){ this.operations=operations;this.audit=audit; }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('ADM_OPENAPI_READ')")
    @CpfOnlineTransaction(id="OADMOPAPI10",name="ADMOpenApiStatus",ownerDomain="ADM",requiredPermission="ADM_OPENAPI_READ")
    @Operation(operationId="admOpenApiStatus",summary="OpenAPI Web MVC 상태 조회")
    public CpfOpenApiSnapshot status(HttpServletRequest request){ requireOperator(request);return operations.snapshot(); }

    @PostMapping("/refresh")
    @PreAuthorize("hasAuthority('ADM_OPENAPI_REFRESH')")
    @CpfOnlineTransaction(id="OADMOPAPI20",name="ADMOpenApiRefresh",ownerDomain="ADM",requiredPermission="ADM_OPENAPI_REFRESH",auditReasonRequired=true)
    @Operation(operationId="admOpenApiRefresh",summary="OpenAPI Web MVC Route Inventory 재대사")
    public CpfOpenApiSnapshot refresh(@RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,@RequestBody RefreshRequest body,HttpServletRequest request){
        if(!"confirmed".equalsIgnoreCase(riskConfirmed)) throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,"위험 조치 확인 Header가 필요합니다.");
        String operator=requireOperator(request);String reason=audit.requireReason(body==null?null:body.reason());CpfOpenApiSnapshot result=operations.refresh(reason);
        audit.record(CpfContexts.transactionId(),operator,"OPENAPI_REFRESH","openapi",result.instanceId(),reason,request.getRemoteAddr());return result;
    }
    public record RefreshRequest(String reason) {}
}
