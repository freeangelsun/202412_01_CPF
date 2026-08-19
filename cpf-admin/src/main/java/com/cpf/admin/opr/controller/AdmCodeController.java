package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.code.dto.CommonCodeRequest;
import com.cpf.common.management.CpfCommonManagementApi;
import com.cpf.common.management.CpfCommonMutation;
import com.cpf.common.management.CpfCommonResource;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 공통 코드 Group·Item을 canonical Common Management API로 운영하는 ADM API입니다. */
@RestController
@RequestMapping("/adm/api/codes")
@Tag(name = "ADM-CPF Codes", description = "CPF 공통 코드 관리 API")
public class AdmCodeController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfCommonManagementApi common;
    private final AdmAuditLogService auditLogService;

    public AdmCodeController(CpfCommonManagementApi common, AdmAuditLogService auditLogService) {
        this.common = common;
        this.auditLogService = auditLogService;
    }

    @GetMapping @Operation(operationId = "admCodeFindCodes", summary = "공통 코드 목록 조회")
    public ResponseEntity<List<Map<String,Object>>> findCodes(HttpServletRequest request) {
        requireOperator(request);
        return ResponseEntity.ok(common.search(CpfCommonResource.CODE, null, 0, 200, true, null).content());
    }

    @GetMapping("/{codeId}") @Operation(operationId = "admCodeFindCode", summary = "공통 코드 상세 조회")
    public ResponseEntity<Map<String,Object>> findCode(@PathVariable Long codeId, HttpServletRequest request) {
        requireOperator(request);
        return ResponseEntity.ok(common.get(CpfCommonResource.CODE, Map.of("code_id", codeId)));
    }

    @PostMapping @Operation(operationId = "admCodeCreateCode", summary = "공통 코드 등록")
    public ResponseEntity<Map<String,Object>> createCode(@Valid @RequestBody CommonCodeRequest body, HttpServletRequest request) {
        String actor=requireOperator(request), reason=auditLogService.requireReason(body.getReason());
        Map<String,Object> created=common.create(CpfCommonResource.CODE,
                new CpfCommonMutation(Map.of(), values(body), null, reason), actor);
        audit(request,actor,"CODE_CREATE",String.valueOf(value(created,"code_id",body.getCodeKey())),reason,null,created);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{codeId}") @Operation(operationId = "admCodeUpdateCode", summary = "공통 코드 수정")
    public ResponseEntity<Map<String,Object>> updateCode(@PathVariable Long codeId,@Valid @RequestBody CommonCodeRequest body,HttpServletRequest request) {
        String actor=requireOperator(request), reason=auditLogService.requireReason(body.getReason());
        Map<String,Object> before=common.get(CpfCommonResource.CODE,Map.of("code_id",codeId));
        Map<String,Object> updated=common.update(CpfCommonResource.CODE,
                new CpfCommonMutation(Map.of("code_id",codeId),values(body),null,reason),actor);
        audit(request,actor,"CODE_UPDATE",String.valueOf(codeId),reason,before,updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{codeId}") @Operation(operationId = "admCodeDeleteCode", summary = "공통 코드 비활성")
    public ResponseEntity<List<Map<String,Object>>> deleteCode(@PathVariable Long codeId,@RequestParam String reason,HttpServletRequest request) {
        String actor=requireOperator(request), required=auditLogService.requireReason(reason);
        Map<String,Object> before=common.get(CpfCommonResource.CODE,Map.of("code_id",codeId));
        common.delete(CpfCommonResource.CODE,new CpfCommonMutation(Map.of("code_id",codeId),Map.of(),null,required),actor);
        audit(request,actor,"CODE_DISABLE",String.valueOf(codeId),required,before,null);
        return ResponseEntity.ok(common.search(CpfCommonResource.CODE,null,0,200,true,null).content());
    }

    private Map<String,Object> values(CommonCodeRequest r) {
        Map<String,Object> v=new LinkedHashMap<>();
        if(r.getParentId()!=null)v.put("parent_id",r.getParentId());
        v.put("code_key",r.getCodeKey()); v.put("code_value",r.getCodeValue());
        if(r.getDescription()!=null)v.put("description",r.getDescription());
        v.put("use_yn",r.getUseYn()); return v;
    }
    private Object value(Map<String,Object> map,String key,Object fallback){
        for(var e:map.entrySet()) if(e.getKey().equalsIgnoreCase(key)) return e.getValue(); return fallback;
    }
    private void audit(HttpServletRequest req,String actor,String action,String key,String reason,Object before,Object after){
        auditLogService.record(CpfContexts.transactionId(),actor,action,"CMN_CODE",key,reason,
                before==null?null:String.valueOf(before),after==null?null:String.valueOf(after),action,req.getRemoteAddr());
    }
}
