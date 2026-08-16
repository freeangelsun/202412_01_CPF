package com.cpf.batch.control;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Scheduler Trigger가 UNKNOWN인 경우 승인된 운영 판단 뒤에만 재실행 가능한 FAILED 상태로 전환합니다.
 *
 * <p>일반 Scheduler dispatch SQL은 UNKNOWN을 절대 자동 선택하지 않습니다. 이 Controller는
 * 승인된 ADM 호출, 원 Trigger idempotency key, attempt CAS와 별도 reconciliation idempotency key를
 * 함께 검증하고 Audit 저장까지 같은 트랜잭션으로 완료한 경우에만 상태를 전환합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/batch/scheduler")
public class SchedulerTriggerReconciliationController {
    private static final String RECONCILED_PREFIX = "RECONCILED_RETRY_";

    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final BatVerifiedActorResolver actorResolver;

    public SchedulerTriggerReconciliationController(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            BatVerifiedActorResolver actorResolver) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
        this.actorResolver = actorResolver;
    }

    @GetMapping("/schedules/{scheduleId}/triggers")
    public ResponseEntity<Map<String, Object>> triggerState(
            @PathVariable String scheduleId,
            @RequestParam Instant scheduledFireAt) {
        validateScheduleId(scheduleId);
        if (scheduledFireAt == null) throw new IllegalArgumentException("scheduledFireAt is required");
        return ResponseEntity.ok(trigger(scheduleId, scheduledFireAt));
    }

    @PostMapping("/schedules/{scheduleId}/triggers/reconcile-unknown/retry")
    @Transactional
    public ResponseEntity<Map<String, Object>> retryUnknown(
            @PathVariable String scheduleId,
            @RequestBody RetryUnknownRequest request,
            HttpServletRequest http) {
        validateScheduleId(scheduleId);
        validate(request);
        var actors = actorResolver.approved(
                http,
                request.requestedBy(),
                request.approvedBy(),
                request.approvalRequestId());
        String entityKey = entityKey(scheduleId, request.scheduledFireAt());

        Map<String, Object> priorAudit = auditByIdempotencyKey(request.idempotencyKey());
        if (priorAudit != null) {
            assertAuditReplay(priorAudit, entityKey, request, actors);
            return accepted(scheduleId, request, actors, true);
        }

        Map<String, Object> before = trigger(scheduleId, request.scheduledFireAt());
        assertRetryableUnknown(before, request);
        String reconciliationCode = reconciliationCode(scheduleId, request, actors);

        int changed = jdbc.update(
                sql.required("scheduler-trigger-reconcile-unknown-retry"),
                reconciliationCode,
                scheduleId,
                Timestamp.from(request.scheduledFireAt()),
                request.expectedTriggerIdempotencyKey(),
                request.expectedAttemptCount());
        if (changed != 1) {
            throw conflict("BATCH_SCHEDULER_TRIGGER_RECONCILE_CONFLICT");
        }

        int audited = jdbc.update(
                sql.required("scheduler-trigger-reconcile-audit"),
                actors.approvalRequestId(),
                entityKey,
                actors.requestedBy(),
                actors.approvedBy(),
                request.reason().trim(),
                request.idempotencyKey().trim(),
                request.expectedAttemptCount());
        if (audited != 1) {
            throw new IllegalStateException("BATCH_SCHEDULER_TRIGGER_RECONCILE_AUDIT_REJECTED");
        }
        return accepted(scheduleId, request, actors, false);
    }

    private Map<String, Object> trigger(String scheduleId, Instant scheduledFireAt) {
        try {
            return jdbc.queryForMap(
                    sql.required("scheduler-trigger-reconcile-load"),
                    scheduleId,
                    Timestamp.from(scheduledFireAt));
        } catch (EmptyResultDataAccessException missing) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "BATCH_SCHEDULER_TRIGGER_NOT_FOUND",
                    missing);
        }
    }

    private Map<String, Object> auditByIdempotencyKey(String idempotencyKey) {
        try {
            return jdbc.queryForMap(
                    sql.required("scheduler-trigger-reconcile-audit-find"),
                    idempotencyKey.trim());
        } catch (EmptyResultDataAccessException missing) {
            return null;
        }
    }

    private static void assertRetryableUnknown(Map<String, Object> row, RetryUnknownRequest request) {
        if (!"UNKNOWN".equalsIgnoreCase(Objects.toString(row.get("trigger_status"), ""))) {
            throw conflict("BATCH_SCHEDULER_TRIGGER_NOT_UNKNOWN");
        }
        if (!request.expectedTriggerIdempotencyKey().equals(
                Objects.toString(row.get("idempotency_key"), ""))) {
            throw conflict("BATCH_SCHEDULER_TRIGGER_IDEMPOTENCY_CONFLICT");
        }
        if (number(row.get("attempt_count")) != request.expectedAttemptCount()) {
            throw conflict("BATCH_SCHEDULER_TRIGGER_ATTEMPT_CONFLICT");
        }
    }

    private static void assertAuditReplay(
            Map<String, Object> audit,
            String entityKey,
            RetryUnknownRequest request,
            BatVerifiedActorResolver.ApprovedActors actors) {
        boolean same = entityKey.equals(Objects.toString(audit.get("entity_key"), ""))
                && actors.approvalRequestId().equals(Objects.toString(audit.get("request_id"), ""))
                && actors.requestedBy().equals(Objects.toString(audit.get("requester_id"), ""))
                && actors.approvedBy().equals(Objects.toString(audit.get("approver_id"), ""))
                && request.reason().trim().equals(Objects.toString(audit.get("reason_text"), ""))
                && number(audit.get("expected_attempt")) == request.expectedAttemptCount()
                && "UNKNOWN".equalsIgnoreCase(Objects.toString(audit.get("from_status"), ""))
                && "FAILED".equalsIgnoreCase(Objects.toString(audit.get("to_status"), ""));
        if (!same) {
            throw conflict("BATCH_SCHEDULER_TRIGGER_RECONCILE_IDEMPOTENCY_KEY_REUSED");
        }
    }

    private static ResponseEntity<Map<String, Object>> accepted(
            String scheduleId,
            RetryUnknownRequest request,
            BatVerifiedActorResolver.ApprovedActors actors,
            boolean replayed) {
        return ResponseEntity.accepted().body(Map.of(
                "scheduleId", scheduleId,
                "scheduledFireAt", request.scheduledFireAt().toString(),
                "previousStatus", "UNKNOWN",
                "status", "FAILED",
                "expectedAttemptCount", request.expectedAttemptCount(),
                "approvalRequestId", actors.approvalRequestId(),
                "replayed", replayed));
    }

    private static void validate(RetryUnknownRequest request) {
        if (request == null
                || request.scheduledFireAt() == null
                || request.expectedTriggerIdempotencyKey() == null
                || request.expectedTriggerIdempotencyKey().isBlank()
                || request.expectedTriggerIdempotencyKey().length() > 200
                || request.expectedAttemptCount() < 0
                || request.idempotencyKey() == null
                || request.idempotencyKey().isBlank()
                || request.idempotencyKey().length() > 200
                || request.reason() == null
                || request.reason().isBlank()
                || request.reason().length() > 1000) {
            throw new IllegalArgumentException(
                    "scheduledFireAt, trigger idempotency key, non-negative expectedAttemptCount, reconciliation idempotency key and reason are required");
        }
    }

    private static void validateScheduleId(String scheduleId) {
        if (scheduleId == null || !scheduleId.matches("[A-Za-z0-9._:-]{1,120}")) {
            throw new IllegalArgumentException("scheduleId is invalid");
        }
    }

    private static String entityKey(String scheduleId, Instant scheduledFireAt) {
        return scheduleId + "@" + scheduledFireAt;
    }

    private static String reconciliationCode(
            String scheduleId,
            RetryUnknownRequest request,
            BatVerifiedActorResolver.ApprovedActors actors) {
        String canonical = String.join("\n",
                scheduleId,
                request.scheduledFireAt().toString(),
                request.expectedTriggerIdempotencyKey(),
                Integer.toString(request.expectedAttemptCount()),
                request.idempotencyKey().trim(),
                actors.requestedBy(),
                actors.approvedBy(),
                actors.approvalRequestId(),
                request.reason().trim());
        try {
            return RECONCILED_PREFIX + HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception unavailable) {
            throw new IllegalStateException("SHA-256 is unavailable", unavailable);
        }
    }

    private static long number(Object value) {
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(Objects.toString(value, "-1"));
    }

    private static ResponseStatusException conflict(String code) {
        return new ResponseStatusException(HttpStatus.CONFLICT, code);
    }

    /** 승인된 UNKNOWN Scheduler Trigger 재판정 입력입니다. */
    public record RetryUnknownRequest(
            Instant scheduledFireAt,
            String expectedTriggerIdempotencyKey,
            int expectedAttemptCount,
            String idempotencyKey,
            String requestedBy,
            String approvedBy,
            String approvalRequestId,
            String reason) {
    }
}
