package cpf.adm.opr.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM 서비스 레지스트리 운영 조회 서비스입니다.
 *
 * <p>PFW 서비스 호출 엔진이 사용하는 service/endpoint/instance/health/routing/circuit/call history
 * 테이블을 운영자가 한 화면에서 점검할 수 있도록 조회 API를 제공합니다.</p>
 */
@Service
public class AdmServiceRegistryService {
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    private final JdbcTemplate pfwJdbcTemplate;

    public AdmServiceRegistryService(@Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
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
        QueryArgs args = new QueryArgs();
        args.addEquals(sql, "service_id", serviceId);
        args.addEquals(sql, "use_yn", useYn);
        sql.append(" ORDER BY service_id LIMIT ?");
        args.add(safeLimit(limit));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
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
        QueryArgs args = new QueryArgs();
        args.addEquals(sql, "service_id", serviceId);
        args.addEquals(sql, "endpoint_code", endpointCode);
        args.addEquals(sql, "use_yn", useYn);
        sql.append(" ORDER BY service_id, endpoint_code LIMIT ?");
        args.add(safeLimit(limit));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
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
        QueryArgs args = new QueryArgs();
        args.addEquals(sql, "service_id", serviceId);
        args.addEquals(sql, "endpoint_code", endpointCode);
        args.addEquals(sql, "instance_status", status);
        sql.append(" ORDER BY service_id, endpoint_code, weight DESC, instance_id LIMIT ?");
        args.add(safeLimit(limit));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> findHealth(String serviceId, String endpointCode, int limit) {
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
        QueryArgs args = new QueryArgs();
        args.addEquals(sql, "service_id", serviceId);
        args.addEquals(sql, "endpoint_code", endpointCode);
        sql.append(" ORDER BY checked_at DESC, health_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
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
        QueryArgs args = new QueryArgs();
        args.addEquals(sql, "service_id", serviceId);
        args.addEquals(sql, "endpoint_code", endpointCode);
        args.addEquals(sql, "active_yn", activeYn);
        sql.append(" ORDER BY priority, policy_id LIMIT ?");
        args.add(safeLimit(limit));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
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
        QueryArgs args = new QueryArgs();
        args.addEquals(sql, "service_id", serviceId);
        args.addEquals(sql, "endpoint_code", endpointCode);
        sql.append(" ORDER BY service_id, endpoint_code, instance_id LIMIT ?");
        args.add(safeLimit(limit));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
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
        QueryArgs args = new QueryArgs();
        args.addEquals(sql, "service_id", serviceId);
        args.addEquals(sql, "transaction_global_id", transactionGlobalId);
        sql.append(" ORDER BY call_id DESC LIMIT ?");
        args.add(safeLimit(limit));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    private boolean tableAvailable(String tableName) {
        try {
            Integer count = pfwJdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = ?
                    """, Integer.class, tableName);
            return count != null && count > 0;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private int safeLimit(int limit) {
        int resolved = limit <= 0 ? DEFAULT_LIMIT : limit;
        return Math.max(1, Math.min(resolved, MAX_LIMIT));
    }

    private static final class QueryArgs {
        private final java.util.ArrayList<Object> values = new java.util.ArrayList<>();

        private void addEquals(StringBuilder sql, String columnName, String value) {
            if (value != null && !value.isBlank()) {
                sql.append(" AND ").append(columnName).append(" = ?");
                values.add(value.trim());
            }
        }

        private void add(Object value) {
            values.add(value);
        }

        private Object[] toArray() {
            return values.toArray();
        }
    }
}
