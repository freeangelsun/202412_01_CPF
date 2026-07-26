package com.cpf.batch.control.centercut;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.runtime.CenterCutParameterProtector;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Center-Cut 실행의 불변 Parameter Snapshot과 상태 전이를 소유합니다.
 * UNKNOWN_RESULT는 자동 Resume되지 않고 명시적 Reconciliation을 거쳐야 합니다.
 */
@Service
public class CenterCutExecutionService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CenterCutParameterProtector protector;
    private final CpfVendorSqlCatalog sql;

    public CenterCutExecutionService(JdbcTemplate jdbc, ObjectMapper mapper,
                                     CenterCutParameterProtector protector,
                                     Environment environment) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.protector = protector;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    @Transactional
    public Map<String, Object> create(CenterCutExecutionRequest request) throws Exception {
        List<Map<String, Object>> existing = jdbc.queryForList(
                sql.required("centercut-execution-find-by-idempotency"), request.idempotencyKey());
        if (!existing.isEmpty()) return existing.getFirst();

        Map<String, Object> job = jdbc.queryForMap(
                sql.required("centercut-job-find-active"),
                request.centerCutJobId());
        if (!"Y".equals(String.valueOf(job.get("use_yn")))) {
            throw new IllegalStateException("Center-Cut job disabled");
        }

        String canonical = mapper.writeValueAsString(new TreeMap<>(request.parameters()));
        var protectedPayload = protector.protect(canonical);
        String executionId = UUID.randomUUID().toString();
        jdbc.update(sql.required("centercut-execution-insert"),
                executionId, request.centerCutJobId(), request.idempotencyKey(), protectedPayload.cipherText(),
                protectedPayload.sha256(), request.parameterSchemaVersion(), request.tpsLimit(),
                request.concurrencyLimit(), request.transactionId(), request.parentSegmentId(), request.requestedBy(),
                SensitiveTextSanitizer.sanitize(request.reason()));
        return detail(executionId);
    }

    public Map<String, Object> detail(String id) {
        return jdbc.queryForMap(sql.required("centercut-execution-detail"), id);
    }

    @Transactional
    public Map<String, Object> transition(String id, String action, String requestedBy,
                                          String approvedBy, String reason) {
        approve(requestedBy, approvedBy, reason);
        String normalized = action.toUpperCase(Locale.ROOT);
        Map<String, Object> before = detail(id);
        String current = String.valueOf(before.get("execution_state"));
        String next = nextState(normalized, current, "Y".equals(before.get("target_complete_yn")));

        int changed = jdbc.update(
                sql.required("centercut-execution-transition"), next, id, current);
        if (changed != 1) throw new IllegalStateException("Center-Cut state changed concurrently");

        jdbc.update(sql.required("centercut-execution-audit"),
                before.get("center_cut_job_id"), "CENTER_CUT_" + normalized, requestedBy,
                SensitiveTextSanitizer.sanitize(reason), SensitiveTextSanitizer.sanitize(String.valueOf(before)),
                "state=" + next + ",approvedBy=" + approvedBy, requestedBy, requestedBy);
        return detail(id);
    }

    private static String nextState(String action, String current, boolean targetComplete) {
        if (Set.of("COMPLETED", "CANCELLED").contains(current)) {
            throw new IllegalStateException("Terminal Center-Cut execution cannot transition");
        }
        return switch (action) {
            case "START" -> {
                if (!Set.of("CREATED", "TARGETING", "PAUSED").contains(current)) {
                    throw new IllegalStateException("START not allowed from " + current);
                }
                yield targetComplete ? "RUNNING" : "TARGETING";
            }
            case "RESUME" -> {
                if (!Set.of("PAUSED", "DRAINING").contains(current)) {
                    throw new IllegalStateException("RESUME not allowed from " + current);
                }
                yield "RUNNING";
            }
            case "PAUSE" -> {
                if (!"RUNNING".equals(current)) throw new IllegalStateException("PAUSE not allowed from " + current);
                yield "PAUSED";
            }
            case "DRAIN" -> {
                if (!"RUNNING".equals(current)) throw new IllegalStateException("DRAIN not allowed from " + current);
                yield "DRAINING";
            }
            case "CANCEL" -> "CANCELLED";
            default -> throw new IllegalArgumentException("Unsupported Center-Cut action: " + action);
        };
    }

    private static void approve(String requestedBy, String approvedBy, String reason) {
        if (requestedBy == null || requestedBy.isBlank() || approvedBy == null || approvedBy.isBlank()
                || requestedBy.equals(approvedBy) || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("requester/approver separation and reason required");
        }
    }
}
