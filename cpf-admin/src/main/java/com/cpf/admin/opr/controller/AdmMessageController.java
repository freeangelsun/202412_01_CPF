package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.message.api.*;
import com.cpf.common.message.dto.CommonMessageRequest;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** 공통 메시지 카탈로그를 canonical Common Product Service로 관리합니다. */
@RestController
@RequestMapping("/adm/api/messages")
@Tag(name="ADM-CPF Messages",description="CPF 공통 메시지 관리 API")
public class AdmMessageController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfCommonCatalogManagementService common; private final AdmAuditLogService audit;
    public AdmMessageController(CpfCommonCatalogManagementService common,AdmAuditLogService audit){this.common=common;this.audit=audit;}

    @GetMapping @Operation(operationId="admMessageFindMessages",summary="공통 메시지 목록 조회")
    public ResponseEntity<List<Map<String,Object>>> findMessages(HttpServletRequest request){requireOperator(request);return ResponseEntity.ok(common.searchMessages(null,null,null,0,500).content().stream().map(this::map).toList());}
    @GetMapping("/{messageId}") @Operation(operationId="admMessageFindMessage",summary="공통 메시지 상세 조회")
    public ResponseEntity<Map<String,Object>> findMessage(@PathVariable Long messageId,HttpServletRequest request){requireOperator(request);return ResponseEntity.ok(map(common.getMessage(messageId)));}
    @PostMapping @Operation(operationId="admMessageCreateMessage",summary="공통 메시지 등록")
    public ResponseEntity<Map<String,Object>> createMessage(@Valid @RequestBody CommonMessageRequest b,HttpServletRequest r){String actor=requireOperator(r),reason=audit.requireReason(b.getReason());CpfMessageRecord out=common.createMessage(b,actor,reason);record(r,actor,"MESSAGE_CREATE",String.valueOf(out.messageId()),reason,null,out);return ResponseEntity.ok(map(out));}
    @PutMapping("/{messageId}") @Operation(operationId="admMessageUpdateMessage",summary="공통 메시지 수정")
    public ResponseEntity<Map<String,Object>> updateMessage(@PathVariable Long messageId,@Valid @RequestBody CommonMessageRequest b,HttpServletRequest r){String actor=requireOperator(r),reason=audit.requireReason(b.getReason());CpfMessageRecord before=common.getMessage(messageId);long expected=b.getCatalogVersion()==null?before.catalogVersion():b.getCatalogVersion();CpfMessageRecord out=common.updateMessage(messageId,expected,b,actor,reason);record(r,actor,"MESSAGE_UPDATE",String.valueOf(messageId),reason,before,out);return ResponseEntity.ok(map(out));}
    @DeleteMapping("/{messageId}") @Operation(operationId="admMessageDeleteMessage",summary="공통 메시지 비활성")
    public ResponseEntity<List<Map<String,Object>>> deleteMessage(@PathVariable Long messageId,@RequestParam String reason,HttpServletRequest r){String actor=requireOperator(r),required=audit.requireReason(reason);CpfMessageRecord before=common.getMessage(messageId);common.deleteMessage(messageId,before.catalogVersion(),actor,required);record(r,actor,"MESSAGE_DISABLE",String.valueOf(messageId),required,before,null);return ResponseEntity.ok(common.searchMessages(null,null,null,0,500).content().stream().map(this::map).toList());}

    private Map<String,Object> map(CpfMessageRecord x){Map<String,Object> m=new LinkedHashMap<>();m.put("messageId",x.messageId());m.put("messageCode",x.messageCode());m.put("locale",x.locale());m.put("messageFormatType",x.messageFormatType());m.put("externalMessage",x.externalMessage());m.put("internalMessage",x.internalMessage());m.put("parameterCount",x.parameterCount());m.put("parameterSample",x.parameterSample());m.put("parameterSchemaJson",x.parameterSchemaJson());m.put("escapeHtmlYn",x.escapeHtmlYn());m.put("maskArgumentsYn",x.maskArgumentsYn());m.put("effectiveFrom",x.effectiveFrom());m.put("effectiveTo",x.effectiveTo());m.put("catalogVersion",x.catalogVersion());m.put("description",x.description());m.put("useYn",x.useYn());m.put("updatedAt",x.updatedAt());return m;}
    private void record(HttpServletRequest req,String actor,String action,String key,String reason,Object before,Object after){audit.record(CpfContexts.transactionId(),actor,action,"CMN_MESSAGE",key,reason,before==null?null:String.valueOf(before),after==null?null:String.valueOf(after),action,req.getRemoteAddr());}
}
