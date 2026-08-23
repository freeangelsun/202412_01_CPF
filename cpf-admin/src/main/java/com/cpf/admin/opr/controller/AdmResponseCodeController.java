package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.message.api.*;
import com.cpf.common.message.dto.CommonResponseCodeRequest;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Response Code catalog의 canonical Common Product Service Consumer입니다. */
@RestController
@RequestMapping("/adm/api/response-codes")
@Tag(name="ADM-OPR Response Codes",description="CMN_RESPONSE_CODE management API")
public class AdmResponseCodeController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfCommonCatalogManagementService common; private final AdmAuditLogService audit;
    public AdmResponseCodeController(CpfCommonCatalogManagementService common,AdmAuditLogService audit){this.common=common;this.audit=audit;}
    @GetMapping @Operation(operationId="admResponseCodeFindAll",summary="List response codes")
    public ResponseEntity<Map<String,Object>> findAll(HttpServletRequest r){requireOperator(r);return ResponseEntity.ok(Map.of("available",true,"result",common.searchResponseCodes(null,null,0,500).content().stream().map(this::map).toList()));}
    @GetMapping("/{responseCode}") @Operation(operationId="admResponseCodeFindOne",summary="Get response code")
    public ResponseEntity<Map<String,Object>> findOne(@PathVariable String responseCode,HttpServletRequest r){requireOperator(r);return ResponseEntity.ok(Map.of("available",true,"result",map(common.getResponseCode(responseCode))));}
    @PostMapping @Operation(operationId="admResponseCodeCreate",summary="Create response code")
    public ResponseEntity<Map<String,Object>> create(@Valid @RequestBody CommonResponseCodeRequest b,@RequestParam String reason,HttpServletRequest r){String actor=requireOperator(r),required=audit.requireReason(reason);CpfResponseCodeRecord out=common.createResponseCode(b,actor,required);record(r,actor,"RESPONSE_CODE_CREATE",out.responseCode(),required);return ResponseEntity.ok(Map.of("available",true,"result",map(out)));}
    @PutMapping("/{responseCode}") @Operation(operationId="admResponseCodeUpdate",summary="Update response code")
    public ResponseEntity<Map<String,Object>> update(@PathVariable String responseCode,@Valid @RequestBody CommonResponseCodeRequest b,@RequestParam String reason,HttpServletRequest r){String actor=requireOperator(r),required=audit.requireReason(reason);CpfResponseCodeRecord before=common.getResponseCode(responseCode);long expected=b.getCatalogVersion()==null?before.catalogVersion():b.getCatalogVersion();CpfResponseCodeRecord out=common.updateResponseCode(responseCode,expected,b,actor,required);record(r,actor,"RESPONSE_CODE_UPDATE",responseCode,required);return ResponseEntity.ok(Map.of("available",true,"result",map(out)));}
    @DeleteMapping("/{responseCode}") @Operation(operationId="admResponseCodeDelete",summary="Disable response code")
    public ResponseEntity<Map<String,Object>> delete(@PathVariable String responseCode,@RequestParam String reason,HttpServletRequest r){String actor=requireOperator(r),required=audit.requireReason(reason);CpfResponseCodeRecord before=common.getResponseCode(responseCode);common.deleteResponseCode(responseCode,before.catalogVersion(),actor,required);record(r,actor,"RESPONSE_CODE_DISABLE",responseCode,required);return ResponseEntity.ok(Map.of("available",true,"result",common.searchResponseCodes(null,null,0,500).content().stream().map(this::map).toList()));}
    private Map<String,Object> map(CpfResponseCodeRecord x){Map<String,Object> m=new LinkedHashMap<>();m.put("responseCode",x.responseCode());m.put("messageCode",x.messageCode());m.put("resultType",x.resultType());m.put("moduleId",x.moduleId());m.put("responseGroup",x.responseGroup());m.put("sequenceNo",x.sequenceNo());m.put("httpStatus",x.httpStatus());m.put("category",x.category());m.put("retryDisposition",x.retryDisposition());m.put("exposure",x.exposure());m.put("effectiveFrom",x.effectiveFrom());m.put("effectiveTo",x.effectiveTo());m.put("catalogVersion",x.catalogVersion());m.put("description",x.description());m.put("useYn",x.useYn());m.put("updatedAt",x.updatedAt());return m;}
    private void record(HttpServletRequest req,String actor,String action,String key,String reason){audit.record(CpfContexts.transactionId(),actor,action,"CMN_RESPONSE_CODE",key,reason,req.getRemoteAddr());}
}
