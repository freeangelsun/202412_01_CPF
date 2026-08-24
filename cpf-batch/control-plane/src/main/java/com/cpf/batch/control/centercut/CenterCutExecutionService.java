package com.cpf.batch.control.centercut;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.api.CpfCenterCutOperations;
import com.cpf.batch.runtime.CenterCutParameterProtector;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Center-Cut 실행의 불변 Parameter Snapshot과 상태 전이를 소유합니다.
 * UNKNOWN_RESULT는 자동 Resume되지 않고 명시적 Reconciliation을 거쳐야 합니다.
 */
@Service
public class CenterCutExecutionService implements CpfCenterCutOperations {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CenterCutParameterProtector protector;
    private final CpfVendorSqlCatalog sql;
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfExecutionIdGenerator executionIds;

    public CenterCutExecutionService(JdbcTemplate jdbc, ObjectMapper mapper,
                                     CenterCutParameterProtector protector,
                                     CpfVendorSqlCatalogProvider sqlCatalogProvider,
                                     CpfTransactionIdGenerator transactionIds,
                                     CpfExecutionIdGenerator executionIds) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.protector = protector;
        this.sql = sqlCatalogProvider.forModule("bat");
        this.transactionIds = Objects.requireNonNull(transactionIds, "transactionIds");
        this.executionIds = Objects.requireNonNull(executionIds, "executionIds");
    }

    @Override
    @Transactional
    public Map<String, Object> launch(CenterCutExecutionRequest request) throws Exception {
        List<Map<String, Object>> existing = jdbc.queryForList(
                sql.required("centercut-execution-find-by-idempotency"), request.idempotencyKey());
        if (!existing.isEmpty()) return existing.getFirst();

        Map<String, Object> job;
        try {
            job = jdbc.queryForMap(
                    sql.required("centercut-job-find-active"),
                    request.centerCutJobId());
        } catch (EmptyResultDataAccessException missing) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "CENTER_CUT_JOB_NOT_FOUND");
        }
        if (!"Y".equals(String.valueOf(job.get("use_yn")))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "CENTER_CUT_JOB_DISABLED");
        }

        String canonical = mapper.writeValueAsString(new TreeMap<>(request.parameters()));
        var protectedPayload = protector.protect(canonical);
        LaunchContext launchContext = resolveLaunchContext(request);
        String executionId = UUID.randomUUID().toString();
        jdbc.update(sql.required("centercut-execution-insert"),
                executionId, request.centerCutJobId(), request.idempotencyKey(), protectedPayload.cipherText(),
                protectedPayload.sha256(), request.parameterSchemaVersion(), request.tpsLimit(),
                request.concurrencyLimit(), launchContext.transactionId(), launchContext.parentSegmentId(), request.requestedBy(),
                SensitiveTextSanitizer.sanitize(request.reason()));
        return detail(executionId);
    }

    LaunchContext resolveLaunchContext(CenterCutExecutionRequest request) {
        CpfContext current = CpfContexts.current();
        String suppliedTransaction = text(request.transactionId());
        if (current != null && suppliedTransaction != null
                && !current.transactionId().equals(suppliedTransaction)) {
            throw new SecurityException("CENTER_CUT_TRANSACTION_CONTEXT_MISMATCH");
        }
        String transactionId = suppliedTransaction != null
                ? suppliedTransaction
                : current != null ? current.transactionId() : transactionIds.newTransactionId();
        transactionId = CpfTransactionIds.requireCanonical(transactionId);

        String suppliedParentSegment = text(request.parentSegmentId());
        if (current != null && suppliedParentSegment != null
                && !current.segmentId().equals(suppliedParentSegment)) {
            throw new SecurityException("CENTER_CUT_PARENT_SEGMENT_CONTEXT_MISMATCH");
        }
        String parentSegmentId = suppliedParentSegment != null
                ? suppliedParentSegment
                : current != null ? current.segmentId() : executionIds.newSegmentId();
        parentSegmentId = requiredIdentifier(parentSegmentId, "parentSegmentId");
        return new LaunchContext(transactionId, parentSegmentId);
    }

    @Override
    public Map<String, Object> status(String id) {
        try {
            return jdbc.queryForMap(sql.required("centercut-execution-detail"), id);
        } catch (EmptyResultDataAccessException missing) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "CENTER_CUT_EXECUTION_NOT_FOUND");
        }
    }


    /** @deprecated 내부 호환 호출은 public launch API로 이관합니다. */
    @Deprecated(forRemoval = true)
    public Map<String, Object> create(CenterCutExecutionRequest request) throws Exception { return launch(request); }

    /** @deprecated 내부 호환 호출은 public status API로 이관합니다. */
    @Deprecated(forRemoval = true)
    public Map<String, Object> detail(String id) { return status(id); }

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

    static String nextState(String action, String current, boolean targetComplete) {
        if (Set.of("COMPLETED", "CANCELLED").contains(current)) {
            throw new IllegalStateException("Terminal Center-Cut execution cannot transition");
        }
        return switch (action) {
            case "START" -> {
                if (!Set.of("CREATED", "TARGETING", "TARGET_READY", "PAUSED").contains(current)) {
                    throw new IllegalStateException("START not allowed from " + current);
                }
                yield targetComplete || "PAUSED".equals(current) ? "RUNNING" : "STARTING";
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

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String requiredIdentifier(String value, String name) {
        String normalized = text(value);
        if (normalized == null || normalized.length() > 160
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid Center-Cut " + name);
        }
        return normalized;
    }

    record LaunchContext(String transactionId, String parentSegmentId) { }
}
