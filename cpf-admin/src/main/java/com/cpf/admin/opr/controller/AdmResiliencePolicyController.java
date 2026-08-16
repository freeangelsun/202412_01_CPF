package com.cpf.admin.opr.controller;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.api.CpfResiliencePolicyOperations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** ADM resilience policy query, request and two-person approval API. */
@CpfController
@RequestMapping("/adm/api/platform/resilience-policies")
@Tag(name = "ADM-ResiliencePolicy", description = "Resilience 정책 조회·승인 운영 API")
public final class AdmResiliencePolicyController {
    private final CpfResiliencePolicyOperations operations;

    public AdmResiliencePolicyController(CpfResiliencePolicyOperations operations) {
        this.operations = operations;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADM_RESILIENCE_READ')")
    @CpfOnlineTransaction(id = "OADMRS0010", name = "ADMResiliencePolicySearch", ownerDomain="ADM")
    @Operation(operationId = "admResiliencePolicySearch", summary = "Resilience 정책 검색")
    public List<CpfResiliencePolicy> search(@RequestParam(defaultValue = "") String query,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size,
                                             HttpServletRequest request) {
        operator(request);
        return operations.search(query, page, size);
    }

    @GetMapping("/{operationId}")
    @PreAuthorize("hasAuthority('ADM_RESILIENCE_READ')")
    @CpfOnlineTransaction(id = "OADMRS0011", name = "ADMResiliencePolicyDetail", ownerDomain="ADM")
    @Operation(operationId = "admResiliencePolicyFind", summary = "Resilience 정책 상세")
    public CpfResiliencePolicy find(@PathVariable String operationId, HttpServletRequest request) {
        operator(request);
        return operations.find(operationId);
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('ADM_RESILIENCE_WRITE')")
    @CpfOnlineTransaction(id = "OADMRS0020", name = "ADMResiliencePolicyRequest", ownerDomain="ADM")
    @Operation(operationId = "admResiliencePolicyRequest", summary = "Resilience 정책 승인 요청")
    public Map<String, String> request(@RequestBody PolicyRequest body, HttpServletRequest request) {
        String id = operations.requestChange(body.toPolicy(), operator(request), required(body.reason(), "reason"));
        return Map.of("requestId", id);
    }

    @PostMapping("/requests/{requestId}/approve")
    @PreAuthorize("hasAuthority('ADM_RESILIENCE_APPROVE')")
    @CpfOnlineTransaction(id = "OADMRS0021", name = "ADMResiliencePolicyApprove", ownerDomain="ADM")
    @Operation(operationId = "admResiliencePolicyApprove", summary = "Resilience 정책 승인")
    public CpfResiliencePolicy approve(@PathVariable String requestId,
                                        @RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,
                                        @RequestBody DecisionRequest body,
                                        HttpServletRequest request) {
        confirmRisk(riskConfirmed);
        return operations.approveChange(requestId, operator(request), required(body.reason(), "reason"));
    }

    @PostMapping("/requests/{requestId}/reject")
    @PreAuthorize("hasAuthority('ADM_RESILIENCE_APPROVE')")
    @CpfOnlineTransaction(id = "OADMRS0022", name = "ADMResiliencePolicyReject", ownerDomain="ADM")
    @Operation(operationId = "admResiliencePolicyReject", summary = "Resilience 정책 반려")
    public void reject(@PathVariable String requestId,
                       @RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,
                       @RequestBody DecisionRequest body,
                       HttpServletRequest request) {
        confirmRisk(riskConfirmed);
        operations.rejectChange(requestId, operator(request), required(body.reason(), "reason"));
    }

    private static String operator(HttpServletRequest request) {
        Object value = request.getAttribute("adm.operatorId");
        if (!(value instanceof String id) || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "검증된 ADM operator session이 필요합니다.");
        }
        return id.trim();
    }

    private static void confirmRisk(String value) {
        if (!"confirmed".equalsIgnoreCase(value)) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "위험 조치 확인 Header가 필요합니다.");
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " is required");
        return value.trim();
    }

    public record DecisionRequest(String reason) {}
    public record PolicyRequest(String operationId, long timeoutMs, int maxAttempts, long retryBackoffMs,
                                int circuitFailureThreshold, long circuitOpenMs, int bulkheadMaxConcurrent,
                                int rateLimitPermits, long rateLimitWindowMs, boolean idempotent,
                                boolean unknownResultReconcileEnabled, String reason) {
        CpfResiliencePolicy toPolicy() {
            return new CpfResiliencePolicy(required(operationId, "operationId"), 0,
                    Duration.ofMillis(timeoutMs), maxAttempts, Duration.ofMillis(retryBackoffMs),
                    circuitFailureThreshold, Duration.ofMillis(circuitOpenMs), bulkheadMaxConcurrent,
                    rateLimitPermits, Duration.ofMillis(rateLimitWindowMs), idempotent,
                    unknownResultReconcileEnabled);
        }
    }
}
