package cpf.pfw.common.servicecall;

import cpf.pfw.common.logging.TransactionContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PFW 서비스 레지스트리 테이블을 조회하고 호출 이력을 기록하는 JDBC 저장소입니다.
 *
 * <p>pfwDB가 아직 설치되지 않은 개발 환경에서는 빈 결과를 반환해 애플리케이션 기동을 막지 않습니다.
 * 운영 조회 API는 빈 결과와 tableAvailable 정보를 함께 보여 주어 미설치 상태를 명확하게 확인하게 합니다.</p>
 */
public class CpfServiceRegistryRepository {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public CpfServiceRegistryRepository(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("pfwDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    public boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null || dataSourceProvider.getIfAvailable() != null;
    }

    public boolean tableAvailable(String tableName) {
        if (!available() || !hasText(tableName)) {
            return false;
        }
        try {
            Integer count = jdbc().queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = ?
                    """, Integer.class, tableName.trim());
            return count != null && count > 0;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public List<Map<String, Object>> findServices(String serviceId, String useYn, int limit) {
        if (!tableAvailable("pfw_service")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT service_id AS serviceId,
                       service_name AS serviceName,
                       service_type AS serviceType,
                       owner_module_code AS ownerModuleCode,
                       description AS description,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM pfw_service
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "use_yn", useYn);
        sql.append(" ORDER BY service_id LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> findEndpoints(String serviceId, String endpointCode, String useYn, int limit) {
        if (!tableAvailable("pfw_service_endpoint")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT endpoint_code AS endpointCode,
                       service_id AS serviceId,
                       endpoint_name AS endpointName,
                       endpoint_type AS endpointType,
                       base_url AS baseUrl,
                       context_path AS contextPath,
                       default_timeout_ms AS defaultTimeoutMs,
                       default_retry_count AS defaultRetryCount,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM pfw_service_endpoint
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        appendEquals(sql, args, "use_yn", useYn);
        sql.append(" ORDER BY service_id, endpoint_code LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> findInstances(String serviceId, String endpointCode, String status, int limit) {
        if (!tableAvailable("pfw_service_instance")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT instance_id AS instanceId,
                       service_id AS serviceId,
                       endpoint_code AS endpointCode,
                       instance_name AS instanceName,
                       base_url AS baseUrl,
                       host_name AS hostName,
                       port_no AS portNo,
                       instance_status AS instanceStatus,
                       weight AS weight,
                       active_yn AS activeYn,
                       last_heartbeat_at AS lastHeartbeatAt,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM pfw_service_instance
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        appendEquals(sql, args, "instance_status", status);
        sql.append(" ORDER BY service_id, endpoint_code, weight DESC, instance_id LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> findHealthStatuses(String serviceId, String endpointCode, int limit) {
        if (!tableAvailable("pfw_service_health_status")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT health_id AS healthId,
                       service_id AS serviceId,
                       endpoint_code AS endpointCode,
                       instance_id AS instanceId,
                       health_status AS healthStatus,
                       http_status AS httpStatus,
                       response_time_ms AS responseTimeMs,
                       failure_message AS failureMessage,
                       checked_at AS checkedAt,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM pfw_service_health_status
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        sql.append(" ORDER BY checked_at DESC, health_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> findRoutingPolicies(String serviceId, String endpointCode, String activeYn, int limit) {
        if (!tableAvailable("pfw_service_routing_policy")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT policy_id AS policyId,
                       service_id AS serviceId,
                       endpoint_code AS endpointCode,
                       routing_mode AS routingMode,
                       load_balance_type AS loadBalanceType,
                       failover_enabled_yn AS failoverEnabledYn,
                       health_check_required_yn AS healthCheckRequiredYn,
                       active_yn AS activeYn,
                       priority AS priority,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM pfw_service_routing_policy
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        appendEquals(sql, args, "active_yn", activeYn);
        sql.append(" ORDER BY priority, policy_id LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> findCircuitStates(String serviceId, String endpointCode, int limit) {
        if (!tableAvailable("pfw_service_circuit_state")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT circuit_id AS circuitId,
                       service_id AS serviceId,
                       endpoint_code AS endpointCode,
                       instance_id AS instanceId,
                       circuit_state AS circuitState,
                       failure_count AS failureCount,
                       success_count AS successCount,
                       opened_at AS openedAt,
                       half_opened_at AS halfOpenedAt,
                       closed_at AS closedAt,
                       last_failure_message AS lastFailureMessage,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM pfw_service_circuit_state
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        sql.append(" ORDER BY service_id, endpoint_code, instance_id LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public Optional<Map<String, Object>> findCircuitState(ServiceCallResolvedTarget target) {
        if (!tableAvailable("pfw_service_circuit_state") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
            return Optional.empty();
        }
        try {
            List<Map<String, Object>> rows = jdbc().queryForList("""
                    SELECT circuit_id AS circuitId,
                           service_id AS serviceId,
                           endpoint_code AS endpointCode,
                           instance_id AS instanceId,
                           circuit_state AS circuitState,
                           failure_count AS failureCount,
                           success_count AS successCount,
                           opened_at AS openedAt,
                           half_opened_at AS halfOpenedAt,
                           closed_at AS closedAt,
                           last_failure_message AS lastFailureMessage
                    FROM pfw_service_circuit_state
                    WHERE service_id = ?
                      AND endpoint_code = ?
                      AND (instance_id = ? OR (? IS NULL AND instance_id IS NULL))
                    ORDER BY circuit_id DESC
                    LIMIT 1
                    """, target.serviceId(), target.endpointCode(), target.instanceId(), target.instanceId());
            return rows.stream().findFirst();
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Map<String, Object>> findCallHistory(String serviceId, String transactionGlobalId, int limit) {
        if (!tableAvailable("pfw_service_call_history")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT call_id AS callId,
                       transaction_global_id AS transactionGlobalId,
                       trace_id AS traceId,
                       service_id AS serviceId,
                       endpoint_code AS endpointCode,
                       instance_id AS instanceId,
                       http_method AS httpMethod,
                       request_path AS requestPath,
                       call_status AS callStatus,
                       http_status AS httpStatus,
                       duration_ms AS durationMs,
                       timeout_ms AS timeoutMs,
                       retry_count AS retryCount,
                       failure_code AS failureCode,
                       failure_message AS failureMessage,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM pfw_service_call_history
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "transaction_global_id", transactionGlobalId);
        sql.append(" ORDER BY call_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public void insertCallHistory(
            ServiceCallRequest request,
            ServiceCallResolvedTarget target,
            String callStatus,
            Integer httpStatus,
            long durationMillis,
            String failureCode,
            String failureMessage) {
        if (!tableAvailable("pfw_service_call_history")) {
            return;
        }
        jdbc().update("""
                INSERT INTO pfw_service_call_history (
                    transaction_global_id, trace_id, service_id, endpoint_code, instance_id,
                    http_method, request_path, call_status, http_status, duration_ms,
                    timeout_ms, retry_count, failure_code, failure_message, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PFW_SERVICE_CALL', 'PFW_SERVICE_CALL')
                """,
                TransactionContext.currentTransactionId(),
                TransactionContext.currentTraceId(),
                normalize(request.serviceId()),
                firstText(request.endpointCode(), value(target.endpoint(), "endpointCode")),
                firstText(request.instanceId(), value(target.instance(), "instanceId")),
                firstText(request.httpMethod(), "GET"),
                firstText(request.requestPath(), "/"),
                firstText(callStatus, "UNKNOWN"),
                httpStatus,
                durationMillis,
                request.timeoutMillis(),
                request.retryCount(),
                failureCode,
                failureMessage);
    }

    public void recordHealthStatus(
            ServiceCallResolvedTarget target,
            String healthStatus,
            Integer httpStatus,
            long durationMillis,
            String failureMessage) {
        if (!tableAvailable("pfw_service_health_status") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
            return;
        }
        try {
            jdbc().update("""
                    INSERT INTO pfw_service_health_status (
                        service_id, endpoint_code, instance_id, health_status, http_status,
                        response_time_ms, failure_message, checked_at, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3), 'PFW_SERVICE_CALL', 'PFW_SERVICE_CALL')
                    """,
                    target.serviceId(),
                    target.endpointCode(),
                    target.instanceId(),
                    firstText(healthStatus, "UNKNOWN"),
                    httpStatus,
                    durationMillis,
                    truncate(failureMessage, 900));
            updateInstanceHealth(target, healthStatus);
        } catch (DataAccessException ignored) {
            // Health 기록 실패가 업무 호출 결과를 뒤집지 않도록 fail-open으로 처리합니다.
        }
    }

    public void recordCircuitSuccess(ServiceCallResolvedTarget target) {
        if (!tableAvailable("pfw_service_circuit_state") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
            return;
        }
        try {
            jdbc().update("""
                    INSERT INTO pfw_service_circuit_state (
                        service_id, endpoint_code, instance_id, circuit_state,
                        failure_count, success_count, closed_at, created_by, updated_by
                    ) VALUES (?, ?, ?, 'CLOSED', 0, 1, CURRENT_TIMESTAMP(3), 'PFW_SERVICE_CALL', 'PFW_SERVICE_CALL')
                    ON DUPLICATE KEY UPDATE
                        circuit_state = 'CLOSED',
                        failure_count = 0,
                        success_count = success_count + 1,
                        closed_at = CURRENT_TIMESTAMP(3),
                        last_failure_message = NULL,
                        updated_by = 'PFW_SERVICE_CALL',
                        updated_at = CURRENT_TIMESTAMP
                    """, target.serviceId(), target.endpointCode(), target.instanceId());
        } catch (DataAccessException ignored) {
            // Circuit 상태 기록 실패는 호출 본문 성공을 실패로 바꾸지 않습니다.
        }
    }

    public void recordCircuitHalfOpen(ServiceCallResolvedTarget target) {
        if (!tableAvailable("pfw_service_circuit_state") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
            return;
        }
        try {
            jdbc().update("""
                    UPDATE pfw_service_circuit_state
                    SET circuit_state = 'HALF_OPEN',
                        half_opened_at = CURRENT_TIMESTAMP(3),
                        updated_by = 'PFW_SERVICE_CALL',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE service_id = ?
                      AND endpoint_code = ?
                      AND (instance_id = ? OR (instance_id IS NULL AND ? IS NULL))
                      AND circuit_state = 'OPEN'
                    """, target.serviceId(), target.endpointCode(), target.instanceId(), target.instanceId());
        } catch (DataAccessException ignored) {
            // Circuit 상태 전이는 호출 차단 여부 판단을 보조하는 기록이므로 저장 실패 시 fail-open 처리합니다.
        }
    }

    public void recordCircuitFailure(ServiceCallResolvedTarget target, String failureMessage, int threshold) {
        if (!tableAvailable("pfw_service_circuit_state") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
            return;
        }
        int resolvedThreshold = Math.max(1, threshold);
        try {
            int currentFailureCount = findCircuitState(target)
                    .map(row -> intValue(row.get("failureCount")))
                    .orElse(0);
            int nextFailureCount = currentFailureCount + 1;
            String nextState = nextFailureCount >= resolvedThreshold ? "OPEN" : "CLOSED";
            jdbc().update("""
                    INSERT INTO pfw_service_circuit_state (
                        service_id, endpoint_code, instance_id, circuit_state,
                        failure_count, success_count, opened_at, last_failure_message, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, 0,
                        CASE WHEN ? = 'OPEN' THEN CURRENT_TIMESTAMP(3) ELSE NULL END,
                        ?, 'PFW_SERVICE_CALL', 'PFW_SERVICE_CALL')
                    ON DUPLICATE KEY UPDATE
                        circuit_state = VALUES(circuit_state),
                        failure_count = VALUES(failure_count),
                        success_count = 0,
                        opened_at = CASE WHEN VALUES(circuit_state) = 'OPEN' THEN CURRENT_TIMESTAMP(3) ELSE opened_at END,
                        last_failure_message = VALUES(last_failure_message),
                        updated_by = 'PFW_SERVICE_CALL',
                        updated_at = CURRENT_TIMESTAMP
                    """,
                    target.serviceId(),
                    target.endpointCode(),
                    target.instanceId(),
                    nextState,
                    nextFailureCount,
                    nextState,
                    truncate(failureMessage, 900));
        } catch (DataAccessException ignored) {
            // Circuit 상태 기록 실패는 원 호출 실패 사유를 덮어쓰지 않습니다.
        }
    }

    private void updateInstanceHealth(ServiceCallResolvedTarget target, String healthStatus) {
        if (!tableAvailable("pfw_service_instance") || !hasText(target.instanceId())) {
            return;
        }
        String instanceStatus = "UP".equalsIgnoreCase(healthStatus) ? "UP" : "DOWN";
        jdbc().update("""
                UPDATE pfw_service_instance
                SET instance_status = ?,
                    last_heartbeat_at = CURRENT_TIMESTAMP(3),
                    updated_by = 'PFW_SERVICE_CALL',
                    updated_at = CURRENT_TIMESTAMP
                WHERE instance_id = ?
                """, instanceStatus, target.instanceId());
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("pfwDataSource 또는 pfwJdbcTemplate이 필요합니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String columnName, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(columnName).append(" = ?");
            args.add(value.trim());
        }
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 1000));
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim().toUpperCase() : null;
    }

    private String firstText(String first, String fallback) {
        return hasText(first) ? first.trim() : fallback;
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String value(Map<String, Object> row, String key) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
