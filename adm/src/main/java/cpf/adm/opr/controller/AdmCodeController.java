package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.cmn.cde.dto.CommonCodeRequest;
import cpf.cmn.cde.service.CodeCacheService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/codes")
@Tag(name = "ADM-PFW Codes", description = "PFW 공통 코드 관리 API")
public class AdmCodeController {
    private final CodeCacheService codeCacheService;
    private final AdmAuditLogService auditLogService;

    public AdmCodeController(CodeCacheService codeCacheService, AdmAuditLogService auditLogService) {
        this.codeCacheService = codeCacheService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADM-CDE-01-0010", name = "ADMCodeList")
    @Operation(operationId = "admCodeFindCodes", summary = "공통 코드 목록 조회", description = "pfw_code 기준 코드 그룹과 코드를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCodes() {
        return ResponseEntity.ok(codeCacheService.getAllCodes());
    }

    @GetMapping("/{codeId}")
    @CpfOnlineTransaction(id = "OADM-CDE-01-0011", name = "ADMCodeDetail")
    @Operation(operationId = "admCodeFindCode", summary = "공통 코드 상세 조회", description = "코드 ID로 pfw_code 상세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findCode(@PathVariable Long codeId) {
        return ResponseEntity.ok(codeCacheService.getCodeById(codeId));
    }

    @PostMapping
    @CpfOnlineTransaction(id = "OADM-CDE-02-0012", name = "ADMCodeCreate")
    @Operation(operationId = "admCodeCreateCode", summary = "공통 코드 등록", description = "pfw_code에 신규 코드를 등록하고 코드 캐시를 갱신합니다.")
    public ResponseEntity<Map<String, Object>> createCode(
            @Valid @RequestBody CommonCodeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.getReason());
        Map<String, Object> created = codeCacheService.createCode(request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.getRequestUser()),
                "CODE_CREATE",
                "pfw_code",
                String.valueOf(created.getOrDefault("codeId", request.getCodeKey())),
                reason,
                null,
                String.valueOf(created),
                String.valueOf(created),
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{codeId}")
    @CpfOnlineTransaction(id = "OADM-CDE-03-0013", name = "ADMCodeUpdate")
    @Operation(operationId = "admCodeUpdateCode", summary = "공통 코드 수정", description = "pfw_code를 수정하고 코드 캐시를 갱신합니다.")
    public ResponseEntity<Map<String, Object>> updateCode(
            @PathVariable Long codeId,
            @Valid @RequestBody CommonCodeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.getReason());
        Map<String, Object> before = codeCacheService.getCodeById(codeId);
        Map<String, Object> updated = codeCacheService.updateCode(codeId, request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.getRequestUser()),
                "CODE_UPDATE",
                "pfw_code",
                String.valueOf(codeId),
                reason,
                String.valueOf(before),
                String.valueOf(updated),
                "코드 수정",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{codeId}")
    @CpfOnlineTransaction(id = "OADM-CDE-04-0014", name = "ADMCodeDisable")
    @Operation(operationId = "admCodeDeleteCode", summary = "공통 코드 비활성", description = "pfw_code를 비활성화하고 코드 캐시를 갱신합니다.")
    public ResponseEntity<List<Map<String, Object>>> deleteCode(
            @PathVariable Long codeId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser,
            HttpServletRequest servletRequest) {
        String requiredReason = auditLogService.requireReason(reason);
        Map<String, Object> before = codeCacheService.getCodeById(codeId);
        List<Map<String, Object>> latest = codeCacheService.deleteCode(codeId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, requestUser),
                "CODE_DISABLE",
                "pfw_code",
                String.valueOf(codeId),
                requiredReason,
                String.valueOf(before),
                null,
                "코드 비활성",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(latest);
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
