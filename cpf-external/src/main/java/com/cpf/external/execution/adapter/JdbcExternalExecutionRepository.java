package com.cpf.external.execution.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.external.execution.domain.ExternalEndpointPolicy;
import com.cpf.external.execution.domain.ExternalExecution;
import com.cpf.external.execution.port.ExternalExecutionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** EXS 소유 테이블만 접근하는 JDBC 저장소 구현입니다. */
@Repository
public class JdbcExternalExecutionRepository implements ExternalExecutionRepository {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcExternalExecutionRepository(
            @Qualifier("exsJdbcTemplate") JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ExternalEndpointPolicy> findEndpointPolicy(String institutionCode, String endpointCode) {
        List<ExternalEndpointPolicy> rows = jdbcTemplate.query("""
                SELECT institution_code, endpoint_code, service_id, endpoint_uri,
                       result_query_uri, timeout_ms, retry_count
                  FROM exs_endpoint endpoint
                 WHERE endpoint.institution_code = ?
                   AND endpoint.endpoint_code = ?
                   AND endpoint.enabled_yn = 'Y'
                   AND NOT EXISTS (
                       SELECT 1
                         FROM exs_control_policy policy
                        WHERE policy.institution_code = endpoint.institution_code
                          AND policy.control_type IN ('ALL', 'SEND')
                          AND policy.enabled_yn = 'N'
                   )
                """, (rs, rowNum) -> new ExternalEndpointPolicy(
                rs.getString("institution_code"),
                rs.getString("endpoint_code"),
                rs.getString("service_id"),
                rs.getString("endpoint_uri"),
                rs.getString("result_query_uri"),
                rs.getInt("timeout_ms"),
                rs.getInt("retry_count")), institutionCode, endpointCode);
        return rows.stream().findFirst();
    }

    @Override
    public Optional<ExternalExecution> findByIdempotencyKey(String idempotencyKey) {
        return find("idempotency_key", idempotencyKey);
    }

    @Override
    public Optional<ExternalExecution> findByExecutionId(String executionId) {
        return find("execution_id", executionId);
    }

    private Optional<ExternalExecution> find(String column, String value) {
        List<ExternalExecution> rows = jdbcTemplate.query("""
                SELECT execution_id, institution_code, endpoint_code, external_request_id,
                       idempotency_key, request_hash, execution_status, response_json,
                       unknown_result_id, failure_code, failure_message, created_at, updated_at
                  FROM exs_execution
                 WHERE %s = ?
                """.formatted(column), (rs, rowNum) -> new ExternalExecution(
                rs.getString("execution_id"),
                rs.getString("institution_code"),
                rs.getString("endpoint_code"),
                rs.getString("external_request_id"),
                rs.getString("idempotency_key"),
                rs.getString("request_hash"),
                ExternalExecution.Status.valueOf(rs.getString("execution_status")),
                readJson(rs.getString("response_json")),
                rs.getString("unknown_result_id"),
                rs.getString("failure_code"),
                rs.getString("failure_message"),
                instant(rs.getTimestamp("created_at")),
                instant(rs.getTimestamp("updated_at"))), value);
        return rows.stream().findFirst();
    }

    @Override
    public void insert(ExternalExecution execution) {
        jdbcTemplate.update("""
                INSERT INTO exs_execution (
                    execution_id, institution_code, endpoint_code, external_request_id,
                    idempotency_key, request_hash, execution_status, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'EXS', 'EXS')
                """, execution.executionId(), execution.institutionCode(), execution.endpointCode(),
                execution.externalRequestId(), execution.idempotencyKey(), execution.requestHash(),
                execution.status().name());
    }

    @Override
    public void complete(String executionId, Map<String, Object> response) {
        updateResult(executionId, ExternalExecution.Status.COMPLETED, writeJson(response), null, null, null);
    }

    @Override
    public void fail(String executionId, String failureCode, String failureMessage) {
        updateResult(executionId, ExternalExecution.Status.FAILED, null, null, failureCode, failureMessage);
    }

    @Override
    public void markUnknown(String executionId, String unknownResultId, String failureCode, String failureMessage) {
        updateResult(executionId, ExternalExecution.Status.UNKNOWN_RESULT, null, unknownResultId, failureCode, failureMessage);
    }

    private void updateResult(
            String executionId,
            ExternalExecution.Status status,
            String responseJson,
            String unknownResultId,
            String failureCode,
            String failureMessage) {
        jdbcTemplate.update("""
                UPDATE exs_execution
                   SET execution_status = ?, response_json = ?, unknown_result_id = ?,
                       failure_code = ?, failure_message = ?, updated_by = 'EXS', updated_at = CURRENT_TIMESTAMP(3)
                 WHERE execution_id = ?
                """, status.name(), responseJson, unknownResultId, failureCode, failureMessage, executionId);
    }

    @Override
    @Transactional(transactionManager = "exsTransactionManager")
    public void reconcile(String executionId, ExternalExecution.Status status, String operatorId, String reason) {
        Map<String, Object> before = jdbcTemplate.queryForMap("""
                SELECT execution_status, unknown_result_id
                  FROM exs_execution
                 WHERE execution_id = ? AND execution_status = 'UNKNOWN_RESULT'
                 FOR UPDATE
                """, executionId);
        int updated = jdbcTemplate.update("""
                UPDATE exs_execution
                   SET execution_status = ?, recovery_operator_id = ?, recovery_reason = ?,
                       recovered_at = CURRENT_TIMESTAMP(3), updated_by = ?, updated_at = CURRENT_TIMESTAMP(3)
                 WHERE execution_id = ? AND execution_status = 'UNKNOWN_RESULT'
                """, status.name(), operatorId, reason, operatorId, executionId);
        if (updated != 1) {
            throw new IllegalStateException("결과 불명 상태가 변경되어 복구를 완료할 수 없습니다.");
        }
        jdbcTemplate.update("""
                INSERT INTO exs_reconciliation_log (
                    execution_id, unknown_result_id, before_status, after_status,
                    operator_id, audit_reason, source_type, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, executionId, String.valueOf(before.get("unknown_result_id")),
                String.valueOf(before.get("execution_status")), status.name(), operatorId, reason,
                "EXS_RECONCILIATION", operatorId, operatorId);
    }

    private Map<String, Object> readJson(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 대외 응답 JSON을 읽을 수 없습니다.", exception);
        }
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("대외 응답 JSON을 저장할 수 없습니다.", exception);
        }
    }

    private Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
