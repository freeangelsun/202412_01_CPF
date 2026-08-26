package com.cpf.admin.opr.batch.runtime;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.admin.common.base.AdmBaseController;
import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.data.api.CpfDataRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ADM Batch Control Plane facade. 조회 실패를 정상 빈 목록으로 위장하지 않고 stale/partial로 반환합니다.
 */
@RestController
@RequestMapping("/adm/api/batch-runtime")
@Tag(name = "ADM-Batch-Runtime", description = "BAT Control Server 기반 Runtime 운영 API")
public class BatchRuntimeControlController extends AdmBaseController {
    private static final Set<String> ALLOWED_VIEWS = Set.of(
            "overview", "instances", "scheduler", "worker-pools", "center-cut", "agents",
            "job-packs", "executions", "deployments", "recovery", "leases", "alerts", "audit");
    private static final Set<String> CLIENT_ACTOR_FIELDS = Set.of(
            "requestedBy", "requestUser", "actorId", "operatorId", "operatorIdOverride");

    private final BatchRuntimeControlClient client;
    private final AdmApprovalService approvalService;
    private final ObjectMapper objectMapper;

    public BatchRuntimeControlController(BatchRuntimeControlClient client, AdmApprovalService approvalService,
                                         ObjectMapper objectMapper) {
        this.client = client;
        this.approvalService = approvalService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/instances")
    @Operation(operationId = "admBatchRuntimeInstances", summary = "BAT Runtime Instance 조회")
    ResponseEntity<Map<String, Object>> instances(@RequestParam(defaultValue = "30") long staleAfterSeconds) {
        Instant fetchedAt = Instant.now();
        try {
            return ResponseEntity.ok(Map.of(
                    "fetchedAt", fetchedAt, "stale", false, "partial", false,
                    "items", client.instances(Math.max(5, staleAfterSeconds))));
        } catch (BatchControlClientException failure) {
            Map<String, Object> body = new java.util.LinkedHashMap<>(errorBody(failure));
            body.put("fetchedAt", fetchedAt);
            body.put("stale", true);
            body.put("partial", true);
            body.put("items", List.of());
            return ResponseEntity.status(status(failure)).body(body);
        } catch (RuntimeException failure) {
            return ResponseEntity.status(503).body(Map.of(
                    "fetchedAt", fetchedAt, "stale", true, "partial", true,
                    "items", List.of(), "errorCode", "BAT_CONTROL_UNREACHABLE",
                    "state", "FAILED", "message", "BAT Control Server 조회에 실패했습니다."));
        }
    }

    @GetMapping("/views/{view}")
    @Operation(operationId = "admBatchRuntimeView", summary = "BAT Runtime 운영 View 조회")
    ResponseEntity<Map<String, Object>> view(@PathVariable String view) {
        if (!ALLOWED_VIEWS.contains(view)) {
            return ResponseEntity.badRequest().body(Map.of("errorCode", "BAT_VIEW_NOT_ALLOWED"));
        }
        Instant fetchedAt = Instant.now();
        try {
            Map<String, Object> ownerView = client.view(view);
            return ResponseEntity.ok(Map.of(
                    "fetchedAt", fetchedAt,
                    "stale", false,
                    "partial", false,
                    "view", view,
                    "items", ownerView.getOrDefault("items", List.of())
            ));
        } catch (RuntimeException failure) {
            return ResponseEntity.status(503).body(Map.of(
                    "fetchedAt", fetchedAt,
                    "stale", true,
                    "partial", true,
                    "view", view,
                    "items", List.of(),
                    "errorCode", "BAT_CONTROL_UNREACHABLE"
            ));
        }
    }

    @GetMapping("/job-definitions")
    @Operation(operationId = "admBatchJobDefinitions", summary = "Versioned Batch Job Definition 조회")
    ResponseEntity<Map<String, Object>> jobDefinitions(@RequestParam(required = false) String jobId,
                                                       @RequestParam(required = false) String state,
                                                       @RequestParam(defaultValue = "200") int limit) {
        try {
            return ResponseEntity.ok(Map.of("fetchedAt", Instant.now(), "stale", false, "partial", false,
                    "items", client.jobDefinitions(jobId, state, limit)));
        } catch (BatchControlClientException failure) {
            Map<String,Object> body=new java.util.LinkedHashMap<>(errorBody(failure));body.put("fetchedAt",Instant.now());body.put("stale",true);body.put("partial",true);body.put("items",List.of());
            return ResponseEntity.status(status(failure)).body(body);
        }
    }

    @GetMapping("/job-definitions/{jobId}/versions/{version}")
    @Operation(operationId = "admBatchJobDefinitionDetail", summary = "Versioned Batch Job Definition 상세 조회")
    ResponseEntity<Map<String,Object>> jobDefinitionDetail(@PathVariable String jobId,@PathVariable long version) {
        try{return ResponseEntity.ok(client.jobDefinitionDetail(jobId,version));}
        catch(BatchControlClientException failure){return error(failure);}
    }

    @PostMapping("/job-definitions/validate")
    @Operation(operationId = "admBatchJobDefinitionValidate", summary = "Batch Job Definition 검증")
    ResponseEntity<Map<String, Object>> validateJobDefinition(@Valid @RequestBody BatchJobDefinitionRequest request) {
        try { return ResponseEntity.ok(client.validateJobDefinition(request.toMap())); }
        catch (BatchControlClientException failure) { Map<String,Object> body=new java.util.LinkedHashMap<>(errorBody(failure));body.put("valid",false);body.put("errors",
                List.of(failure.getMessage()));return ResponseEntity.status(status(failure)).body(body); }
    }

    @PostMapping("/job-definitions/drafts")
    @Operation(operationId = "admBatchJobDefinitionSave", summary = "Batch Job Definition Draft 저장")
    ResponseEntity<Map<String, Object>> saveJobDefinition(
            @RequestAttribute("adm.operatorId") String operatorId,
            @Valid @RequestBody BatchJobDefinitionRequest request) {
        try {
            Map<String,Object> payload = request.toMap();
            requireCommandField(payload, "jobId");
            requireCommandField(payload, "reason");
            return ResponseEntity.status(201).body(client.saveJobDefinition(withServerActor(payload, operatorId)));
        }
        catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("BAT_JOB_DEFINITION_INVALID", failure); }
    }

