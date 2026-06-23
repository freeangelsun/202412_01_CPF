package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmTransactionMetaService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.transaction.CpfTransactionMetaScanResult;
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

@RestController
@RequestMapping("/adm/api/transactions")
@Tag(name = "ADM-OPR Transaction Meta", description = "CPF 온라인 거래 메타 조회와 scan API")
public class AdmTransactionMetaController {
    private final AdmTransactionMetaService transactionMetaService;
    private final AdmAuditLogService auditLogService;

    public AdmTransactionMetaController(
            AdmTransactionMetaService transactionMetaService,
            AdmAuditLogService auditLogService) {
        this.transactionMetaService = transactionMetaService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfTransaction(id = "ADM01TRN0010", name = "ADMTransactionMetaList")
    @Operation(summary = "거래 메타 목록 조회", description = "@CpfTransaction 기반으로 등록된 온라인 거래 메타를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findTransactions(
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String activeYn,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(transactionMetaService.findTransactions(moduleCode, activeYn, transactionId, limit));
    }

    @GetMapping("/{transactionId}")
    @CpfTransaction(id = "ADM01TRN0011", name = "ADMTransactionMetaDetail")
    @Operation(summary = "거래 메타 상세 조회", description = "단일 업무 거래 ID의 Controller/API mapping 메타를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionMetaService.findTransaction(transactionId));
    }

    @PostMapping("/scan")
    @CpfTransaction(id = "ADM05TRN0012", name = "ADMTransactionMetaScan")
    @Operation(summary = "거래 메타 재스캔", description = "현재 기동 중인 Spring MVC mapping을 스캔해 pfw_transaction_meta를 upsert합니다.")
    public ResponseEntity<CpfTransactionMetaScanResult> scan(
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        String operatorId = requestUser(servletRequest, requestUser);
        CpfTransactionMetaScanResult result = transactionMetaService.scan(operatorId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                operatorId,
                "TRANSACTION_META_SCAN",
                "pfw_transaction_meta",
                "ALL",
                auditReason,
                null,
                String.valueOf(result),
                "거래 메타 재스캔",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{transactionId}/inactive")
    @CpfTransaction(id = "ADM04TRN0013", name = "ADMTransactionMetaInactive")
    @Operation(summary = "거래 메타 비활성화", description = "더 이상 사용하지 않는 거래 메타를 inactive 처리합니다.")
    public ResponseEntity<Map<String, Object>> inactivate(
            @PathVariable String transactionId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        String operatorId = requestUser(servletRequest, requestUser);
        Map<String, Object> result = transactionMetaService.inactivate(transactionId, operatorId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                operatorId,
                "TRANSACTION_META_INACTIVE",
                "pfw_transaction_meta",
                transactionId,
                auditReason,
                String.valueOf(result.get("before")),
                String.valueOf(result.get("after")),
                "거래 메타 비활성화",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
