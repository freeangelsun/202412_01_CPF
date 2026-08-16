package com.cpf.batch.control;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Center-Cut 실패/결과불명 재처리는 자동 Replay가 아니라 승인된 실행 단위 조치로만 수행합니다.
 */
@RestController
@RequestMapping("/api/v1/batch/center-cut")
public class CenterCutReconciliationController {
    private static final int MAX_REASON_LENGTH = 512;

    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final BatVerifiedActorResolver actorResolver;

    public CenterCutReconciliationController(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            BatVerifiedActorResolver actorResolver) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.sql = Objects.requireNonNull(sqlCatalogProvider, "sqlCatalogProvider").forModule("bat");
        this.actorResolver = Objects.requireNonNull(actorResolver, "actorResolver");
    }

    /** Read-only observation endpoint used by ADM UNKNOWN reconciliation. */
    @GetMapping("/executions/{executionId}/reconciliation-status")
    public ResponseEntity<Map<String, Object>> reconciliationStatus(
            @PathVariable String executionId, HttpServletRequest http) {
        String safeExecutionId = requiredIdentifier(executionId, "executionId");
        actorResolver.identity(http);
        return ResponseEntity.ok(execution(safeExecutionId));
    }

    @PostMapping("/executions/{executionId}/reprocess-failed")
    @Transactional
    public ResponseEntity<Map<String, Object>> failedExecution(
            @PathVariable String executionId,
            @RequestBody ApprovedRequest request,
            HttpServletRequest http) {
        String safeExecutionId = requiredIdentifier(executionId, "executionId");
        String reason = requiredReason(request);
        var actors = approved(http, request);
        Map<String, Object> execution = execution(safeExecutionId);
        int changed = jdbc.update(sql.required("centercut-reconcile-failed-items"), safeExecutionId);
        requireChangedItems(changed, "CENTER_CUT_FAILED_ITEMS_NOT_FOUND");
        int executionChanged = jdbc.update(
                sql.required("centercut-reconcile-failed-execution"),
                changed,
                changed,
                safeExecutionId);
        requireExecutionTransition(
                executionChanged,
                "CENTER_CUT_FAILED_EXECUTION_STATE_CONFLICT");
        audit(
                requiredJobId(execution),
                safeExecutionId,
                "REPROCESS_FAILED",
                actors,
                reason,
                changed);
        return ResponseEntity.accepted().body(
                Map.of("executionId", safeExecutionId, "requeued", changed));
    }

    @PostMapping("/executions/{executionId}/reconcile-unknown")
    @Transactional
    public ResponseEntity<Map<String, Object>> unknownExecution(
            @PathVariable String executionId,
            @RequestBody ApprovedRequest request,
            HttpServletRequest http) {
        String safeExecutionId = requiredIdentifier(executionId, "executionId");
        String reason = requiredReason(request);
        var actors = approved(http, request);
        Map<String, Object> execution = execution(safeExecutionId);
        int changed = jdbc.update(sql.required("centercut-reconcile-unknown-items"), safeExecutionId);
        requireChangedItems(changed, "CENTER_CUT_UNKNOWN_ITEMS_NOT_FOUND");
        int executionChanged = jdbc.update(
                sql.required("centercut-reconcile-unknown-execution"),
                changed,
                changed,
                safeExecutionId);
        requireExecutionTransition(
                executionChanged,
                "CENTER_CUT_UNKNOWN_EXECUTION_STATE_CONFLICT");
        audit(
                requiredJobId(execution),
                safeExecutionId,
                "RECONCILE_UNKNOWN",
                actors,
                reason,
                changed);
        return ResponseEntity.accepted().body(
                Map.of("executionId", safeExecutionId, "requeued", changed));
    }

    /**
     * Job 단위 일괄 전이는 여러 Execution 상태를 원자적으로 보존할 수 없으므로 fail-closed 합니다.
     * 기존 Route는 유지해 호출자가 안전한 execution-scope API로 전환할 수 있도록 409를 반환합니다.
     */
    @PostMapping("/jobs/{jobId}/reprocess-failed")
    public ResponseEntity<Map<String, Object>> failedJob(
            @PathVariable String jobId,
            @RequestBody ApprovedRequest request,
            HttpServletRequest http) {
        requiredIdentifier(jobId, "jobId");
        requiredReason(request);
        approved(http, request);
        throw conflict("CENTER_CUT_JOB_SCOPE_REPROCESS_DISABLED_USE_EXECUTION_SCOPE");
    }

    /**
     * UNKNOWN은 각 Execution의 확인·승인·감사를 분리해야 하므로 실행 단위 API만 허용합니다.
     */
    @PostMapping("/jobs/{jobId}/reconcile-unknown")
    public ResponseEntity<Map<String, Object>> unknownJob(
            @PathVariable String jobId,
            @RequestBody ApprovedRequest request,
            HttpServletRequest http) {
        requiredIdentifier(jobId, "jobId");
        requiredReason(request);
        approved(http, request);
        throw conflict("CENTER_CUT_JOB_SCOPE_RECONCILE_DISABLED_USE_EXECUTION_SCOPE");
    }

    private static void requireChangedItems(int changed, String code) {
        if (changed < 1) {
            throw conflict(code);
        }
    }

    private static void requireExecutionTransition(int changed, String code) {
        if (changed != 1) {
            /*
             * RuntimeException으로 @Transactional 전체를 롤백합니다. 앞선 Item RETRY 전이만
             * 커밋되고 Execution이 UNKNOWN/Terminal로 남는 부분 전이를 방지합니다.
             */
            throw conflict(code);
        }
    }

    private static ResponseStatusException badRequest(String code) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, code);
    }

    private static ResponseStatusException conflict(String code) {
        return new ResponseStatusException(HttpStatus.CONFLICT, code);
    }

    private static ResponseStatusException notFound(String code) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, code);
    }

    private static ResponseStatusException unavailable(String code) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, code);
    }

    private Map<String, Object> execution(String id) {
        try {
            return jdbc.queryForMap(sql.required("centercut-reconcile-load-execution"), id);
        } catch (EmptyResultDataAccessException missing) {
            throw notFound("CENTER_CUT_EXECUTION_NOT_FOUND");
        }
    }

    private BatVerifiedActorResolver.ApprovedActors approved(
            HttpServletRequest http,
            ApprovedRequest request) {
        return actorResolver.approved(
                http,
                request.requestedBy(),
                request.approvedBy(),
                null);
    }

    private void audit(
            String jobId,
            String executionId,
            String operation,
            BatVerifiedActorResolver.ApprovedActors actors,
            String reason,
            int count) {
        int written = jdbc.update(
                sql.required("centercut-reconcile-audit"),
                jobId,
                operation,
                actors.requestedBy(),
                SensitiveTextSanitizer.sanitize(reason),
                "executionId=" + executionId + ",count=" + count
                        + ",approvalRequestId=" + actors.approvalRequestId()
                        + ",approvedBy=" + actors.approvedBy(),
                actors.approvedBy(),
                actors.approvedBy());
        if (written != 1) {
            throw unavailable("CENTER_CUT_RECONCILE_AUDIT_PERSISTENCE_FAILED");
        }
    }

    private static String requiredIdentifier(String value, String field) {
        if (value == null || value.isBlank()) {
            throw badRequest("CENTER_CUT_" + field.toUpperCase() + "_REQUIRED");
        }
        String normalized = value.trim();
        if (normalized.length() > 128 || containsControl(normalized)) {
            throw badRequest("CENTER_CUT_" + field.toUpperCase() + "_INVALID");
        }
        return normalized;
    }

    private static String requiredReason(ApprovedRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw badRequest("CENTER_CUT_RECONCILE_REASON_REQUIRED");
        }
        String reason = request.reason().trim();
        if (reason.length() > MAX_REASON_LENGTH || containsControl(reason)) {
            throw badRequest("CENTER_CUT_RECONCILE_REASON_INVALID");
        }
        return reason;
    }

    private static String requiredJobId(Map<String, Object> execution) {
        String jobId = Objects.toString(execution.get("center_cut_job_id"), "").trim();
        if (jobId.isEmpty() || containsControl(jobId)) {
            throw unavailable("CENTER_CUT_EXECUTION_JOB_ID_MISSING");
        }
        return jobId;
    }

    private static boolean containsControl(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public record ApprovedRequest(String requestedBy, String approvedBy, String reason) {
    }
}
