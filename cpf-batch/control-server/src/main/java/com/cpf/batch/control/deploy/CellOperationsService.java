package com.cpf.batch.control.deploy;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Cell Scale/Reconcile의 승인·멱등·결과불명 경계를 소유합니다. */
@Service
public final class CellOperationsService {
    private final JdbcTemplate jdbc;
    private final RuntimeLifecycleService lifecycle;
    private final CpfVendorSqlCatalog sql;

    public CellOperationsService(
            JdbcTemplate jdbc,
            RuntimeLifecycleService lifecycle,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        this.sql = Objects.requireNonNull(sqlCatalogProvider, "sqlCatalogProvider").forModule("bat");
    }

    public Map<String, Object> status(String cellId) {
        String safeCellId = required(cellId, "cellId");
        Map<String, Object> cell = jdbc.queryForMap(sql.required("deploy-cell-detail"), safeCellId);
        List<Map<String, Object>> instances = jdbc.queryForList(sql.required("deploy-cell-status-instances"), safeCellId);
        return Map.of("cell", cell, "instances", instances);
    }

    public OperationResult scale(String cellId, int desired, ApprovedRequest approval) {
        String safeCellId = required(cellId, "cellId");
        approve(approval);
        List<Map<String, Object>> inventory = jdbc.queryForList(sql.required("deploy-cell-scale-inventory"), safeCellId);
        if (desired < 0 || desired > inventory.size()) {
            throw new IllegalArgumentException("desired count must be within approved cell inventory");
        }
        int running = (int) inventory.stream().filter(this::running).count();
        List<Map<String, Object>> results = new ArrayList<>();
        if (desired > running) {
            int need = desired - running;
            for (Map<String, Object> row : inventory) {
                if (need == 0) break;
                if (running(row)) continue;
                AgentCommandResult result = operate(row, "start", approval);
                results.add(view(result, row, "START"));
                if (result.state() == CommandState.SUCCEEDED) need--;
                if (result.state() == CommandState.UNKNOWN_RESULT) break;
            }
            if (need > 0) return finish(safeCellId, "SCALE", desired, approval, results,
                    aggregate(results, "PARTIAL"), "Not enough instances could be started");
        } else if (desired < running) {
            int remove = running - desired;
            List<Map<String, Object>> reversed = new ArrayList<>(inventory);
            Collections.reverse(reversed);
            for (Map<String, Object> row : reversed) {
                if (remove == 0) break;
                if (!running(row)) continue;
                AgentCommandResult drain = operate(row, "drain", approval);
                results.add(view(drain, row, "DRAIN"));
                if (drain.state() != CommandState.SUCCEEDED) {
                    if (drain.state() == CommandState.UNKNOWN_RESULT) break;
                    continue;
                }
                String instanceId = Objects.toString(row.get("instance_id"), "");
                if (!waitIdle(instanceId, Duration.ofMinutes(10))) {
                    results.add(Map.of("instanceId", instanceId, "operation", "STOP", "state", "UNKNOWN_RESULT"));
                    break;
                }
                AgentCommandResult stop = operate(row, "stop", approval);
                results.add(view(stop, row, "STOP"));
                if (stop.state() == CommandState.SUCCEEDED) remove--;
                if (stop.state() == CommandState.UNKNOWN_RESULT) break;
            }
            if (remove > 0) return finish(safeCellId, "SCALE", desired, approval, results,
                    aggregate(results, "PARTIAL"), "Scale-in incomplete; busy or unknown instances were preserved");
        }
        int changed = jdbc.update(sql.required("deploy-cell-update-desired-count"), desired, safeCellId);
        if (changed != 1) throw new IllegalStateException("Desired instance count was not persisted");
        return finish(safeCellId, "SCALE", desired, approval, results, "SUCCEEDED", "Scale completed");
    }

