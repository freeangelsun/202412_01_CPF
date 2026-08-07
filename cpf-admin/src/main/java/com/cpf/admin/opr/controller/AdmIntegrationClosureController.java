package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.time.CpfTimeSnapshot;
import com.cpf.core.api.webhook.CpfWebhookDelivery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@ConditionalOnBean(AdmIntegrationClosureService.class)
@RequestMapping("/adm/api/integration-closure")
@Tag(name = "ADM-Integration-Closure", description = "시간·데이터 품질·Webhook 운영 조회 및 서버 승인 조치")
@SecurityRequirement(name = "admSessionCookie")
@Validated
public class AdmIntegrationClosureController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmIntegrationClosureService service;

    public AdmIntegrationClosureController(AdmIntegrationClosureService service) {
        this.service = service;
    }


    @GetMapping("/crypto/status")
    @CpfOnlineTransaction(id = "OADMIC0108", name = "AdmCryptoStatus")
    @Operation(operationId = "admIntegrationCryptoStatus", summary = "암호화 Provider 및 활성 Key Version 상태")
    public ResponseEntity<Map<String, Object>> cryptoStatus() {
        return ResponseEntity.ok(service.cryptoStatus());
    }

    @GetMapping("/time/health")
    @CpfOnlineTransaction(id = "OADMIC0101", name = "AdmTimeHealth")
    @Operation(operationId = "admIntegrationTimeHealth", summary = "UTC/업무시간/NTP skew 상태")
    public ResponseEntity<CpfTimeSnapshot> time(
            @RequestParam(defaultValue = "Asia/Seoul") String zone,
            @RequestParam(defaultValue = "1000") long maxSkewMillis) {
        return ResponseEntity.ok(service.timeHealth(zone, maxSkewMillis));
    }

    @PostMapping("/data-quality/validate/{recordId}")
    @CpfOnlineTransaction(id = "OADMIC0102", name = "AdmDataQualityValidate")
    @Operation(operationId = "admIntegrationDataQualityValidate", summary = "데이터 품질 검증")
    public ResponseEntity<CpfDataQualityDecision> validate(
            @PathVariable String recordId,
            @RequestBody Map<String, Object> record) {
        return ResponseEntity.ok(service.validate(recordId, record));
    }

    @PostMapping("/data-quality/quarantine/{id}/correction-approvals")
    @CpfOnlineTransaction(id = "OADMIC0103", name = "AdmDataQualityCorrectionApprovalRequest")
    @Operation(operationId = "admIntegrationDataQualityCorrectionApprovalRequest", summary = "격리 데이터 정정 승인 요청")
    public ResponseEntity<Map<String, Object>> requestCorrection(
            @PathVariable String id,
            @RequestAttribute("adm.operatorId") String operator,
            @Valid @RequestBody CorrectionApprovalRequest request) {
        return ResponseEntity.ok(service.requestCorrection(
                id,
                request.expectedVersion(),
                request.corrected(),
                request.idempotencyKey(),
                operator,
                request.reason()));
    }

    @PostMapping("/data-quality/correction-approvals/{approvalRequestId}/execute")
    @CpfOnlineTransaction(id = "OADMIC0107", name = "AdmDataQualityCorrectionExecute")
    @Operation(operationId = "admIntegrationDataQualityCorrectionExecute", summary = "승인 완료 격리 데이터 정정 단회 실행")
    public ResponseEntity<Map<String, Object>> executeCorrection(
            @PathVariable long approvalRequestId,
            @RequestAttribute("adm.operatorId") String operator,
            @Valid @RequestBody CorrectionExecutionRequest request) {
        Map<String, Object> result = service.executeCorrection(approvalRequestId, operator, request.reason());
        return "DQ-VERSION-CONFLICT".equals(ownerResultCode(result))
                ? ResponseEntity.status(HttpStatus.CONFLICT).body(result)
                : ResponseEntity.ok(result);
    }

    @PostMapping("/data-quality/quarantine/{id}/replay")
    @CpfOnlineTransaction(id = "OADMIC0104", name = "AdmDataQualityReplay")
    @Operation(operationId = "admIntegrationDataQualityReplay", summary = "격리 데이터 재검증")
    public ResponseEntity<CpfDataQualityDecision> replayQuality(
            @PathVariable String id,
            @RequestAttribute("adm.operatorId") String operator,
            @Valid @RequestBody QualityReplayRequest request) {
        return ResponseEntity.ok(service.replayQuality(
                id, request.expectedVersion(), request.idempotencyKey(), operator, request.reason()));
    }

    @GetMapping("/webhooks/dlq")
    @CpfOnlineTransaction(id = "OADMIC0105", name = "AdmWebhookDlq")
    @Operation(operationId = "admIntegrationWebhookDlq", summary = "Webhook DLQ 조회")
    public ResponseEntity<List<CpfWebhookDelivery>> dlq(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(service.webhookDlq(limit));
    }

    @PostMapping("/webhooks/{id}/replay")
    @CpfOnlineTransaction(id = "OADMIC0106", name = "AdmWebhookReplay")
    @Operation(operationId = "admIntegrationWebhookReplay", summary = "Webhook DLQ/UNKNOWN 재처리")
    public ResponseEntity<CpfWebhookDelivery> replayWebhook(
            @PathVariable String id,
            @RequestParam @Min(1) long expectedVersion,
            @RequestParam @Size(min=8,max=500) String reason,
            @RequestAttribute("adm.operatorId") String operator) {
        return ResponseEntity.ok(service.replayWebhook(id, expectedVersion, operator, reason));
    }

    public record CorrectionApprovalRequest(
            @Min(1) long expectedVersion,
            @NotBlank @Size(min=8,max=128) String idempotencyKey,
            @NotBlank @Size(min=8,max=500) String reason,
            @NotEmpty Map<String, Object> corrected) {
        public CorrectionApprovalRequest {
            idempotencyKey = require(idempotencyKey, "idempotencyKey");
            reason = require(reason, "reason");
            corrected = immutableNullable(corrected);
        }
    }

    public record CorrectionExecutionRequest(@NotBlank @Size(min=8,max=500) String reason) {
        public CorrectionExecutionRequest { reason = require(reason, "reason"); }
    }

    public record QualityReplayRequest(
            @Min(1) long expectedVersion,
            @NotBlank @Size(min=8,max=128) String idempotencyKey,
            @NotBlank @Size(min=8,max=500) String reason) {
        public QualityReplayRequest {
            idempotencyKey=require(idempotencyKey,"idempotencyKey");
            reason=require(reason,"reason");
        }
    }

    private static Map<String,Object> immutableNullable(Map<String,Object> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source == null ? Map.of() : source));
    }

    private static String ownerResultCode(Map<String, Object> result) {
        Object execution = result.get("execution");
        if (!(execution instanceof Map<?, ?> row)) return "";
        for (String key : List.of("ownerResultCode", "OWNER_RESULT_CODE", "owner_result_code")) {
            Object value = row.get(key);
            if (value != null) return String.valueOf(value).trim();
        }
        return "";
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
