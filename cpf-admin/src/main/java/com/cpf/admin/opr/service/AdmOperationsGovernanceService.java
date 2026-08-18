package com.cpf.admin.opr.service;

import com.cpf.foundation.annotation.CpfService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 운영 정책·SLO Workbench의 읽기 전용 통합 Projection을 구성합니다.
 *
 * <p>별도 운영 데이터 복제 저장소를 만들지 않고 거래로그, Service Registry, Runtime Control,
 * Incident, External segment, 공통 설정의 실제 상태를 조합합니다. 변경은 각각의 Owner API
 * (Config/RuntimeControl/Approval/Incident)에서 수행하도록 하여 중복 Control Plane을 만들지 않습니다.</p>
 */
@CpfService
public class AdmOperationsGovernanceService extends com.cpf.admin.common.base.AdmBaseService {
    private static final int MAX_ITEMS = 50;
    private final JdbcTemplate cpfJdbcTemplate;
    private final Clock clock;
    private final Environment environment;

    public AdmOperationsGovernanceService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            Clock clock, Environment environment) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.clock = clock;
        this.environment = environment;
    }

    /** 운영자가 한 화면에서 확인해야 하는 SLO/Alert/Topology/Drift/DR/Runbook 상태를 반환합니다. */
    public Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("generatedAt", LocalDateTime.now(clock));
        result.put("metrics", metrics());
        result.put("slo", slo());
        result.put("alerts", recentAlerts());
        result.put("runbooks", configs("cpf.ops.runbook."));
        result.put("selfHealing", selfHealing());
        result.put("topology", topology());
        result.put("drift", drift());
        result.put("capacity", capacity());
        result.put("disasterRecovery", disasterRecovery());
        result.put("externalInstitutions", externalInstitutions());
        result.put("ownerRoutes", Map.of(
                "config", "configs",
                "runtimeControl", "runtimeControl",
                "approval", "approvals",
                "incident", "incidents",
                "topology", "topology",
                "gateway", "gateway-dashboard",
                "trace", "transactionGroups"));
        return result;
    }

    private Map<String, Object> metrics() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!table("cpf_transaction_log")) return unavailable("cpf_transaction_log");
        LocalDateTime since = LocalDateTime.now(clock).minus(1, ChronoUnit.HOURS);
        List<Map<String, Object>> rows = query("""
                SELECT COUNT(*) AS total_count,
                       SUM(CASE WHEN LOG_TYPE = 'FAILURE' OR ERROR_CODE IS NOT NULL THEN 1 ELSE 0 END) AS failure_count,
                       AVG(DURATION_MS) AS avg_duration_ms,
                       MAX(DURATION_MS) AS max_duration_ms
                FROM cpf_transaction_log
                WHERE START_TIME >= ?
                """, List.of(since), 1);
        result.put("available", true);
        result.put("windowMinutes", 60);
        result.put("summary", rows.isEmpty() ? Map.of() : rows.getFirst());
        result.put("source", "cpf_transaction_log");
        return result;
    }

    private Map<String, Object> slo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("policy", configs("cpf.ops.slo."));
        result.put("actual", metrics());
        result.put("policyOwner", "CMN_PARAMETER");
        result.put("changeRoute", "configs");
        return result;
    }

    private Map<String, Object> recentAlerts() {
        if (!table("adm_incident_lifecycle")) return unavailable("adm_incident_lifecycle");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", true);
        result.put("items", query("""
                SELECT incident_id, policy_id, severity, status, title, source_type, source_id,
                       correlation_id, transaction_id, occurrence_count, escalation_level,
                       first_occurred_at, last_occurred_at, acknowledged_at, resolved_at, owner_id, version
                FROM adm_incident_lifecycle
                ORDER BY last_occurred_at DESC, incident_id DESC
                """, List.of(), MAX_ITEMS));
        result.put("ownerRoute", "incidents");
        return result;
    }

    private Map<String, Object> selfHealing() {
        Map<String, Object> result = new LinkedHashMap<>();
        String allowlist = environment.getProperty("cpf.runtime.control.self-healing.allowed-change-types", "").trim();
        int rateLimit = boundedInt(environment.getProperty("cpf.runtime.control.self-healing.rate-limit-per-minute"), 10, 1, 60);
        int circuitThreshold = boundedInt(environment.getProperty("cpf.runtime.control.self-healing.circuit-failure-threshold"), 3, 1, 20);
        int circuitWindow = boundedInt(environment.getProperty("cpf.runtime.control.self-healing.circuit-window-seconds"), 900, 60, 86400);
        result.put("enabled", !allowlist.isBlank());
        result.put("killSwitch", allowlist.isBlank() ? "STOPPED" : "ACTIVE");
        result.put("allowlist", allowlist.isBlank() ? List.of() : java.util.Arrays.stream(allowlist.split(","))
                .map(String::trim).filter(v -> !v.isBlank()).map(String::toUpperCase).distinct().sorted().toList());
        result.put("rateLimitPerMinute", rateLimit);
        result.put("maxAttemptsPerChange", 1);
        result.put("circuitFailureThreshold", circuitThreshold);
        result.put("circuitWindowSeconds", circuitWindow);
        result.put("approvalBoundary", "원 변경에 approvalId 또는 breakGlassId가 기록된 경우에만 automatic rollback 후보");
        result.put("executionOwner", "cpf-starter-platform-operations-runtime-control");
        result.put("approvalOwner", "cpf-admin Approval Engine");
        result.put("safety", List.of("explicit-allowlist", "rate-limit", "one-attempt-per-change", "circuit-stop",
                "approval-boundary", "rollback-snapshot", "controller-lease-fencing", "immutable-audit"));
        result.put("policies", configs("cpf.ops.self-healing."));
        if (table("OPS_RUNTIME_CHANGE")) {
            result.put("candidates", query("""
                    SELECT change_id, operation_id, change_type, change_state, approval_id, break_glass_id,
                           requested_by, reason, created_at, updated_at
                    FROM OPS_RUNTIME_CHANGE
                    WHERE change_state IN ('FAILED','EXPIRED')
                      AND rollback_payload_json IS NOT NULL
                      AND (approval_id IS NOT NULL OR break_glass_id IS NOT NULL)
                    ORDER BY updated_at, change_id
                    """, List.of(), MAX_ITEMS));
            result.put("recentChanges", query("""
                    SELECT change_id, operation_id, change_type, change_state, reason, requested_by, created_at, updated_at
                    FROM OPS_RUNTIME_CHANGE
                    WHERE requested_by='CPF_CONTROLLER' OR reason LIKE 'CPF_SELF_HEALING:%'
                    ORDER BY created_at DESC
                    """, List.of(), 20));
        } else {
            result.put("candidates", List.of());
            result.put("recentChanges", List.of());
        }
        result.put("runbook", List.of(
                "Drift/FAILED/EXPIRED 원인 확인",
                "Allowlist와 기존 승인 Snapshot 확인",
                "Controller lease/fencing 획득",
                "Rate/attempt/circuit guard 통과",
                "Rollback snapshot 자동 적용",
                "UNKNOWN이면 재실행하지 말고 Operation ID와 Audit로 Reconcile"));
        return result;
    }

    private int boundedInt(String raw, int fallback, int min, int max) {
        if (raw == null || raw.isBlank()) return fallback;
        try { return Math.max(min, Math.min(max, Integer.parseInt(raw.trim()))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private Map<String, Object> topology() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("services", count("OPS_SERVICE"));
        result.put("instances", count("OPS_SERVICE_INSTANCE"));
        result.put("endpoints", count("OPS_SERVICE_ENDPOINT"));
        result.put("health", table("OPS_SERVICE_HEALTH_STATUS") ? query("""
                SELECT service_id, instance_id, health_status, checked_at
                FROM OPS_SERVICE_HEALTH_STATUS
                ORDER BY checked_at DESC
                """, List.of(), MAX_ITEMS) : List.of());
        result.put("ownerRoute", "topology");
        return result;
    }

    private Map<String, Object> drift() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", table("OPS_RUNTIME_INSTANCE_FEATURE_STATE"));
        result.put("items", table("OPS_RUNTIME_INSTANCE_FEATURE_STATE") ? query("""
                SELECT instance_id, change_type, desired_version, actual_version, desired_hash, actual_hash, drift_state, updated_at
                FROM OPS_RUNTIME_INSTANCE_FEATURE_STATE
                ORDER BY updated_at DESC
                """, List.of(), MAX_ITEMS) : List.of());
        result.put("ownerRoute", "runtimeControl");
        return result;
    }

    private Map<String, Object> capacity() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instances", count("OPS_RUNTIME_INSTANCE_STATE"));
        result.put("serviceInstances", count("OPS_SERVICE_INSTANCE"));
        result.put("transactionWindow", metrics());
        result.put("ownerRoute", "capacity");
        return result;
    }

    private Map<String, Object> disasterRecovery() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("policy", configs("cpf.ops.dr."));
        result.put("backupPolicy", configs("cpf.backup."));
        result.put("runtimeChanges", table("OPS_RUNTIME_CHANGE") ? query("""
                SELECT change_id, operation_id, change_type, change_state, reason, created_at, updated_at
                FROM OPS_RUNTIME_CHANGE
                WHERE UPPER(change_type) LIKE '%DR%' OR UPPER(change_type) LIKE '%FAILOVER%' OR UPPER(change_type) LIKE '%RESTORE%'
                ORDER BY created_at DESC
                """, List.of(), 20) : List.of());
        result.put("changeRoute", "runtimeControl");
        return result;
    }

    private Map<String, Object> externalInstitutions() {
        if (!table("cpf_transaction_segment")) return unavailable("cpf_transaction_segment");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", true);
        result.put("items", query("""
                SELECT external_institution_code, result_state, COUNT(*) AS call_count, MAX(ended_at) AS last_call_at
                FROM cpf_transaction_segment
                WHERE external_institution_code IS NOT NULL
                GROUP BY external_institution_code, result_state
                ORDER BY external_institution_code, result_state
                """, List.of(), MAX_ITEMS));
        result.put("traceRoute", "transactionGroups");
        result.put("gatewayRoute", "gateway-dashboard");
        return result;
    }

    private Map<String, Object> configs(String prefix) {
        if (!table("CMN_PARAMETER")) return unavailable("CMN_PARAMETER");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", true);
        result.put("items", query("""
                SELECT config_id, config_key, config_type, description, use_yn, updated_by, updated_at
                FROM CMN_PARAMETER
                WHERE UPPER(config_key) LIKE UPPER(?)
                ORDER BY config_key
                """, List.of(prefix + "%"), MAX_ITEMS));
        result.put("prefix", prefix);
        result.put("valuesMasked", true);
        return result;
    }

    private Map<String, Object> count(String tableName) {
        if (!table(tableName)) return unavailable(tableName);
        List<Map<String, Object>> rows = query("SELECT COUNT(*) AS item_count FROM " + tableName, List.of(), 1);
        return Map.of("available", true, "source", tableName, "summary", rows.isEmpty() ? Map.of() : rows.getFirst());
    }

    private Map<String, Object> unavailable(String source) {
        return Map.of("available", false, "source", source, "items", List.of());
    }

    private boolean table(String name) { return AdmJdbcQueries.tableExists(cpfJdbcTemplate, name); }
    private List<Map<String, Object>> query(String sql, List<?> args, int maxRows) {
        return AdmJdbcQueries.queryForList(cpfJdbcTemplate, sql, args, maxRows);
    }
}
