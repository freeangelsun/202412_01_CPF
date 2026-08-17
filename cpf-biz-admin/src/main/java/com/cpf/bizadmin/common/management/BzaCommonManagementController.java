package com.cpf.bizadmin.common.management;


import org.springframework.web.bind.annotation.RestController;
import com.cpf.common.management.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/** Code/Parameter/Calendar/Template Common Product Service 운영 API입니다. */
@RestController
@RequestMapping("/api/bza/common")
@Tag(name="BZA-Common",description="Common Product Service 관리 API")
public final class BzaCommonManagementController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final BzaCommonManagementService service;
    public BzaCommonManagementController(BzaCommonManagementService service){this.service=service;}

    @GetMapping("/{resource}")    @Operation(operationId="bzaCommonSearch",summary="Common Code/Parameter/Calendar/Template 검색/Paging")
    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfCommonPage<Map<String,Object>>> search(@PathVariable CpfCommonResource resource,
            @RequestParam(required=false) String query,@RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size,
            @RequestParam(required=false) Boolean includeDisabled,@RequestParam(required=false) Instant effectiveAt){
        return ResponseEntity.ok(service.search(resource,query,page,size,includeDisabled,effectiveAt));
    }

    @PostMapping("/{resource}/detail")    @Operation(operationId="bzaCommonDetail",summary="Common 자원 상세")
    /** detail 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> detail(@PathVariable CpfCommonResource resource,@RequestBody Map<String,Object> identifiers){return ResponseEntity.ok(service.get(resource,identifiers));}

    @PostMapping("/{resource}")    @Operation(operationId="bzaCommonCreate",summary="Common 자원 등록")
    public ResponseEntity<Map<String,Object>> create(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("bza.operatorId") String actor){return ResponseEntity.ok(service.create(resource,request,requiredOperator(actor)));}

    @PutMapping("/{resource}")    @Operation(operationId="bzaCommonUpdate",summary="Common 자원 수정/버전검증")
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> update(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("bza.operatorId") String actor){return ResponseEntity.ok(service.update(resource,request,requiredOperator(actor)));}

    @DeleteMapping("/{resource}")    @Operation(operationId="bzaCommonDelete",summary="Common 자원 비활성화")
    public ResponseEntity<Map<String,Object>> delete(@PathVariable CpfCommonResource resource,@RequestBody CpfCommonMutation request,@RequestAttribute("bza.operatorId") String actor){return ResponseEntity.ok(service.delete(resource,request,requiredOperator(actor)));}
}
