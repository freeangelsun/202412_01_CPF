package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmCacheOperationService;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/cache")
@Tag(name = "ADM-OPR Cache", description = "CMN cache summary and refresh APIs")
public class AdmCacheController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmCacheOperationService cacheOperationService;
    private final AdmAuditLogService auditLogService;

    public AdmCacheController(AdmCacheOperationService cacheOperationService, AdmAuditLogService auditLogService) {
        this.cacheOperationService = cacheOperationService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/summary")
    @CpfOnlineTransaction(id = "OADMOP0010", name = "ADMCacheSummary")
    @Operation(operationId = "admCacheSummary", summary = "Cache summary", description = "Returns CMN cache counts and samples.")
    public ResponseEntity<Map<String, Object>> summary() {
        return safeResponse(cacheOperationService::summary);
    }

    @PostMapping("/refresh")
    @CpfOnlineTransaction(id = "OADMOP0011", name = "ADMCacheRefresh")
    @Operation(operationId = "admCacheRefresh", summary = "Refresh cache", description = "Refreshes CODE, MESSAGE, RESPONSE_CODE, CONFIG, or ALL cache targets.")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestParam(defaultValue = "ALL") String target,
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        ResponseEntity<Map<String, Object>> response = safeResponse(() -> cacheOperationService.refresh(target));
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, requestUser),
                "CACHE_REFRESH",
                "cache",
                target,
                auditReason,
                servletRequest.getRemoteAddr());
        return response;
    }

    private ResponseEntity<Map<String, Object>> safeResponse(CacheAction action) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("result", action.run());
        } catch (DataAccessException ex) {
            response.put("available", false);
            response.put("result", Map.of());
            response.put("message", "CMN cache database is not available.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @FunctionalInterface
    private interface CacheAction {
        Map<String, Object> run();
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
