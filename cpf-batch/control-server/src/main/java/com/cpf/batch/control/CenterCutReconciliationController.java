package com.cpf.batch.control;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Center-Cut의 실패/결과불명 재처리는 자동 Replay가 아니라 승인된 Owner 조치로만 수행합니다.
 */
@RestController
@RequestMapping("/api/v1/batch/center-cut")
public class CenterCutReconciliationController {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final BatVerifiedActorResolver actorResolver;

    public CenterCutReconciliationController(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            BatVerifiedActorResolver actorResolver) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
        this.actorResolver = actorResolver;
    }

    @PostMapping("/executions/{executionId}/reprocess-failed")
    @Transactional
    public ResponseEntity<Map<String, Object>> failedExecution(@PathVariable String executionId,
                                                                @RequestBody ApprovedRequest request,
                                                                HttpServletRequest http) {
        var actors = approved(http, request);
        Map<String, Object> execution = execution(executionId);
        int changed = jdbc.update(sql.required("centercut-reconcile-failed-items"), executionId);
        jdbc.update(sql.required("centercut-reconcile-failed-execution"),
                changed, changed, executionId);
        audit(String.valueOf(execution.get("center_cut_job_id")), executionId,
                "REPROCESS_FAILED", actors, request.reason(), changed);
        return ResponseEntity.accepted().body(Map.of("executionId", executionId, "requeued", changed));
    }

    @PostMapping("/executions/{executionId}/reconcile-unknown")
    @Transactional
    public ResponseEntity<Map<String, Object>> unknownExecution(@PathVariable String executionId,
                                                                 @RequestBody ApprovedRequest request,
                                                                 HttpServletRequest http) {
        var actors = approved(http, request);
        Map<String, Object> execution = execution(executionId);
        int changed = jdbc.update(sql.required("centercut-reconcile-unknown-items"), executionId);
        jdbc.update(sql.required("centercut-reconcile-unknown-execution"),
                changed, changed, executionId);
        audit(String.valueOf(execution.get("center_cut_job_id")), executionId,
                "RECONCILE_UNKNOWN", actors, request.reason(), changed);
        return ResponseEntity.accepted().body(Map.of("executionId", executionId, "requeued", changed));
    }

    /** 기존 Job 단위 API는 같은 승인 계약으로 유지하되 신규 운영 화면은 execution API를 사용합니다. */
    @PostMapping("/jobs/{jobId}/reprocess-failed")
    @Transactional
    public ResponseEntity<Map<String, Object>> failedJob(@PathVariable String jobId,
                                                          @RequestBody ApprovedRequest request,
                                                          HttpServletRequest http) {
        var actors = approved(http, request);
        int changed = jdbc.update(sql.required("centercut-reconcile-failed-job"), jobId);
        audit(jobId, null, "REPROCESS_FAILED_JOB", actors, request.reason(), changed);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId, "requeued", changed));
    }

    @PostMapping("/jobs/{jobId}/reconcile-unknown")
    @Transactional
    public ResponseEntity<Map<String, Object>> unknownJob(@PathVariable String jobId,
                                                           @RequestBody ApprovedRequest request,
                                                           HttpServletRequest http) {
        var actors = approved(http, request);
        int changed = jdbc.update(sql.required("centercut-reconcile-unknown-job"), jobId);
        audit(jobId, null, "RECONCILE_UNKNOWN_JOB", actors, request.reason(), changed);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId, "requeued", changed));
    }

    private Map<String, Object> execution(String id) {
        return jdbc.queryForMap(sql.required("centercut-reconcile-load-execution"), id);
    }

    private BatVerifiedActorResolver.ApprovedActors approved(
            HttpServletRequest http,
            ApprovedRequest request) {
        if (request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("requester/approver separation and reason required");
        }
        return actorResolver.approved(http,request.requestedBy(),request.approvedBy(),null);
    }

    private void audit(String jobId, String executionId, String operation,
                       BatVerifiedActorResolver.ApprovedActors actors,
                       String reason,
                       int count) {
        jdbc.update(sql.required("centercut-reconcile-audit"),
                jobId, operation, actors.requestedBy(),
                SensitiveTextSanitizer.sanitize(reason),
                "executionId=" + executionId + ",count=" + count
                        + ",approvalRequestId=" + actors.approvalRequestId()
                        + ",approvedBy=" + actors.approvedBy(),
                actors.approvedBy(), actors.approvedBy());
    }

    public record ApprovedRequest(String requestedBy, String approvedBy, String reason) {}
}
