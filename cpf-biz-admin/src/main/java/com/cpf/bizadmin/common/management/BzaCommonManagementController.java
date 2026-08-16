package com.cpf.bizadmin.common.management;


import com.cpf.web.api.CpfController;
import com.cpf.common.management.*;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/** Code/Parameter/Calendar/Template Common Product Service 운영 API입니다. */
@CpfController
@RequestMapping("/api/bza/common")
@Tag(name="BZA-Common",description="Common Product Service 관리 API")
public final class BzaCommonManagementController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final BzaCommonManagementService service;
    public BzaCommonManagementController(BzaCommonManagementService service){this.service=service;}

    @GetMapping("/{resource}")
    @CpfOnlineTransaction(id="OBZACM2101",name="BzaCommonSearch",ownerDomain="BZA",requiredPermission="SETTING.READ")
    @Operation(operationId="bzaCommonSearch",summary="Common Code/Parameter/Calendar/Template 검색/Paging")
    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfCommonPage<Map<String,Object>>> search(@PathVariable CpfCommonResource resource,
            @RequestParam(required=false) String query,@RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size,
            @RequestParam(required=false) Boolean includeDisabled,@RequestParam(required=false) Instant effectiveAt){
        return ResponseEntity.ok(service.search(resource,query,page,size,includeDisabled,effectiveAt));
    }

    @PostMapping("/{resource}/detail")
    @CpfOnlineTransaction(id="OBZACM2102",name="BzaCommonDetail",ownerDomain="BZA",requiredPermission="SETTING.READ")
    @Operation(operationId="bzaCommonDetail",summary="Common 자원 상세")
    /** detail 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> detail(@PathVariable CpfCommonResource resource,@RequestBody Map<String,Object> identifiers){return ResponseEntity.ok(service.get(resource,identifiers));}

    @PostMapping("/{resource}")
    @CpfOnlineTransaction(id="OBZACM2201",name="BzaCommonCreate",ownerDomain="BZA",requiredPermission="SETTING.WRITE",auditReasonRequired=true)
    @Operation(operationId="bzaCommonCreate",summary="Common 자원 등록")
    public ResponseEntity<Map<String,Object>> create(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("bza.operatorId") String actor){return ResponseEntity.ok(service.create(resource,request,requiredOperator(actor)));}

    @PutMapping("/{resource}")
    @CpfOnlineTransaction(id="OBZACM2202",name="BzaCommonUpdate",ownerDomain="BZA",requiredPermission="SETTING.WRITE",auditReasonRequired=true)
    @Operation(operationId="bzaCommonUpdate",summary="Common 자원 수정/버전검증")
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> update(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("bza.operatorId") String actor){return ResponseEntity.ok(service.update(resource,request,requiredOperator(actor)));}

    @DeleteMapping("/{resource}")
    @CpfOnlineTransaction(id="OBZACM2203",name="BzaCommonDisable",ownerDomain="BZA",requiredPermission="SETTING.WRITE",auditReasonRequired=true)
    @Operation(operationId="bzaCommonDelete",summary="Common 자원 비활성화")
    public ResponseEntity<Map<String,Object>> delete(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("bza.operatorId") String actor){return ResponseEntity.ok(service.delete(resource,request,requiredOperator(actor)));}
}
