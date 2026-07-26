package com.cpf.admin.opr.batch.runtime;

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
public class BatchRuntimeControlController {
    private static final Set<String> ALLOWED_VIEWS = Set.of(
            "overview", "instances", "scheduler", "worker-pools", "center-cut", "agents",
            "job-packs", "executions", "deployments", "recovery", "leases", "alerts", "audit");

    private final BatchRuntimeControlClient client;

    public BatchRuntimeControlController(BatchRuntimeControlClient client) {
        this.client = client;
    }

    @GetMapping("/instances")
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

    @PostMapping("/deployment-plans")
    ResponseEntity<Map<String, Object>> plan(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.status(201).body(client.createPlan(request));
        } catch (RuntimeException failure) {
            return ResponseEntity.status(503).body(Map.of(
                    "state", "UNKNOWN_RESULT", "errorCode", "BAT_CONTROL_UNREACHABLE"));
        }
    }
}
