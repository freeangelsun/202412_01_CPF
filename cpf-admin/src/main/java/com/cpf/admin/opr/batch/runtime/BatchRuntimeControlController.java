package com.cpf.admin.opr.batch.runtime;

import com.cpf.admin.common.base.AdmBaseController;
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
@RestController
@RequestMapping("/adm/api/batch-runtime")
@Tag(name = "ADM-Batch-Runtime", description = "BAT Control Server 기반 Runtime 운영 API")
public class BatchRuntimeControlController extends AdmBaseController {
    private static final Set<String> ALLOWED_VIEWS = Set.of(
            "overview", "instances", "scheduler", "worker-pools", "center-cut", "agents",
            "job-packs", "executions", "deployments", "recovery", "leases", "alerts", "audit");

    private final BatchRuntimeControlClient client;

    public BatchRuntimeControlController(BatchRuntimeControlClient client) {
        this.client = client;
    }

    @GetMapping("/instances")
    @Operation(operationId = "admBatchRuntimeInstances", summary = "BAT Runtime Instance 조회")
    Map<String, Object> instances(@RequestParam(defaultValue = "30") long staleAfterSeconds) {
        Instant fetchedAt = Instant.now();
        try {
            return Map.of("fetchedAt", fetchedAt, "stale", false, "partial", false,
                    "items", client.instances(Math.max(5, staleAfterSeconds)));
        } catch (RuntimeException failure) {
            return Map.of("fetchedAt", fetchedAt, "stale", true, "partial", true,
                    "items", List.of(), "errorCode", "BAT_CONTROL_UNREACHABLE");
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
        catch (BatchControlClientException failure) { Map<String,Object> body=new java.util.LinkedHashMap<>(errorBody(failure));body.put("valid",false);body.put("errors",List.of(failure.getMessage()));return ResponseEntity.status(status(failure)).body(body); }
    }

    @PostMapping("/job-definitions/drafts")
    @Operation(operationId = "admBatchJobDefinitionSave", summary = "Batch Job Definition Draft 저장")
    ResponseEntity<Map<String, Object>> saveJobDefinition(@RequestBody Map<String, Object> request) {
        try { return ResponseEntity.status(201).body(client.saveJobDefinition(request)); }
        catch (BatchControlClientException failure) { return error(failure); }
    }

    @PostMapping("/job-definitions/{jobId}/versions/{version}/transition")
    @Operation(operationId = "admBatchJobDefinitionTransition", summary = "Batch Job Definition 승인·배포 상태 전환")
    ResponseEntity<Map<String, Object>> transitionJobDefinition(@PathVariable String jobId, @PathVariable long version,
                                                                 @RequestBody Map<String, Object> request) {
        try { return ResponseEntity.ok(client.transitionJobDefinition(jobId, version, request)); }
        catch (BatchControlClientException failure) { return error(failure); }
    }

    @PostMapping("/deployment-plans")
    @Operation(operationId = "admBatchRuntimeCreateDeploymentPlan", summary = "BAT 배포 계획 생성")
    ResponseEntity<Map<String, Object>> plan(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.status(201).body(client.createPlan(request));
        } catch (RuntimeException failure) {
            return ResponseEntity.status(503).body(Map.of(
                    "state", "UNKNOWN_RESULT", "errorCode", "BAT_CONTROL_UNREACHABLE"));
        }
    }

    private ResponseEntity<Map<String,Object>> error(BatchControlClientException failure){return ResponseEntity.status(status(failure)).body(errorBody(failure));}
    private int status(BatchControlClientException failure){return switch(failure.category()){case VALIDATION->400;case PERMISSION->403;case NOT_FOUND->404;case CONFLICT->409;case UNKNOWN_RESULT->502;case UNAVAILABLE->503;case OWNER_ERROR->500;};}
    private Map<String,Object> errorBody(BatchControlClientException failure){Map<String,Object> body=new java.util.LinkedHashMap<>();body.put("state",failure.category()==BatchControlClientException.Category.UNKNOWN_RESULT?"UNKNOWN_RESULT":"FAILED");body.put("errorCode",failure.errorCode());body.put("message",failure.getMessage());if(failure.traceId()!=null)body.put("traceId",failure.traceId());return body;}
}