    @PostMapping("/job-definitions/{jobId}/versions/{version}/transition")
    @Operation(operationId = "admBatchJobDefinitionTransition", summary = "Batch Job Definition 승인·배포 상태 전환")
    ResponseEntity<Map<String, Object>> transitionJobDefinition(
            @RequestAttribute("adm.operatorId") String operatorId,
            @PathVariable String jobId, @PathVariable long version,
            @Valid @RequestBody BatchJobDefinitionTransitionRequest request) {
        try {
            Map<String,Object> payload = request.toMap();
            requireCommandField(payload, "targetState");
            requireCommandField(payload, "reason");
            requireVersion(payload, "expectedRowVersion");
            requireApprovalForRiskState(payload);
            return ResponseEntity.ok(client.transitionJobDefinition(jobId, version, withServerActor(payload, operatorId)));
        }
        catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("BAT_JOB_TRANSITION_INVALID", failure); }
    }

    @PostMapping("/commands")
    @Operation(operationId = "admBatchRuntimeCommand", summary = "승인 완료 BAT Runtime 위험조치 실행",
            description = "브라우저의 target/actor를 신뢰하지 않고 approvalRequestId의 immutable Snapshot을 Approval Engine fencing 경로로 실행합니다.")
    ResponseEntity<Map<String, Object>> command(
            @RequestAttribute("adm.operatorId") String operatorId,
            @RequestBody BatchRuntimeCommandRequest body) {
        try {
            if (body == null || body.approvalRequestId == null || body.approvalRequestId.isBlank()) {
                throw new IllegalArgumentException("approvalRequestId is required");
            }
            long approvalRequestId = Long.parseLong(body.approvalRequestId.trim());
            requireCommandField(Map.of("reason", body.reason == null ? "" : body.reason), "reason");
            return ResponseEntity.accepted().body(approvalService.execute(approvalRequestId, body.reason, operatorId));
        } catch (NumberFormatException failure) {
            return validation("BAT_APPROVAL_ID_INVALID", new IllegalArgumentException("approvalRequestId must be numeric", failure));
        } catch (IllegalArgumentException failure) {
            return validation("BAT_COMMAND_INVALID", failure);
        }
    }

    @GetMapping("/commands/{key}")
    @Operation(operationId = "admBatchRuntimeCommandState", summary = "BAT Runtime 위험조치 상태 조회")
    ResponseEntity<Map<String, Object>> commandState(@PathVariable String key) {
        try { return ResponseEntity.ok(client.commandState(key)); }
        catch (BatchControlClientException failure) { return error(failure); }
    }

    @PostMapping("/deployment-plans")
    @Operation(operationId = "admBatchRuntimeCreateDeploymentPlan", summary = "BAT 배포 계획 생성")
    ResponseEntity<Map<String, Object>> plan(
            @RequestAttribute("adm.operatorId") String operatorId,
            @RequestBody BatchRuntimeDeploymentPlanRequest body) {
        Map<String, Object> request = Map.of();
        if (body != null) {
            Map<String, Object> mapped = body.toMap();
            if (mapped != null) {
                request = mapped;
            }
        }
        try {
            requireCommandField(request, "reason");
            Object manifest = request.get("manifest");
            if (!(manifest instanceof Map<?, ?> map) || map.isEmpty()) {
                throw new IllegalArgumentException("manifest is required");
            }
            return ResponseEntity.status(201).body(client.createPlan(withServerActor(request, operatorId)));
        } catch (BatchControlClientException failure) {
            return error(failure);
        } catch (IllegalArgumentException failure) {
            return validation("BAT_DEPLOYMENT_PLAN_INVALID", failure);
        } catch (RuntimeException failure) {
            return ResponseEntity.status(503).body(Map.of(
                    "state", "UNKNOWN_RESULT", "errorCode", "BAT_CONTROL_UNREACHABLE",
                    "message", "BAT Control Server 호출 결과를 확정할 수 없습니다."));
        }
    }


    @GetMapping("/retention/policies")
    @Operation(operationId = "admRetentionPolicies", summary = "DB Retention 정책 조회")
    ResponseEntity<Map<String,Object>> retentionPolicies() {
        try { return ResponseEntity.ok(Map.of("items", client.retentionPolicies(), "fetchedAt", Instant.now())); }
        catch (BatchControlClientException failure) { return error(failure); }
    }

    @GetMapping("/retention/runs")
    @Operation(operationId = "admRetentionRuns", summary = "DB Retention 실제 실행 이력 조회")
    ResponseEntity<Map<String,Object>> retentionRuns(@RequestParam(required=false) String policyId,
                                                     @RequestParam(defaultValue="100") int limit) {
        try { return ResponseEntity.ok(Map.of("items", client.retentionRuns(policyId, limit), "fetchedAt", Instant.now())); }
        catch (BatchControlClientException failure) { return error(failure); }
    }

    @PostMapping("/retention/policies")
    @Operation(operationId = "admRetentionPolicySave", summary = "DB Retention 정책 변경 승인 요청",
            description = "Retention 정책 변경은 현재 rowVersion을 immutable approval snapshot으로 고정한 뒤 독립 승인 후 BAT Owner에서 CAS 실행합니다.")
    ResponseEntity<Map<String,Object>> retentionPolicySave(@RequestAttribute("adm.operatorId") String operatorId,
                                                            @Valid @RequestBody RetentionPolicySaveRequest request) {
        try {
            Map<String,Object> requestMap = request.toMap();
            long expectedVersion = request.rowVersion();
            Map<String,Object> payload = new java.util.LinkedHashMap<>(withServerActor(requestMap,operatorId));
            payload.put("expectedVersion", expectedVersion);
            return approvalRequested("retentionPolicySave", "BATCH_RETENTION_POLICY_CHANGE",
                    "bat_retention_policy", request.policyId(), expectedVersion,
                    payload, request.reason(), operatorId);
        } catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("RETENTION_POLICY_INVALID",failure); }
    }

    @PostMapping("/retention/preview")
    @Operation(operationId = "admRetentionPreview", summary = "DB Retention 실행 미리보기",
            description = "파괴 실행 전에 BAT Owner의 실제 Retention handler를 dry-run으로 호출하여 대상 건수와 legal hold 상태를 확인합니다.")
    ResponseEntity<Map<String,Object>> retentionPreview(@RequestAttribute("adm.operatorId") String operatorId,
                                                        @Valid @RequestBody RetentionPreviewRequest request) {
        try {
            return ResponseEntity.ok(client.previewRetention(withServerActor(request.toMap(),operatorId)));
        } catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("RETENTION_PREVIEW_INVALID",failure); }
    }

    @PostMapping("/retention/policies/{policyId}/run")
    @Operation(operationId = "admRetentionRunNow", summary = "DB Retention 실제 수동 실행 승인 요청",
            description = "현재 policy rowVersion을 승인 Snapshot으로 고정하고 독립 승인 완료 후 BAT Owner가 동일 Version에서만 실행합니다.")
    ResponseEntity<Map<String,Object>> retentionRunNow(@PathVariable String policyId,
                                                        @RequestAttribute("adm.operatorId") String operatorId,
                                                        @Valid @RequestBody RetentionReasonRequest request) {
        try {
            CpfDataRow policy = client.retentionPolicy(policyId);
            long expectedVersion = rowVersion(policy);
            Map<String,Object> payload = new java.util.LinkedHashMap<>();
            payload.put("policyId", policyId); payload.put("expectedVersion", expectedVersion);
            payload.put("reason", request.reason());
            return approvalRequested("retentionRunNow", "BATCH_RETENTION_EXECUTE", "bat_retention_policy",
                    policyId, expectedVersion, payload, request.reason(), operatorId);
        } catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("RETENTION_RUN_INVALID",failure); }
    }

    @PostMapping("/retention/runs/{runId}/pause")
    @Operation(operationId = "admRetentionRunPause", summary = "실행 중 Retention 안전 일시정지 요청",
            description = "삭제/보관을 더 진행하지 않게 하는 fail-safe 조치이며 승인 대신 권한·사유·인증 actor를 강제합니다.")
    ResponseEntity<Map<String,Object>> retentionRunPause(@PathVariable String runId,
                                                          @RequestAttribute("adm.operatorId") String operatorId,
                                                          @Valid @RequestBody RetentionVersionedReasonRequest request) {
        try {
            return ResponseEntity.ok(client.pauseRetentionRun(runId, request.expectedVersion(), request.reason()));
        } catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("RETENTION_PAUSE_INVALID",failure); }
    }

    @PostMapping("/retention/runs/{runId}/resume")
    @Operation(operationId = "admRetentionRunResume", summary = "Retention Run 재개 승인 요청")
    ResponseEntity<Map<String,Object>> retentionRunResume(@PathVariable String runId,
                                                           @RequestAttribute("adm.operatorId") String operatorId,
                                                           @Valid @RequestBody RetentionVersionedReasonRequest request) {
        try {
            CpfDataRow run = client.retentionRun(runId);
            String policyId = rowText(run, "policyId", "policy_id");
            long expectedVersion = request.expectedVersion();
            Map<String,Object> payload = Map.of("runId",runId,"policyId",policyId,
                    "expectedVersion",expectedVersion,"reason",request.reason());
            return approvalRequested("retentionRunResume", "BATCH_RETENTION_RESUME", "bat_retention_run",
                    runId, expectedVersion, payload, request.reason(), operatorId);
        } catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("RETENTION_RESUME_INVALID",failure); }
    }

    @PostMapping("/retention/policies/{policyId}/pause")
    @Operation(operationId = "admRetentionPolicyPause", summary = "Retention Schedule 안전 일시정지",
            description = "향후 실행을 막는 fail-safe 조치이며 현재 rowVersion CAS와 사유를 강제합니다.")
    ResponseEntity<Map<String,Object>> retentionPolicyPause(@PathVariable String policyId,
                                                             @RequestAttribute("adm.operatorId") String operatorId,
                                                             @Valid @RequestBody RetentionVersionedReasonRequest request) {
        try {
            return ResponseEntity.ok(client.pauseRetentionPolicy(policyId, request.expectedVersion(), request.reason()));
        } catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("RETENTION_POLICY_PAUSE_INVALID",failure); }
    }

    @PostMapping("/retention/policies/{policyId}/resume")
    @Operation(operationId = "admRetentionPolicyResume", summary = "Retention Schedule 재개 승인 요청")
    ResponseEntity<Map<String,Object>> retentionPolicyResume(@PathVariable String policyId,
                                                              @RequestAttribute("adm.operatorId") String operatorId,
                                                              @Valid @RequestBody RetentionVersionedReasonRequest request) {
        try {
            long expectedVersion = request.expectedVersion();
            Map<String,Object> payload = Map.of("policyId",policyId,"expectedVersion",expectedVersion,
                    "reason",request.reason());
            return approvalRequested("retentionPolicyResume", "BATCH_RETENTION_POLICY_RESUME",
                    "bat_retention_policy", policyId, expectedVersion, payload,
                    request.reason(), operatorId);
        } catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("RETENTION_POLICY_RESUME_INVALID",failure); }
    }

    private ResponseEntity<Map<String,Object>> approvalRequested(String ownerCommand, String actionType,
                                                                  String targetType, String targetId,
                                                                  long expectedVersion, Map<String,Object> payload,
                                                                  String reason, String operatorId) {
        if (expectedVersion < 0) throw new IllegalArgumentException("expectedVersion must be non-negative");
        try {
            Map<String,Object> snapshot = new java.util.LinkedHashMap<>(payload);
            snapshot.put("expectedVersion", expectedVersion);
            String canonicalSnapshot = objectMapper.writeValueAsString(new java.util.TreeMap<>(snapshot));
            String requestKey = retentionRequestKey(actionType, targetType, targetId, expectedVersion,
                    operatorId, reason, canonicalSnapshot);
            AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                    requestKey, null, null, actionType, "BAT", ownerCommand, targetType, targetId,
                    canonicalSnapshot, Instant.now().plusSeconds(900), reason);
            return ResponseEntity.accepted().body(approvalService.requestApproval(request, operatorId));
        } catch (JsonProcessingException invalid) {
            throw new IllegalArgumentException("Retention approval snapshot 직렬화에 실패했습니다.", invalid);
        }
    }


    private static String retentionRequestKey(String actionType, String targetType, String targetId, long expectedVersion,
                                              String operatorId, String reason, String canonicalSnapshot) {
        String material = String.join("\n", actionType, targetType, targetId, String.valueOf(expectedVersion),
                operatorId, reason, canonicalSnapshot);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
            return "RET-" + HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static long rowVersion(Map<String,Object> row) {
        Object value = row.containsKey("rowVersion") ? row.get("rowVersion") : row.get("row_version");
        if (!(value instanceof Number number) || number.longValue() < 0) {
            try {
                long parsed = Long.parseLong(String.valueOf(value));
                if (parsed >= 0) return parsed;
            } catch (RuntimeException ignored) { }
            throw new IllegalArgumentException("Retention rowVersion is missing or invalid");
        }
        return number.longValue();
    }

    private static String rowText(Map<String,Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null && !String.valueOf(value).isBlank()) return String.valueOf(value).trim();
        }
        throw new IllegalArgumentException("Retention row field is missing: " + String.join("/", keys));
    }

    private static Map<String, Object> withServerActor(Map<String, Object> request, String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw new IllegalArgumentException("authenticated operator is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        Map<String, Object> command = sanitizeCommandMap(request);
        command.put("requestedBy", operatorId);
        return java.util.Collections.unmodifiableMap(command);
    }

    /**
     * Browser가 보낸 Actor alias는 깊이에 관계없이 제거하고 BAT Owner에는 인증 Session의
     * {@code requestedBy} 하나만 전달합니다. JSON 요청은 순환 참조를 만들 수 없으므로 재귀 복사하며,
     * 반환된 중첩 Collection도 불변으로 고정해 후속 Client가 Actor 값을 다시 주입하지 못하게 합니다.
     */
    private static Map<String, Object> sanitizeCommandMap(Map<?, ?> source) {
        Map<String, Object> sanitized = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String key) || key.isBlank()) {
                throw new IllegalArgumentException("batch command field name must be a non-blank string");
            }
            if (CLIENT_ACTOR_FIELDS.contains(key)) {
                continue;
            }
            sanitized.put(key, sanitizeCommandValue(entry.getValue()));
        }
        return sanitized;
    }

    private static Object sanitizeCommandValue(Object value) {
        if (value instanceof Map<?, ?> nested) {
            return java.util.Collections.unmodifiableMap(sanitizeCommandMap(nested));
        }
        if (value instanceof List<?> list) {
            return list.stream().map(BatchRuntimeControlController::sanitizeCommandValue).toList();
        }
        return value;
    }


    private static long requireVersion(Map<String, Object> request, String field) {
        Object value = request.get(field);
        if (value instanceof Number number && number.longValue() >= 0) return number.longValue();
        try {
            long parsed = Long.parseLong(String.valueOf(value));
            if (parsed >= 0) return parsed;
        } catch (RuntimeException ignored) { }
        throw new IllegalArgumentException(field + " must be a non-negative number");
    }

    private static void requireApprovalForRiskState(Map<String, Object> request) {
        String state = String.valueOf(request.get("targetState")).toUpperCase(java.util.Locale.ROOT);
        if (Set.of("PUBLISHED", "DEPLOYED", "DEPRECATED", "RETIRED").contains(state)) {
            requireCommandField(request, "approvalRequestId");
        }
    }


    private static void requireCommandField(Map<String, Object> request, String field) {
        Object value = request.get(field);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
    private ResponseEntity<Map<String,Object>> validation(String errorCode, IllegalArgumentException failure) {
        return ResponseEntity.badRequest().body(Map.of(
                "state", "FAILED",
                "errorCode", errorCode,
                "message", failure.getMessage() == null ? "Invalid batch control request" : failure.getMessage()));
    }
    private ResponseEntity<Map<String,Object>> error(BatchControlClientException failure){return ResponseEntity.status(status(failure)).body(errorBody(failure));}
    private int status(BatchControlClientException failure){return switch(failure.category()){case VALIDATION->400;case PERMISSION->403;case NOT_FOUND->404;case CONFLICT->409;case
            UNKNOWN_RESULT->502;case UNAVAILABLE->503;case OWNER_ERROR->500;};}
    private Map<String,Object> errorBody(BatchControlClientException failure){Map<String,Object> body=new java.util.LinkedHashMap<>();body.put("state",
            failure.category()==BatchControlClientException.Category.UNKNOWN_RESULT?"UNKNOWN_RESULT":"FAILED");body.put("errorCode",failure.errorCode());body.put("message",
            failure.getMessage());if(failure.traceId()!=null)body.put("traceId",failure.traceId());return body;}
}
