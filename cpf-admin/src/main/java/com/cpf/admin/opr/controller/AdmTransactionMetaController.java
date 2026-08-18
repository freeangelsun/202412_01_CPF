package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmTransactionMetaService;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 거래 Metadata·Timeline·Page 조회와 운영 변경 계약을 제공합니다. */
@RestController
@RequestMapping("/adm/api/transactions")
@Tag(name = "ADM-OPR Operation Catalog", description = "Runtime이 자동 등록한 CPF 업무 Operation Catalog와 운영 정책 조회 API")
public class AdmTransactionMetaController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmTransactionMetaService transactionMetaService;
    private final AdmAuditLogService auditLogService;

    public AdmTransactionMetaController(
            AdmTransactionMetaService transactionMetaService,
            AdmAuditLogService auditLogService) {
        this.transactionMetaService = transactionMetaService;
        this.auditLogService = auditLogService;
    }

    @GetMapping    @Operation(operationId = "admTransactionMetaFindTransactions", summary = "거래 메타 목록 조회", description = "@CpfOnlineTransaction 기반으로 등록된 온라인 거래 메타를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findTransactions(
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String activeYn,
            @RequestParam(required = false) String operationId,
            @RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(transactionMetaService.findTransactions(moduleCode, activeYn, operationId, limit));
    }

    @GetMapping("/page")    @Operation(operationId = "admTransactionMetaFindPage", summary = "거래 메타 Server Paging", description = "3개 공식 DB Vendor의 Owner Repository에서 count와 page를 함께 조회합니다.")
    public ResponseEntity<AdmTransactionMetaService.TransactionMetaPage> findPage(
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String activeYn,
            @RequestParam(required = false) String operationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionMetaService.findPage(
                moduleCode, activeYn, operationId, page, size));
    }

    @GetMapping("/{operationId}")    @Operation(operationId = "admTransactionMetaFindTransaction", summary = "거래 메타 상세 조회", description = "단일 업무 거래 ID의 Controller/API mapping 메타를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findTransaction(@PathVariable String operationId) {
        return ResponseEntity.ok(transactionMetaService.findTransaction(operationId));
    }

    @PostMapping("/{operationId}/inactive")    @Operation(operationId = "admTransactionMetaInactivate", summary = "거래 메타 비활성화", description = "더 이상 사용하지 않는 거래 메타를 inactive 처리합니다.")
    public ResponseEntity<Map<String, Object>> inactivate(
            @PathVariable String operationId,
            @RequestParam long policyVersion,
            @RequestParam String reason,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        String operatorId = requireOperator(servletRequest);
        Map<String, Object> result = transactionMetaService.inactivate(operationId, policyVersion, operatorId, auditReason);
        auditLogService.record(
                CpfContexts.transactionId(),
                operatorId,
                "TRANSACTION_META_INACTIVE",
                "OPS_OPERATION_POLICY",
                operationId,
                auditReason,
                String.valueOf(result.get("before")),
                String.valueOf(result.get("after")),
                "거래 메타 비활성화",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

}
