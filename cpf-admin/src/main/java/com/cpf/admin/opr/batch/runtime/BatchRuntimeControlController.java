package com.cpf.admin.opr.batch.runtime;

import com.cpf.web.api.CpfController;
import com.cpf.admin.common.base.AdmBaseController;
import com.cpf.admin.approval.service.AdmApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ADM Batch Control Plane facade. 조회 실패를 정상 빈 목록으로 위장하지 않고 stale/partial로 반환합니다.
 */
@CpfController
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

    public BatchRuntimeControlController(BatchRuntimeControlClient client, AdmApprovalService approvalService) {
        this.client = client;
        this.approvalService = approvalService;
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
    ResponseEntity<Map<String, Object>> validateJobDefinition(@RequestBody Map<String, Object> request) {
        try { return ResponseEntity.ok(client.validateJobDefinition(request)); }
        catch (BatchControlClientException failure) { Map<String,Object> body=new java.util.LinkedHashMap<>(errorBody(failure));body.put("valid",false);body.put("errors",
                List.of(failure.getMessage()));return ResponseEntity.status(status(failure)).body(body); }
    }

    @PostMapping("/job-definitions/drafts")
    @Operation(operationId = "admBatchJobDefinitionSave", summary = "Batch Job Definition Draft 저장")
    ResponseEntity<Map<String, Object>> saveJobDefinition(
            @RequestAttribute("adm.operatorId") String operatorId,
            @RequestBody Map<String, Object> request) {
        try {
            requireCommandField(request, "jobId");
            requireCommandField(request, "reason");
            return ResponseEntity.status(201).body(client.saveJobDefinition(withServerActor(request, operatorId)));
        }
        catch (BatchControlClientException failure) { return error(failure); }
        catch (IllegalArgumentException failure) { return validation("BAT_JOB_DEFINITION_INVALID", failure); }
    }

    @PostMapping("/job-definitions/{jobId}/versions/{version}/transition")
    @Operation(operationId = "admBatchJobDefinitionTransition", summary = "Batch Job Definition 승인·배포 상태 전환")
    ResponseEntity<Map<String, Object>> transitionJobDefinition(
            @RequestAttribute("adm.operatorId") String operatorId,
            @PathVariable String jobId, @PathVariable long version,
            @RequestBody Map<String, Object> request) {
        try {
            requireCommandField(request, "targetState");
            requireCommandField(request, "reason");
            requireExpectedVersion(request);
            requireApprovalForRiskState(request);
            return ResponseEntity.ok(client.transitionJobDefinition(jobId, version, withServerActor(request, operatorId)));
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
        Map<String, Object> request = body == null ? null : body.toMap();
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

    private static void requireExpectedVersion(Map<String, Object> request) {
        Object value = request.get("expectedVersion");
        if (!(value instanceof Number number) || number.longValue() < 0) {
            throw new IllegalArgumentException("expectedVersion must be a non-negative number");
        }
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
