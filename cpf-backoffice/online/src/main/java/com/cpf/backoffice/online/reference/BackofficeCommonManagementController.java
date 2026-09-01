package com.cpf.backoffice.online.reference;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;


import com.cpf.common.management.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/** Code/Parameter/Calendar/Template Common Product Service 운영 API입니다. */
// CPF stereotype 이 붙은 Business Type 은 proxy-safe 여야 한다.
// CpfCapabilityUsageAspect.proxySafeBusinessType() 이 final Type 을 proxy-unsafe 로 판정하고,
// Advisor 가 매칭되면 CGLIB subclass 생성이 불가능해 Runtime 기동이 실패한다.
@CpfController
@RequestMapping("/api/v1/backoffice/common")
@Tag(name="MBW-Common",description="Common Product Service 관리 API")
public class BackofficeCommonManagementController extends com.cpf.backoffice.online.base.BackofficeBaseController {
    private final BackofficeCommonManagementService service;
    public BackofficeCommonManagementController(BackofficeCommonManagementService service){this.service=service;}

    @GetMapping("/{resource}")    @Operation(operationId="MBW_COMMON_SEARCH",summary="Common Code/Parameter/Calendar/Template 검색/Paging")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_SEARCH", name = "Common Code/Parameter/Calendar/Template 검색/Paging", description = "Common Code/Parameter/Calendar/Template 검색/Paging 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfCommonPage<Map<String,Object>>> search(@PathVariable CpfCommonResource resource,
            @RequestParam(required=false) String query,@RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size,
            @RequestParam(required=false) Boolean includeDisabled,@RequestParam(required=false) Instant effectiveAt){
        return ResponseEntity.ok(service.search(resource,query,page,size,includeDisabled,effectiveAt));
    }

    @PostMapping("/{resource}/detail")    @Operation(operationId="MBW_COMMON_DETAIL",summary="Common 자원 상세")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_DETAIL", name = "Common 자원 상세", description = "Common 자원 상세 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** detail 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> detail(@PathVariable CpfCommonResource resource,@RequestBody Map<String,Object> identifiers){return ResponseEntity.ok(service.get(resource,identifiers));}

    @PostMapping("/{resource}")    @Operation(operationId="MBW_COMMON_CREATE",summary="Common 자원 등록")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_CREATE", name = "Common 자원 등록", description = "Common 자원 등록 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String,Object>> create(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("backoffice.operatorId") String actor){return ResponseEntity.ok(service.create(resource,request,requiredOperator(actor)));}

    @PutMapping("/{resource}")    @Operation(operationId="MBW_COMMON_UPDATE",summary="Common 자원 수정/버전검증")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_UPDATE", name = "Common 자원 수정/버전검증", description = "Common 자원 수정/버전검증 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> update(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("backoffice.operatorId") String actor){return ResponseEntity.ok(service.update(resource,request,requiredOperator(actor)));}

    @DeleteMapping("/{resource}")    @Operation(operationId="MBW_COMMON_DELETE",summary="Common 자원 비활성화")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_DELETE", name = "Common 자원 비활성화", description = "Common 자원 비활성화 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String,Object>> delete(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("backoffice.operatorId") String actor){return ResponseEntity.ok(service.delete(resource,request,requiredOperator(actor)));}
}
