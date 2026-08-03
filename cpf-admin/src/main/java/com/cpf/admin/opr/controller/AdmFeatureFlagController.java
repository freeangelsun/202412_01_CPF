package com.cpf.admin.opr.controller;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.featureflag.CpfFeatureFlagContext;
import com.cpf.core.api.featureflag.CpfFeatureFlagOperations;
import com.cpf.core.api.featureflag.CpfFeatureFlagResult;
import com.cpf.core.api.featureflag.CpfFeatureFlagValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Commercial ADM feature flag query and two-person controlled command API. */
@RestController
@RequestMapping("/adm/api/platform/feature-flags")
@Tag(name = "ADM-FeatureFlag", description = "Feature Flag 평가·Override·Kill Switch 운영 API")
public final class AdmFeatureFlagController {
    private final CpfFeatureFlagOperations operations;

    public AdmFeatureFlagController(CpfFeatureFlagOperations operations) {
        this.operations = operations;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADM_FEATURE_FLAG_READ')")
    @CpfOnlineTransaction(id = "OADMFF0010", name = "ADMFeatureFlagSearch")
    @Operation(operationId = "admFeatureFlagSearch", summary = "Feature Flag 검색")
    public List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest request) {
        operator(request);
        return operations.search(query, page, size);
    }

    @GetMapping("/{flagKey}")
    @PreAuthorize("hasAuthority('ADM_FEATURE_FLAG_READ')")
    @CpfOnlineTransaction(id = "OADMFF0011", name = "ADMFeatureFlagDetail")
    @Operation(operationId = "admFeatureFlagFind", summary = "Feature Flag 상세")
    public CpfFeatureFlagResult<CpfFeatureFlagValue> find(
            @PathVariable String flagKey, HttpServletRequest request) {
        operator(request);
        return operations.find(flagKey);
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasAuthority('ADM_FEATURE_FLAG_READ')")
    @CpfOnlineTransaction(id = "OADMFF0012", name = "ADMFeatureFlagEvaluate")
    @Operation(operationId = "admFeatureFlagEvaluate", summary = "Typed Feature Flag 평가")
    public CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
            @RequestBody EvaluateRequest body, HttpServletRequest request) {
        operator(request);
        return operations.evaluate(body.flagKey(), value(body.valueType(), body.value()),
                new CpfFeatureFlagContext(body.targetingKey(), body.attributes()));
    }

    @PostMapping("/override-requests")
    @PreAuthorize("hasAuthority('ADM_FEATURE_FLAG_WRITE')")
    @CpfOnlineTransaction(id = "OADMFF0020", name = "ADMFeatureFlagOverrideRequest")
    @Operation(operationId = "admFeatureFlagRequestOverride", summary = "Override 승인 요청")
    public Map<String, String> requestOverride(
            @RequestBody OverrideRequest body, HttpServletRequest request) {
        String operator = operator(request);
        String requestId = operations.requestOverride(body.flagKey(), value(body.valueType(), body.value()),
                body.expiresAt(), operator, required(body.reason(), "reason"));
        return Map.of("requestId", requestId);
    }

    @PostMapping("/override-requests/{requestId}/approve")
    @PreAuthorize("hasAuthority('ADM_FEATURE_FLAG_APPROVE')")
    @CpfOnlineTransaction(id = "OADMFF0021", name = "ADMFeatureFlagOverrideApprove")
    @Operation(operationId = "admFeatureFlagApproveOverride", summary = "Override 승인")
    public CpfFeatureFlagResult<CpfFeatureFlagValue> approve(
            @PathVariable String requestId,
            @RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,
            @RequestBody DecisionRequest body,
            HttpServletRequest request) {
        confirmRisk(riskConfirmed);
        return operations.approveOverride(requestId, operator(request), required(body.reason(), "reason"));
    }

    @PostMapping("/override-requests/{requestId}/revoke")
    @PreAuthorize("hasAuthority('ADM_FEATURE_FLAG_WRITE')")
    @CpfOnlineTransaction(id = "OADMFF0022", name = "ADMFeatureFlagOverrideRevoke")
    @Operation(operationId = "admFeatureFlagRevokeOverride", summary = "Override 회수")
    public void revoke(
            @PathVariable String requestId,
            @RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,
            @RequestBody DecisionRequest body,
            HttpServletRequest request) {
        confirmRisk(riskConfirmed);
        operations.revokeOverride(requestId, operator(request), required(body.reason(), "reason"));
    }

    @PostMapping("/{flagKey}/kill-switch")
    @PreAuthorize("hasAuthority('ADM_FEATURE_FLAG_KILL_SWITCH')")
    @CpfOnlineTransaction(id = "OADMFF0023", name = "ADMFeatureFlagKillSwitch")
    @Operation(operationId = "admFeatureFlagSetKillSwitch", summary = "Kill Switch 변경")
    public void setKillSwitch(
            @PathVariable String flagKey,
            @RequestHeader("X-CPF-Risk-Confirmed") String riskConfirmed,
            @RequestBody KillSwitchRequest body,
            HttpServletRequest request) {
        confirmRisk(riskConfirmed);
        operations.setKillSwitch(flagKey, body.enabled(), operator(request), required(body.reason(), "reason"));
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

    private static CpfFeatureFlagValue value(String type, String value) {
        return switch (required(type, "valueType").toUpperCase(java.util.Locale.ROOT)) {
            case "BOOLEAN" -> new CpfFeatureFlagValue.BooleanValue(Boolean.parseBoolean(required(value, "value")));
            case "INTEGER" -> new CpfFeatureFlagValue.IntegerValue(Long.parseLong(required(value, "value")));
            case "DECIMAL", "NUMBER" -> new CpfFeatureFlagValue.DecimalValue(Double.parseDouble(required(value, "value")));
            case "STRING" -> new CpfFeatureFlagValue.StringValue(required(value, "value"));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported valueType");
        };
    }

    public record EvaluateRequest(String flagKey, String valueType, String value, String targetingKey,
                                  Map<String, String> attributes) {}
    public record OverrideRequest(String flagKey, String valueType, String value, Instant expiresAt, String reason) {}
    public record DecisionRequest(String reason) {}
    public record KillSwitchRequest(boolean enabled, String reason) {}
}
