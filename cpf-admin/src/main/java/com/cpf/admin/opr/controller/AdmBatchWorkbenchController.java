package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmBatchOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Batch 전용 운영 Workbench를 위한 Server Paging·상세 집계 API입니다.
 *
 * <p>원본 Batch 제어 API를 대체하지 않고, ADM 화면이 여러 Owner Query를 임의로 조합하지
 * 않도록 조회 경계를 제공합니다. 위험 조치는 기존 명시적 Command API를 사용합니다.</p>
 */
@RestController
@RequestMapping("/adm/api/batch/workbench")
@Tag(name = "ADM-Batch-Workbench", description = "Batch 상용 운영 화면 조회 API")
public class AdmBatchWorkbenchController extends com.cpf.admin.common.base.AdmBaseController {

    private final AdmBatchOperationService operations;
    public AdmBatchWorkbenchController(AdmBatchOperationService operations) {
        this.operations = operations;
    }

    @GetMapping("/executions")    @Operation(operationId = "admBatchWorkbenchExecutions", summary = "배치 실행 Workbench 목록", description = "BAT Owner의 Paging 계약으로 실행 이력을 조회합니다.")
    public ResponseEntity<Map<String, Object>> executions(
            @RequestParam(required = false) String jobId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) Long springBatchJobInstanceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workerId,
            @RequestParam(required = false) String instanceId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(operations.findExecutionPage(
                jobId, transactionId, springBatchJobInstanceId, workerId, instanceId,
                status, fromDate, toDate, page, size));
    }

    @GetMapping("/executions/{executionId}")    @Operation(operationId = "admBatchWorkbenchExecutionDetail", summary = "배치 실행 상세 Workspace", description = "실행 상세, Step Timeline, 작업 이력을 하나의 응답으로 제공합니다.")
    public ResponseEntity<Map<String, Object>> execution(@PathVariable long executionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("execution", operations.findExecutionDetail(executionId));
        result.put("steps", operations.findStepExecutions(executionId, null, 1000));
        result.put("operations", operations.findOperationLogs(null, executionId, 500));
        result.put("fetchedAt", Instant.now().toString());
        result.put("partial", false);
        result.put("stale", false);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/jobs")    @Operation(operationId = "admBatchWorkbenchJobs", summary = "배치 Job Workbench 목록", description = "BAT Owner의 Paging 계약으로 Job과 실행 통계를 조회합니다.")
    public ResponseEntity<Map<String, Object>> jobs(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "job_id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(operations.findJobPage(query, page, size, sort, direction));
    }

    @GetMapping("/jobs/{jobId}")    @Operation(operationId = "admBatchWorkbenchJobDetail", summary = "배치 Job 상세 Workspace", description = "정의·스케줄·DAG 관계·실행·대상·Lock을 통합 조회합니다.")
    public ResponseEntity<Map<String, Object>> job(@PathVariable String jobId) {
        Map<String, Object> detail = new LinkedHashMap<>(operations.findJobDetail(jobId));
        detail.putIfAbsent("relations", operations.findRelations(jobId));
        detail.putIfAbsent("targets", operations.findExecutionTargets(jobId, null, 500));
        detail.putIfAbsent("locks", operations.findLocks(jobId));
        detail.put("fetchedAt", Instant.now().toString());
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/schedules")    @Operation(operationId = "admBatchWorkbenchSchedules", summary = "Scheduler HA Workbench 목록", description = "BAT Owner의 Paging 계약으로 스케줄과 실행 영향을 조회합니다.")
    public ResponseEntity<Map<String, Object>> schedules(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "next_fire_time") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(operations.findSchedulePage(query, page, size, sort, direction));
    }

    @GetMapping("/infrastructure")    @Operation(operationId = "admBatchWorkbenchInfrastructure", summary = "Batch 인프라 통합 조회", description = "Instance·Worker·실행 대상을 통합하고 heartbeat stale 상태를 보존합니다.")
    public ResponseEntity<Map<String, Object>> infrastructure(
            @RequestParam(defaultValue = "120") int heartbeatTimeoutSeconds,
            @RequestParam(defaultValue = "500") int limit) {
        Map<String, Object> result = new LinkedHashMap<>(operations.findInfrastructureSnapshot(heartbeatTimeoutSeconds, limit));
        result.put("fetchedAt", Instant.now().toString());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recovery")    @Operation(operationId = "admBatchWorkbenchRecovery", summary = "Recovery·Unknown Result 통합 조회", description = "Ghost 후보, Lease/Lock, 최근 운영 조치를 함께 조회합니다.")
    public ResponseEntity<Map<String, Object>> recovery(
            @RequestParam(defaultValue = "120") int heartbeatTimeoutSeconds,
            @RequestParam(defaultValue = "500") int limit) {
        Map<String, Object> result = new LinkedHashMap<>(operations.findRecoverySnapshot(heartbeatTimeoutSeconds, limit));
        result.put("fetchedAt", Instant.now().toString());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/overview")    @Operation(operationId = "admBatchWorkbenchOverview", summary = "Batch 운영 Dashboard", description = "Job·Schedule·Execution·Worker·Recovery 상태를 역할별 KPI로 제공합니다.")
    public ResponseEntity<Map<String, Object>> overview() {
        List<Map<String, Object>> jobs = operations.findJobs();
        List<Map<String, Object>> schedules = operations.findSchedules();
        List<Map<String, Object>> executions = operations.findExecutions(null, null, null, null, null, 500);
        List<Map<String, Object>> workers = operations.findWorkers(120);
        List<Map<String, Object>> ghosts = operations.findGhostCandidates(120);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobCount", jobs.size());
        result.put("scheduleCount", schedules.size());
        result.put("executionCount", executions.size());
        result.put("failedExecutionCount", countStatus(executions, "FAIL", "FAILED", "ERROR"));
        result.put("runningExecutionCount", countStatus(executions, "RUNNING", "STARTED", "EXECUTING"));
        result.put("workerCount", workers.size());
        result.put("staleWorkerCount", countStatus(workers, "STALE", "DOWN", "UNAVAILABLE"));
        result.put("unknownResultCount", ghosts.size());
        result.put("recentExecutions", executions.stream().limit(20).toList());
        result.put("fetchedAt", Instant.now().toString());
        return ResponseEntity.ok(result);
    }


    private static long countStatus(List<Map<String, Object>> rows, String... expected) {
        Set<String> statuses = Set.of(expected);
        return rows.stream().filter(row -> {
            Object value = row.get("status");
            if (value == null) value = row.get("execution_status");
            if (value == null) value = row.get("executionStatus");
            return value != null && statuses.contains(String.valueOf(value).toUpperCase());
        }).count();
    }
}
