package com.cpf.batch.control.centercut;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.runtime.CenterCutParameterProtector;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public CenterCutExecutionService(JdbcTemplate jdbc, ObjectMapper mapper,
                                     CenterCutParameterProtector protector) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.protector = protector;
    }

    @Transactional
    public Map<String, Object> create(CenterCutExecutionRequest request) throws Exception {
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT * FROM bat_center_cut_execution WHERE idempotency_key=?", request.idempotencyKey());
        if (!existing.isEmpty()) return existing.getFirst();

        Map<String, Object> job = jdbc.queryForMap(
                "SELECT center_cut_job_id,use_yn FROM bat_center_cut_job WHERE center_cut_job_id=?",
                request.centerCutJobId());
        if (!"Y".equals(String.valueOf(job.get("use_yn")))) {
            throw new IllegalStateException("Center-Cut job disabled");
        }

        String canonical = mapper.writeValueAsString(new TreeMap<>(request.parameters()));
        var protectedPayload = protector.protect(canonical);
        String executionId = UUID.randomUUID().toString();
        jdbc.update("""
            INSERT INTO bat_center_cut_execution(
                center_cut_execution_id,center_cut_job_id,idempotency_key,execution_state,
                parameter_ciphertext,parameter_hash,parameter_schema_version,target_cursor,target_complete_yn,
                target_count,tps_limit,concurrency_limit,transaction_id,parent_segment_id,requested_by,reason_text,
                created_at,updated_at)
            VALUES(?,?,?,'CREATED',?,?,?,NULL,'N',0,?,?,?,?,?,?,CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6))
            """, executionId, request.centerCutJobId(), request.idempotencyKey(), protectedPayload.cipherText(),
                protectedPayload.sha256(), request.parameterSchemaVersion(), request.tpsLimit(),
                request.concurrencyLimit(), request.transactionId(), request.parentSegmentId(), request.requestedBy(),
                SensitiveTextSanitizer.sanitize(request.reason()));
        return detail(executionId);
    }

    public Map<String, Object> detail(String id) {
        return jdbc.queryForMap("SELECT * FROM bat_center_cut_execution WHERE center_cut_execution_id=?", id);
    }

    @Transactional
    public Map<String, Object> transition(String id, String action, String requestedBy,
                                          String approvedBy, String reason) {
        approve(requestedBy, approvedBy, reason);
        String normalized = action.toUpperCase(Locale.ROOT);
        Map<String, Object> before = detail(id);
        String current = String.valueOf(before.get("execution_state"));
        String next = nextState(normalized, current, "Y".equals(before.get("target_complete_yn")));

        int changed = jdbc.update("""
            UPDATE bat_center_cut_execution
               SET execution_state=?,updated_at=CURRENT_TIMESTAMP(6)
             WHERE center_cut_execution_id=? AND execution_state=?
            """, next, id, current);
        if (changed != 1) throw new IllegalStateException("Center-Cut state changed concurrently");

        jdbc.update("""
            INSERT INTO bat_operation_log(job_id,operation_type,operator_id,reason,before_data,after_data,
                                          result_type,result_message,created_by,updated_by)
            VALUES(?,?,?,?,?,?,'S','OK',?,?)
            """, before.get("center_cut_job_id"), "CENTER_CUT_" + normalized, requestedBy,
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
