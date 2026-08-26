package com.cpf.integration.http.internal.servicecall;

import com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort;

import com.cpf.core.api.context.CpfContexts;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.TreeMap;
import java.time.Instant;
import java.util.LinkedHashMap;

/**
 * CPF 서비스 레지스트리 테이블을 조회하고 호출 이력을 기록하는 JDBC 저장소입니다.
 *
 * <p>cpfDB가 아직 설치되지 않은 개발 환경에서는 빈 결과를 반환해 애플리케이션 기동을 막지 않습니다.
 * 운영 조회 API는 빈 결과와 tableAvailable 정보를 함께 보여 주어 미설치 상태를 명확하게 확인하게 합니다.</p>
 */
public class CpfServiceRegistryRepository {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public CpfServiceRegistryRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    public boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null || dataSourceProvider.getIfAvailable() != null;
    }

    public boolean tableAvailable(String tableName) {
        if (!available() || !hasText(tableName)) return false;
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            JdbcTemplate candidate = jdbcTemplateProvider.getIfAvailable();
            dataSource = candidate == null ? null : candidate.getDataSource();
        }
        if (dataSource == null) return false;
        String requested = tableName.trim();
        try (Connection connection = dataSource.getConnection()) {
            for (String candidate : List.of(requested, requested.toUpperCase(java.util.Locale.ROOT), requested.toLowerCase(java.util.Locale.ROOT))) {
                try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, candidate, new String[]{"TABLE"})) {
                    if (tables.next()) return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            return false;
        }
    }

    public List<Map<String, Object>> findServices(String serviceId, String useYn, int limit) {
        if (!tableAvailable("OPS_SERVICE")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT service_id AS serviceId,
                       service_name AS serviceName,
                       service_type AS serviceType,
                       owner_module_code AS ownerModuleCode,
                       description AS description,
                       use_yn AS useYn,
                       row_version AS rowVersion,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM OPS_SERVICE
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "use_yn", useYn);
        sql.append(" ORDER BY service_id");
        return limited(jdbc().queryForList(sql.toString(), args.toArray()), limit);
    }

    public List<Map<String, Object>> findEndpoints(String serviceId, String endpointCode, String useYn, int limit) {
        if (!tableAvailable("OPS_SERVICE_ENDPOINT")) {
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
                       row_version AS rowVersion,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM OPS_SERVICE_ENDPOINT
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        appendEquals(sql, args, "use_yn", useYn);
        sql.append(" ORDER BY service_id, endpoint_code");
        return limited(jdbc().queryForList(sql.toString(), args.toArray()), limit);
    }

    public List<Map<String, Object>> findInstances(String serviceId, String endpointCode, String status, int limit) {
        if (!tableAvailable("OPS_SERVICE_INSTANCE")) {
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
                       priority_no AS priorityNo,
                       environment_code AS environmentCode,
                       zone_code AS zoneCode,
                       cell_code AS cellCode,
                       maintenance_yn AS maintenanceYn,
                       drain_yn AS drainYn,
                       drain_deadline_at AS drainDeadlineAt,
                       row_version AS rowVersion,
                       active_yn AS activeYn,
                       last_heartbeat_at AS lastHeartbeatAt,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM OPS_SERVICE_INSTANCE
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        appendEquals(sql, args, "instance_status", status);
        sql.append(" ORDER BY service_id, endpoint_code, priority_no, weight DESC, instance_id");
        return limited(jdbc().queryForList(sql.toString(), args.toArray()), limit);
    }

    public List<Map<String, Object>> findHealthStatuses(String serviceId, String endpointCode, int limit) {
        if (!tableAvailable("OPS_SERVICE_HEALTH_STATUS")) {
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
                FROM OPS_SERVICE_HEALTH_STATUS
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        sql.append(" ORDER BY checked_at DESC, health_id DESC");
        return limited(jdbc().queryForList(sql.toString(), args.toArray()), limit);
    }

    public List<Map<String, Object>> findRoutingPolicies(String serviceId, String endpointCode, String activeYn, int limit) {
        if (!tableAvailable("OPS_SERVICE_ROUTING_POLICY")) {
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
                FROM OPS_SERVICE_ROUTING_POLICY
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        appendEquals(sql, args, "active_yn", activeYn);
        sql.append(" ORDER BY priority, policy_id");
        return limited(jdbc().queryForList(sql.toString(), args.toArray()), limit);
    }

    public List<Map<String, Object>> findCircuitStates(String serviceId, String endpointCode, int limit) {
        if (!tableAvailable("OPS_SERVICE_CIRCUIT_STATE")) {
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
                FROM OPS_SERVICE_CIRCUIT_STATE
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "endpoint_code", endpointCode);
        sql.append(" ORDER BY service_id, endpoint_code, instance_id");
        return limited(jdbc().queryForList(sql.toString(), args.toArray()), limit);
    }

    public Optional<Map<String, Object>> findCircuitState(ServiceCallResolvedTarget target) {
        if (!tableAvailable("OPS_SERVICE_CIRCUIT_STATE") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
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
                    FROM OPS_SERVICE_CIRCUIT_STATE
                    WHERE service_id = ?
                      AND endpoint_code = ?
                      AND (instance_id = ? OR (? IS NULL AND instance_id IS NULL))
                    ORDER BY circuit_id DESC
                    """, target.serviceId(), target.endpointCode(), target.instanceId(), target.instanceId());
            return rows.stream().findFirst();
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Map<String, Object>> findCallHistory(String serviceId, String transactionId, int limit) {
        if (!tableAvailable("OPS_SERVICE_CALL_HISTORY")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT call_id AS callId,
                       transaction_id AS transactionId,
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
                FROM OPS_SERVICE_CALL_HISTORY
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "service_id", serviceId);
        appendEquals(sql, args, "transaction_id", transactionId);
        sql.append(" ORDER BY call_id DESC");
        return limited(jdbc().queryForList(sql.toString(), args.toArray()), limit);
    }

    public void insertCallHistory(
            ServiceCallRequest request,
            ServiceCallResolvedTarget target,
            String callStatus,
            Integer httpStatus,
            long durationMillis,
            String failureCode,
            String failureMessage) {
        if (!tableAvailable("OPS_SERVICE_CALL_HISTORY")) {
            return;
        }
        jdbc().update("""
                INSERT INTO OPS_SERVICE_CALL_HISTORY (
                    transaction_id, trace_id, service_id, endpoint_code, instance_id,
                    http_method, request_path, call_status, http_status, duration_ms,
                    timeout_ms, retry_count, failure_code, failure_message, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CPF_SERVICE_CALL', 'CPF_SERVICE_CALL')
                """,
                CpfContexts.currentTransactionId(),
                CpfContexts.current() == null ? null : CpfContexts.current().traceId(),
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
        if (!tableAvailable("OPS_SERVICE_HEALTH_STATUS") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
            return;
        }
        try {
            jdbc().update("""
                    INSERT INTO OPS_SERVICE_HEALTH_STATUS (
                        service_id, endpoint_code, instance_id, health_status, http_status,
                        response_time_ms, failure_message, checked_at, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'CPF_SERVICE_CALL', 'CPF_SERVICE_CALL')
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
        if (!tableAvailable("OPS_SERVICE_CIRCUIT_STATE") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) return;
        try {
            int updated = jdbc().update("""
                    UPDATE OPS_SERVICE_CIRCUIT_STATE
                       SET circuit_state='CLOSED', failure_count=0, success_count=success_count+1,
                           closed_at=CURRENT_TIMESTAMP, last_failure_message=NULL,
                           updated_by='CPF_SERVICE_CALL', updated_at=CURRENT_TIMESTAMP
                     WHERE service_id=? AND endpoint_code=?
                       AND (instance_id=? OR (instance_id IS NULL AND ? IS NULL))
                    """, target.serviceId(), target.endpointCode(), target.instanceId(), target.instanceId());
            if (updated == 0) {
                try {
                    jdbc().update("""
                            INSERT INTO OPS_SERVICE_CIRCUIT_STATE
                            (service_id,endpoint_code,instance_id,circuit_state,failure_count,success_count,closed_at,created_by,updated_by)
                            VALUES (?,?,?,'CLOSED',0,1,CURRENT_TIMESTAMP,'CPF_SERVICE_CALL','CPF_SERVICE_CALL')
                            """, target.serviceId(), target.endpointCode(), target.instanceId());
                } catch (DuplicateKeyException raced) {
                    recordCircuitSuccess(target);
                }
            }
        } catch (DataAccessException ignored) {
            // Circuit 상태 기록 실패는 호출 본문 성공을 실패로 바꾸지 않습니다.
        }
    }

    public void recordCircuitHalfOpen(ServiceCallResolvedTarget target) {
        if (!tableAvailable("OPS_SERVICE_CIRCUIT_STATE") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) {
            return;
        }
        try {
            jdbc().update("""
                    UPDATE OPS_SERVICE_CIRCUIT_STATE
                    SET circuit_state = 'HALF_OPEN',
                        half_opened_at = CURRENT_TIMESTAMP,
                        updated_by = 'CPF_SERVICE_CALL',
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
        if (!tableAvailable("OPS_SERVICE_CIRCUIT_STATE") || !hasText(target.serviceId()) || !hasText(target.endpointCode())) return;
        int resolvedThreshold = Math.max(1, threshold);
        try {
            int updated = jdbc().update("""
                    UPDATE OPS_SERVICE_CIRCUIT_STATE
                       SET failure_count=failure_count+1,
                           success_count=0,
                           circuit_state=CASE WHEN failure_count+1 >= ? THEN 'OPEN' ELSE 'CLOSED' END,
                           opened_at=CASE WHEN failure_count+1 >= ? THEN CURRENT_TIMESTAMP ELSE opened_at END,
                           last_failure_message=?, updated_by='CPF_SERVICE_CALL', updated_at=CURRENT_TIMESTAMP
                     WHERE service_id=? AND endpoint_code=?
                       AND (instance_id=? OR (instance_id IS NULL AND ? IS NULL))
                    """, resolvedThreshold, resolvedThreshold, truncate(failureMessage,900),
                    target.serviceId(), target.endpointCode(), target.instanceId(), target.instanceId());
            if (updated == 0) {
                String state = resolvedThreshold <= 1 ? "OPEN" : "CLOSED";
                try {
                    jdbc().update("""
                            INSERT INTO OPS_SERVICE_CIRCUIT_STATE
                            (service_id,endpoint_code,instance_id,circuit_state,failure_count,success_count,opened_at,last_failure_message,created_by,updated_by)
                            VALUES (?,?,?, ?,1,0,CASE WHEN ?='OPEN' THEN CURRENT_TIMESTAMP ELSE NULL END,?,'CPF_SERVICE_CALL','CPF_SERVICE_CALL')
                            """, target.serviceId(), target.endpointCode(), target.instanceId(), state, state, truncate(failureMessage,900));
                } catch (DuplicateKeyException raced) {
                    recordCircuitFailure(target, failureMessage, resolvedThreshold);
                }
            }
        } catch (DataAccessException ignored) {
            // Circuit 보조 상태 기록 실패가 원 호출 실패 사유를 덮어쓰지 않습니다.
        }
    }

    private void updateInstanceHealth(ServiceCallResolvedTarget target, String healthStatus) {
        if (!tableAvailable("OPS_SERVICE_INSTANCE") || !hasText(target.instanceId())) {
            return;
        }
        String instanceStatus = "UP".equalsIgnoreCase(healthStatus) ? "UP" : "DOWN";
        jdbc().update("""
                UPDATE OPS_SERVICE_INSTANCE
                SET instance_status = ?,
                    last_heartbeat_at = CURRENT_TIMESTAMP,
                    updated_by = 'CPF_SERVICE_CALL',
                    updated_at = CURRENT_TIMESTAMP
                WHERE instance_id = ?
                """, instanceStatus, target.instanceId());
    }


    @org.springframework.transaction.annotation.Transactional(transactionManager = "cpfTransactionManager")
    public Map<String,Object> saveService(CpfServiceRegistryControlPort.ServiceDefinition c) {
        requireRegistryCommand(c.operationId(),c.reason(),c.requestedBy()); require(c.serviceId(),"serviceId"); require(c.serviceName(),"serviceName"); require(c.ownerModuleCode(),"ownerModuleCode");
        Map<String,Object> fp=new LinkedHashMap<>();fp.put("serviceId",c.serviceId());fp.put("serviceName",c.serviceName());fp.put("serviceType",textOr(c.serviceType(),"INTERNAL"));fp.put("ownerModuleCode",c.ownerModuleCode());fp.put("description",textOr(c.description(),""));fp.put("useYn",yn(c.useYn(),"useYn"));fp.put("expectedVersion",c.expectedVersion());fp.put("reason",c.reason());
        if(replayOperation(c.operationId(),"SERVICE_REGISTRY_SERVICE",canonicalHash(fp))) return findEntity("OPS_SERVICE","service_id",c.serviceId());
        List<Map<String,Object>> rows=jdbc().queryForList("SELECT row_version FROM OPS_SERVICE WHERE service_id=? FOR UPDATE",c.serviceId());
        if(rows.isEmpty()) { if(c.expectedVersion()!=null&&c.expectedVersion()!=0)throw new CpfServiceRegistryVersionConflictException(c.expectedVersion(),0); jdbc().update("INSERT INTO OPS_SERVICE(service_id,service_name,service_type,owner_module_code,description,use_yn,row_version,created_by,updated_by) VALUES (?,?,?,?,?,?,0,?,?)",c.serviceId(),c.serviceName(),textOr(c.serviceType(),"INTERNAL"),c.ownerModuleCode(),emptyToNull(c.description()),yn(c.useYn(),"useYn"),c.requestedBy(),c.requestedBy()); }
        else { long v=number(rows.getFirst().get("row_version")); requireVersion(c.expectedVersion(),v); if(jdbc().update("UPDATE OPS_SERVICE SET service_name=?,service_type=?,owner_module_code=?,description=?,use_yn=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE service_id=? AND row_version=?",c.serviceName(),textOr(c.serviceType(),"INTERNAL"),c.ownerModuleCode(),emptyToNull(c.description()),yn(c.useYn(),"useYn"),c.requestedBy(),c.serviceId(),v)!=1)throw new CpfServiceRegistryVersionConflictException(v,v); }
        completeOperation(c.operationId(),c.serviceId()); return findEntity("OPS_SERVICE","service_id",c.serviceId());
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "cpfTransactionManager")
    public Map<String,Object> saveEndpoint(CpfServiceRegistryControlPort.EndpointDefinition c) {
        requireRegistryCommand(c.operationId(),c.reason(),c.requestedBy());require(c.endpointCode(),"endpointCode");require(c.serviceId(),"serviceId");require(c.endpointName(),"endpointName");require(c.baseUrl(),"baseUrl");
        if(jdbc().queryForObject("SELECT COUNT(*) FROM OPS_SERVICE WHERE service_id=? AND use_yn='Y'",Integer.class,c.serviceId())!=1)throw new IllegalArgumentException("활성 service가 없습니다: "+c.serviceId());
        Map<String,Object> fp=new LinkedHashMap<>();fp.put("endpointCode",c.endpointCode());fp.put("serviceId",c.serviceId());fp.put("endpointName",c.endpointName());fp.put("endpointType",textOr(c.endpointType(),"HTTP"));fp.put("baseUrl",c.baseUrl());fp.put("contextPath",textOr(c.contextPath(),""));fp.put("timeout",positive(c.defaultTimeoutMs(),3000));fp.put("retry",nonNegative(c.defaultRetryCount(),0));fp.put("useYn",yn(c.useYn(),"useYn"));fp.put("expectedVersion",c.expectedVersion());fp.put("reason",c.reason());
        if(replayOperation(c.operationId(),"SERVICE_REGISTRY_ENDPOINT",canonicalHash(fp)))return findEntity("OPS_SERVICE_ENDPOINT","endpoint_code",c.endpointCode());
        List<Map<String,Object>> rows=jdbc().queryForList("SELECT row_version FROM OPS_SERVICE_ENDPOINT WHERE endpoint_code=? FOR UPDATE",c.endpointCode());
        if(rows.isEmpty()){if(c.expectedVersion()!=null&&c.expectedVersion()!=0)throw new CpfServiceRegistryVersionConflictException(c.expectedVersion(),0);jdbc().update("INSERT INTO OPS_SERVICE_ENDPOINT(endpoint_code,service_id,endpoint_name,endpoint_type,base_url,context_path,default_timeout_ms,default_retry_count,use_yn,row_version,created_by,updated_by) VALUES (?,?,?,?,?,?,?,?,?,0,?,?)",c.endpointCode(),c.serviceId(),c.endpointName(),textOr(c.endpointType(),"HTTP"),c.baseUrl(),emptyToNull(c.contextPath()),positive(c.defaultTimeoutMs(),3000),nonNegative(c.defaultRetryCount(),0),yn(c.useYn(),"useYn"),c.requestedBy(),c.requestedBy());}
        else {long v=number(rows.getFirst().get("row_version"));requireVersion(c.expectedVersion(),v);if(jdbc().update("UPDATE OPS_SERVICE_ENDPOINT SET service_id=?,endpoint_name=?,endpoint_type=?,base_url=?,context_path=?,default_timeout_ms=?,default_retry_count=?,use_yn=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE endpoint_code=? AND row_version=?",c.serviceId(),c.endpointName(),textOr(c.endpointType(),"HTTP"),c.baseUrl(),emptyToNull(c.contextPath()),positive(c.defaultTimeoutMs(),3000),nonNegative(c.defaultRetryCount(),0),yn(c.useYn(),"useYn"),c.requestedBy(),c.endpointCode(),v)!=1)throw new CpfServiceRegistryVersionConflictException(v,v);}
        completeOperation(c.operationId(),c.endpointCode());return findEntity("OPS_SERVICE_ENDPOINT","endpoint_code",c.endpointCode());
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "cpfTransactionManager")
    public Map<String,Object> saveInstance(CpfServiceRegistryControlPort.InstanceDefinition c) {
        requireRegistryCommand(c.operationId(),c.reason(),c.requestedBy());require(c.instanceId(),"instanceId");require(c.serviceId(),"serviceId");require(c.endpointCode(),"endpointCode");require(c.instanceName(),"instanceName");require(c.baseUrl(),"baseUrl");
        if(jdbc().queryForObject("SELECT COUNT(*) FROM OPS_SERVICE_ENDPOINT WHERE endpoint_code=? AND service_id=? AND use_yn='Y'",Integer.class,c.endpointCode(),c.serviceId())!=1)throw new IllegalArgumentException("활성 endpoint가 없습니다: "+c.serviceId()+"/"+c.endpointCode());
        Map<String,Object> fp=new LinkedHashMap<>();fp.put("instanceId",c.instanceId());fp.put("serviceId",c.serviceId());fp.put("endpointCode",c.endpointCode());fp.put("baseUrl",c.baseUrl());fp.put("weight",positive(c.weight(),100));fp.put("priority",positive(c.priorityNo(),100));fp.put("environment",textOr(c.environmentCode(),"default"));fp.put("active",yn(c.activeYn(),"activeYn"));fp.put("maintenance",ynDefault(c.maintenanceYn(),"N","maintenanceYn"));fp.put("drain",ynDefault(c.drainYn(),"N","drainYn"));fp.put("expectedVersion",c.expectedVersion());fp.put("reason",c.reason());
        if(replayOperation(c.operationId(),"SERVICE_REGISTRY_INSTANCE",canonicalHash(fp)))return findEntity("OPS_SERVICE_INSTANCE","instance_id",c.instanceId());
        List<Map<String,Object>> rows=jdbc().queryForList("SELECT row_version FROM OPS_SERVICE_INSTANCE WHERE instance_id=? FOR UPDATE",c.instanceId());
        if(rows.isEmpty()){if(c.expectedVersion()!=null&&c.expectedVersion()!=0)throw new CpfServiceRegistryVersionConflictException(c.expectedVersion(),0);jdbc().update("INSERT INTO OPS_SERVICE_INSTANCE(instance_id,service_id,endpoint_code,instance_name,base_url,host_name,port_no,instance_status,weight,active_yn,environment_code,zone_code,cell_code,priority_no,maintenance_yn,drain_yn,row_version,created_by,updated_by) VALUES (?,?,?,?,?,?,?,'UP',?,?,?,?,?,?,?, ?,0,?,?)",c.instanceId(),c.serviceId(),c.endpointCode(),c.instanceName(),c.baseUrl(),emptyToNull(c.hostName()),c.portNo(),positive(c.weight(),100),yn(c.activeYn(),"activeYn"),textOr(c.environmentCode(),"default"),emptyToNull(c.zoneCode()),emptyToNull(c.cellCode()),positive(c.priorityNo(),100),ynDefault(c.maintenanceYn(),"N","maintenanceYn"),ynDefault(c.drainYn(),"N","drainYn"),c.requestedBy(),c.requestedBy());}
        else {long v=number(rows.getFirst().get("row_version"));requireVersion(c.expectedVersion(),v);if(jdbc().update("UPDATE OPS_SERVICE_INSTANCE SET service_id=?,endpoint_code=?,instance_name=?,base_url=?,host_name=?,port_no=?,weight=?,active_yn=?,environment_code=?,zone_code=?,cell_code=?,priority_no=?,maintenance_yn=?,drain_yn=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE instance_id=? AND row_version=?",c.serviceId(),c.endpointCode(),c.instanceName(),c.baseUrl(),emptyToNull(c.hostName()),c.portNo(),positive(c.weight(),100),yn(c.activeYn(),"activeYn"),textOr(c.environmentCode(),"default"),emptyToNull(c.zoneCode()),emptyToNull(c.cellCode()),positive(c.priorityNo(),100),ynDefault(c.maintenanceYn(),"N","maintenanceYn"),ynDefault(c.drainYn(),"N","drainYn"),c.requestedBy(),c.instanceId(),v)!=1)throw new CpfServiceRegistryVersionConflictException(v,v);}
        completeOperation(c.operationId(),c.instanceId());return findEntity("OPS_SERVICE_INSTANCE","instance_id",c.instanceId());
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "cpfTransactionManager")
    public void deleteService(String serviceId,CpfServiceRegistryControlPort.DeleteCommand c){deleteEntity("OPS_SERVICE","service_id",serviceId,c,"SERVICE_REGISTRY_SERVICE_DELETE",List.of("OPS_SERVICE_ENDPOINT","service_id"));}
    @org.springframework.transaction.annotation.Transactional(transactionManager = "cpfTransactionManager")
    public void deleteEndpoint(String endpointCode,CpfServiceRegistryControlPort.DeleteCommand c){deleteEntity("OPS_SERVICE_ENDPOINT","endpoint_code",endpointCode,c,"SERVICE_REGISTRY_ENDPOINT_DELETE",List.of("OPS_SERVICE_INSTANCE","endpoint_code"));}
    @org.springframework.transaction.annotation.Transactional(transactionManager = "cpfTransactionManager")
    public void deleteInstance(String instanceId,CpfServiceRegistryControlPort.DeleteCommand c){deleteEntity("OPS_SERVICE_INSTANCE","instance_id",instanceId,c,"SERVICE_REGISTRY_INSTANCE_DELETE",List.of());}

    private void deleteEntity(String table,String key,String id,CpfServiceRegistryControlPort.DeleteCommand c,String type,List<String> child){requireRegistryCommand(c.operationId(),c.reason(),c.requestedBy());require(id,key);String hash=canonicalHash(Map.of("id",id,"expectedVersion",c.expectedVersion()==null?-1:c.expectedVersion(),"reason",c.reason()));if(replayOperation(c.operationId(),type,hash))return;List<Map<String,Object>> rows=jdbc().queryForList("SELECT row_version FROM "+table+" WHERE "+key+"=? FOR UPDATE",id);if(rows.isEmpty()){completeOperation(c.operationId(),id);return;}long v=number(rows.getFirst().get("row_version"));requireVersion(c.expectedVersion(),v);if(!child.isEmpty()){Integer cnt=jdbc().queryForObject("SELECT COUNT(*) FROM "+child.get(0)+" WHERE "+child.get(1)+"=?",Integer.class,id);if(cnt!=null&&cnt>0)throw new IllegalStateException("하위 Registry 항목이 있어 삭제할 수 없습니다: "+id);}if(jdbc().update("DELETE FROM "+table+" WHERE "+key+"=? AND row_version=?",id,v)!=1)throw new CpfServiceRegistryVersionConflictException(v,v);completeOperation(c.operationId(),id);}

    private boolean replayOperation(String operationId,String type,String hash){if(!tableAvailable("OPS_CONTROL_OPERATION"))throw new IllegalStateException("OPS_CONTROL_OPERATION table이 필요합니다. V64 migration을 적용하십시오.");List<Map<String,Object>> rows=jdbc().queryForList("SELECT request_hash,result_state FROM OPS_CONTROL_OPERATION WHERE operation_id=? FOR UPDATE",operationId);if(!rows.isEmpty()){Map<String,Object> row=rows.getFirst();if(!hash.equals(String.valueOf(row.get("request_hash"))))throw new IllegalStateException("operationId payload fingerprint 충돌: "+operationId);String state=String.valueOf(row.get("result_state"));if("SUCCESS".equals(state))return true;throw new IllegalStateException("operationId 처리 상태가 완료되지 않았습니다: "+operationId+", state="+state);}try{jdbc().update("INSERT INTO OPS_CONTROL_OPERATION(operation_id,command_type,request_hash,result_state,expires_at,created_by,updated_by) VALUES (?,?,?,'PROCESSING',?, ?,?)",operationId,type,hash,Timestamp.from(Instant.now().plusSeconds(604800)),"CPF_REGISTRY","CPF_REGISTRY");return false;}catch(DuplicateKeyException raced){return replayOperation(operationId,type,hash);}}
    private void completeOperation(String operationId,String entityId){if(jdbc().update("UPDATE OPS_CONTROL_OPERATION SET entity_id=?,result_state='SUCCESS',updated_at=CURRENT_TIMESTAMP WHERE operation_id=? AND result_state='PROCESSING'",entityId,operationId)!=1)throw new IllegalStateException("Registry operation 결과 저장 실패: "+operationId);}
    private Map<String,Object> findEntity(String table,String key,String id){List<Map<String,Object>> rows=jdbc().queryForList("SELECT * FROM "+table+" WHERE "+key+"=?",id);if(rows.isEmpty())throw new IllegalStateException("Registry 결과를 찾을 수 없습니다: "+id);return rows.getFirst();}
    private void requireRegistryCommand(String operationId,String reason,String requestedBy){require(operationId,"operationId");require(reason,"reason");require(requestedBy,"requestedBy");}
    private void requireVersion(Long expected,long current){if(expected==null||expected.longValue()!=current)throw new CpfServiceRegistryVersionConflictException(expected==null?-1:expected,current);}
    private long number(Object v){return v==null?0:((Number)v).longValue();}
    private int positive(Integer v,int d){return v==null?d:Math.max(1,v);}
    private int nonNegative(Integer v,int d){return v==null?d:Math.max(0,v);}
    private String textOr(String v,String d){return hasText(v)?v.trim():d;}
    private String emptyToNull(String v){return hasText(v)?v.trim():null;}
    private String yn(String v,String name){return ynDefault(v,"Y",name);}
    private String ynDefault(String v,String d,String name){String x=hasText(v)?v.trim().toUpperCase(java.util.Locale.ROOT):d;if(!"Y".equals(x)&&!"N".equals(x))throw new IllegalArgumentException(name+"는 Y/N만 허용합니다.");return x;}
    private String require(String v,String name){if(!hasText(v))throw new IllegalArgumentException(name+"가 필요합니다.");return v.trim();}

    /**
     * 운영 Drain/Disable/Resume 명령을 operationId·Version으로 원자 처리합니다.
     * Body Actor를 신뢰하지 않으며 Public Controller가 인증 Actor로 재구성한 Command만 받습니다.
     */
    @org.springframework.transaction.annotation.Transactional(transactionManager = "cpfTransactionManager")
    public Map<String,Object> changeInstanceState(
            String serviceId, String endpointCode, String instanceId,
            CpfServiceRegistryControlPort.InstanceStateCommand command) {
        if (!tableAvailable("OPS_SERVICE_INSTANCE")) {
            throw new IllegalStateException("OPS_SERVICE_INSTANCE table is unavailable");
        }
        requireRegistryCommand(command.operationId(), command.reason(), command.requestedBy());
        require(serviceId, "serviceId"); require(endpointCode, "endpointCode"); require(instanceId, "instanceId");
        Map<String,Object> fingerprint = new LinkedHashMap<>();
        fingerprint.put("serviceId", serviceId);
        fingerprint.put("endpointCode", endpointCode);
        fingerprint.put("instanceId", instanceId);
        fingerprint.put("command", command.command().name());
        fingerprint.put("expectedVersion", command.expectedVersion());
        fingerprint.put("reason", command.reason());
        String hash = canonicalHash(fingerprint);
        if (replayOperation(command.operationId(), "SERVICE_REGISTRY_INSTANCE_STATE", hash)) {
            return findEntity("OPS_SERVICE_INSTANCE", "instance_id", instanceId);
        }

        List<Map<String,Object>> current = jdbc().queryForList("""
                SELECT row_version FROM OPS_SERVICE_INSTANCE
                 WHERE service_id=? AND endpoint_code=? AND instance_id=? FOR UPDATE
                """, serviceId, endpointCode, instanceId);
        if (current.isEmpty()) {
            throw new IllegalArgumentException(
                    "Service instance not found: " + serviceId + "/" + endpointCode + "/" + instanceId);
        }
        long rowVersion = number(current.getFirst().get("row_version"));
        requireVersion(command.expectedVersion(), rowVersion);

        String status;
        String activeYn;
        String drainYn;
        switch (command.command()) {
            case DRAIN -> { status = "DRAINING"; activeYn = "Y"; drainYn = "Y"; }
            case DISABLE -> { status = "DISABLED"; activeYn = "N"; drainYn = "N"; }
            case RESUME -> { status = "UP"; activeYn = "Y"; drainYn = "N"; }
            default -> throw new IllegalArgumentException("Unsupported instance command: " + command.command());
        }
        int updated = jdbc().update("""
                UPDATE OPS_SERVICE_INSTANCE
                   SET instance_status=?,active_yn=?,drain_yn=?,row_version=row_version+1,
                       updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE service_id=? AND endpoint_code=? AND instance_id=? AND row_version=?
                """, status, activeYn, drainYn, command.requestedBy(),
                serviceId, endpointCode, instanceId, rowVersion);
        if (updated != 1) {
            throw new CpfServiceRegistryVersionConflictException(rowVersion, rowVersion);
        }
        completeOperation(command.operationId(), instanceId);
        return findEntity("OPS_SERVICE_INSTANCE", "instance_id", instanceId);
    }

    /** Registry 멱등성 fingerprint를 Owner 내부에서 결정적으로 계산해 타 Module internal 구현 의존을 제거합니다. */
    private static String canonicalHash(Map<String, ?> values) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(new TreeMap<>(values).toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("cpfDataSource 또는 cpfJdbcTemplate이 필요합니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String columnName, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(columnName).append(" = ?");
            args.add(value.trim());
        }
    }

    private List<Map<String,Object>> limited(List<Map<String,Object>> rows, int limit) {
        int resolved = safeLimit(limit);
        return rows.size() <= resolved ? rows : List.copyOf(rows.subList(0, resolved));
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 1000));
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim().toUpperCase(java.util.Locale.ROOT) : null;
    }

    private String firstText(String first, String fallback) {
        return hasText(first) ? first.trim() : fallback;
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
