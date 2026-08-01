package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** ADM 감사 로그와 durable delivery 재처리를 제공하는 운영 API입니다. */
@RestController
@RequestMapping("/adm/api/audit-logs")
@Tag(name="ADM-OPR Audit Logs",description="Operator audit and durable delivery recovery APIs")
public class AdmAuditLogController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmAuditLogService service;
    public AdmAuditLogController(AdmAuditLogService service){this.service=service;}
    @GetMapping @CpfOnlineTransaction(id="OADMOP0050",name="ADMAuditLogList")
    @Operation(operationId="admAuditLogFindAuditLogs",summary="감사 로그 조회")
    public ResponseEntity<Map<String,Object>> find(@RequestParam(required=false)String operatorId,@RequestParam(required=false)String actionType,@RequestParam(required=false)String
            targetType,@RequestParam(required=false)String targetId,@RequestParam(defaultValue="100")int limit){
        Map<String,Object> r=new LinkedHashMap<>();r.put("items",service.findAuditLogs(operatorId,actionType,targetType,targetId,limit));return ResponseEntity.ok(r);
    }
    @GetMapping("/deliveries") @CpfOnlineTransaction(id="OADMOP0052",name="ADMAuditDeliveryList")
    @Operation(operationId="admAuditDeliveryList",summary="감사 전달 상태 조회")
    public ResponseEntity<Map<String,Object>> deliveries(@RequestParam(required=false)String deliveryStatus,@RequestParam(defaultValue="100")int limit){return
            ResponseEntity.ok(Map.of("items",service.findDeliveries(deliveryStatus,limit)));}
    @PostMapping("/deliveries/{deliveryId}/retry") @CpfOnlineTransaction(id="OADMOP0053",name="ADMAuditDeliveryRetry")
    @Operation(operationId="admAuditDeliveryRetry",summary="감사 전달 수동 재처리")
    public ResponseEntity<Map<String,Object>> retry(@PathVariable long deliveryId,@RequestParam @NotBlank String reason,HttpServletRequest request){return
            ResponseEntity.ok(service.retryDelivery(deliveryId,requireOperator(request),reason));}
}