    public OperationResult reconcile(String cellId, ApprovedRequest approval) {
        String safeCellId = required(cellId, "cellId");
        approve(approval);
        List<Map<String, Object>> rows = jdbc.queryForList(sql.required("deploy-cell-reconcile-inventory"), safeCellId);
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String desired = Objects.toString(row.get("desired_state"), "RUNNING");
            String actual = Objects.toString(row.get("actual_state"), "STOPPED");
            String operation = null;
            if ("RUNNING".equals(desired) && !Set.of("READY", "BUSY", "STARTING").contains(actual)) operation = "start";
            else if ("STOPPED".equals(desired) && !"STOPPED".equals(actual)) operation = "stop";
            else if ("DRAINING".equals(desired) && !"DRAINING".equals(actual)) operation = "drain";
            if (operation != null) {
                AgentCommandResult result = operate(row, operation, approval);
                results.add(view(result, row, operation.toUpperCase(Locale.ROOT)));
                if (result.state() == CommandState.UNKNOWN_RESULT) break;
            }
        }
        String state = aggregate(results, "SUCCEEDED");
        String message = switch (state) {
            case "SUCCEEDED" -> "Reconciliation completed";
            case "UNKNOWN_RESULT" -> "Reconciliation result is unknown; resolve before retry";
            case "FAILED" -> "Reconciliation failed";
            default -> "Reconciliation partially completed";
        };
        return finish(safeCellId, "RECONCILE", null, approval, results, state, message);
    }

    private AgentCommandResult operate(Map<String, Object> row, String operation, ApprovedRequest approval) {
        String instanceId = required(Objects.toString(row.get("instance_id"), ""), "instanceId");
        return lifecycle.operate(instanceId, operation, approval.requestedBy(), approval.approvedBy(),
                approval.approvalRequestId(), approval.reason());
    }

    private static Map<String, Object> view(AgentCommandResult result, Map<String, Object> row, String operation) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("instanceId", Objects.toString(row.get("instance_id"), ""));
        value.put("operation", operation);
        value.put("state", result.state().name());
        value.put("commandId", result.commandId());
        value.put("resultCode", result.resultCode());
        return Map.copyOf(value);
    }

    private OperationResult finish(
            String cellId,
            String operation,
            Integer desired,
            ApprovedRequest approval,
            List<Map<String, Object>> results,
            String state,
            String message) {
        audit(cellId, operation, approval, state, desired, results.size());
        return new OperationResult(state, desired, List.copyOf(results), message);
    }

    private boolean running(Map<String, Object> row) {
        return Set.of("STARTING", "READY", "BUSY", "DRAINING", "DEGRADED")
                .contains(Objects.toString(row.get("actual_state"), ""));
    }

    private boolean waitIdle(String instanceId, Duration timeout) {
        Instant end = Instant.now().plus(timeout);
        while (Instant.now().isBefore(end)) {
            Integer count = jdbc.queryForObject(sql.required("deploy-runtime-current-execution-count"), Integer.class, instanceId);
            if (count != null && count == 0) return true;
            try {
                Thread.sleep(500);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private void approve(ApprovedRequest approval) {
        if (approval == null) throw new IllegalArgumentException("approval is required");
        String requester = required(approval.requestedBy(), "requestedBy");
        String approver = required(approval.approvedBy(), "approvedBy");
        required(approval.approvalRequestId(), "approvalRequestId");
        required(approval.reason(), "reason");
        if (requester.equals(approver)) throw new SecurityException("requester and approver must be different");
    }

    private void audit(
            String cellId,
            String operation,
            ApprovedRequest approval,
            String state,
            Integer desired,
            int resultCount) {
        String afterData = "cell=" + cellId + ",state=" + state + ",desired=" + desired + ",results=" + resultCount
                + ",approvalRequestId=" + approval.approvalRequestId() + ",approvedBy=" + approval.approvedBy();
        int changed = jdbc.update(sql.required("deploy-cell-operation-audit"), operation, approval.requestedBy(),
                approval.reason(), afterData, approval.requestedBy(), approval.approvedBy());
        if (changed != 1) throw new IllegalStateException("Cell operation audit was not persisted");
    }

    private static String aggregate(List<Map<String, Object>> results, String emptyState) {
        if (results.isEmpty()) return emptyState;
        boolean failed = false;
        boolean succeeded = false;
        for (Map<String, Object> result : results) {
            String state = Objects.toString(result.get("state"), "UNKNOWN_RESULT");
            if ("UNKNOWN_RESULT".equals(state)) return "UNKNOWN_RESULT";
            if (Set.of("FAILED", "PARTIALLY_ROLLED_BACK").contains(state)) failed = true;
            if (Set.of("SUCCEEDED", "ROLLED_BACK").contains(state)) succeeded = true;
        }
        if (failed && succeeded) return "PARTIAL";
        return failed ? "FAILED" : "SUCCEEDED";
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(fieldName + " is required");
        String safe = value.trim();
        if (safe.length() > 512 || safe.indexOf('\n') >= 0 || safe.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(fieldName + " is invalid");
        }
        return safe;
    }

    public record ApprovedRequest(String requestedBy, String approvedBy, String approvalRequestId, String reason) {
        /** Legacy compile compatibility only. Runtime validation rejects missing approvalRequestId. */
        public ApprovedRequest(String requestedBy, String approvedBy, String reason) {
            this(requestedBy, approvedBy, null, reason);
        }
    }

    public record OperationResult(String state, Integer desiredCount, List<Map<String, Object>> instances, String message) { }
}
