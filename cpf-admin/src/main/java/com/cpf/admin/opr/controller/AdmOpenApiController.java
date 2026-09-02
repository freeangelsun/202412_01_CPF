package com.cpf.admin.opr.controller;

import com.cpf.admin.common.base.AdmBaseController;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.web.api.openapi.CpfOpenAPIOperations;
import com.cpf.web.api.openapi.CpfOpenAPISnapshot;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Web Profile의 OpenAPI 운영 Contract만 소비하는 ADM Owner API입니다. */
@RestController
@RequestMapping("/adm/api/openapi")
@Tag(name = "ADM-OpenAPI", description = "OpenAPI Web MVC 상태와 감사된 재대사")
/** 메서드의 @PreAuthorize 가 Spring AOP 프록시를 만든다. CGLIB 은 final 클래스를 상속할 수 없어
 *  final 을 두면 "Cannot subclass final class" 로 기동이 실패한다. final 을 다시 붙이지 말 것. */
public class AdmOpenApiController extends AdmBaseController {
    private final CpfOpenAPIOperations operations;
    private final AdmAuditLogService audit;
    public AdmOpenApiController(CpfOpenAPIOperations operations, AdmAuditLogService audit){ this.operations=operations;this.audit=audit; }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('ADM_OPENAPI_READ')")    @Operation(operationId="admOpenApiStatus",summary="OpenAPI Web MVC 상태 조회")
    public CpfOpenAPISnapshot status(HttpServletRequest request){ requireOperator(request);return operations.snapshot(); }

    @PostMapping("/refresh")
    @PreAuthorize("hasAuthority('ADM_OPENAPI_REFRESH')")    @Operation(operationId="admOpenApiRefresh",summary="OpenAPI Web MVC Route Inventory 재대사")
    public CpfOpenAPISnapshot refresh(@RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,@RequestBody RefreshRequest body,HttpServletRequest request){
        if(!"confirmed".equalsIgnoreCase(riskConfirmed)) throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,"위험 조치 확인 Header가 필요합니다.");
        String operator=requireOperator(request);String reason=audit.requireReason(body==null?null:body.reason());CpfOpenAPISnapshot result=operations.refresh(reason);
        audit.record(CpfContexts.transactionId(),operator,"OPENAPI_REFRESH","openapi",result.instanceId(),reason,request.getRemoteAddr());return result;
    }
    public record RefreshRequest(String reason) {}
}
