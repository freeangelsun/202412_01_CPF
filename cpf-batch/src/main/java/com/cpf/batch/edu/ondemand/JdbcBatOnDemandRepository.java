package com.cpf.batch.edu.ondemand;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** BAT DB에 온디맨드 접수와 최종 결과를 저장합니다. */
@Repository
public class JdbcBatOnDemandRepository implements BatOnDemandRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcBatOnDemandRepository(
            @Qualifier("batJdbcTemplate") JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public BatOnDemandStatus createOrFind(
            BatOnDemandStatus requested, String parametersJson, String reason, String requestUser) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO bat_on_demand_request (
                        execution_request_id, standard_batch_id, idempotency_key,
                        transaction_global_id, business_date, request_status,
                        parameters_json, request_reason, request_user, requested_at,
                        created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, 'REQUESTED', ?, ?, ?, CURRENT_TIMESTAMP(3), ?, ?)
                    """, requested.executionRequestId(), requested.standardBatchId(), requested.idempotencyKey(),
                    requested.transactionGlobalId(), requested.businessDate(), parametersJson,
                    reason, requestUser, requestUser, requestUser);
            return requested;
        } catch (DuplicateKeyException duplicate) {
            return findByIdempotencyKey(requested.standardBatchId(), requested.idempotencyKey())
                    .orElseThrow(() -> duplicate);
        }
    }

    @Override
    public Optional<BatOnDemandStatus> find(String executionRequestId) {
        return query("WHERE execution_request_id = ?", executionRequestId).stream().findFirst();
    }

    @Override
    public void markRunning(String executionRequestId) {
        jdbcTemplate.update("""
                UPDATE bat_on_demand_request
                SET request_status = 'RUNNING', updated_by = 'BAT_WORKER', updated_at = CURRENT_TIMESTAMP
                WHERE execution_request_id = ? AND request_status = 'REQUESTED'
                """, executionRequestId);
    }

    @Override
    public void complete(
            String executionRequestId,
            String status,
            Long cpfExecutionId,
            Long springExecutionId,
            String resultJson,
            String failureCode,
            String failureMessage) {
        jdbcTemplate.update("""
                UPDATE bat_on_demand_request
                SET request_status = ?,
                    cpf_execution_id = COALESCE(?, cpf_execution_id),
                    spring_batch_execution_id = COALESCE(?, spring_batch_execution_id),
                    result_json = ?, failure_code = ?, failure_message = ?,
                    completed_at = CURRENT_TIMESTAMP(3), updated_by = 'BAT_WORKER', updated_at = CURRENT_TIMESTAMP
                WHERE execution_request_id = ?
                """, status, cpfExecutionId, springExecutionId, resultJson,
                failureCode, failureMessage, executionRequestId);
    }

    private Optional<BatOnDemandStatus> findByIdempotencyKey(String standardBatchId, String idempotencyKey) {
        return query("WHERE standard_batch_id = ? AND idempotency_key = ?", standardBatchId, idempotencyKey)
                .stream().findFirst();
    }

    private List<BatOnDemandStatus> query(String where, Object... arguments) {
        return jdbcTemplate.query("""
                SELECT execution_request_id, standard_batch_id, idempotency_key,
                       transaction_global_id, business_date, request_status,
                       cpf_execution_id, spring_batch_execution_id, result_json,
                       failure_code, failure_message, requested_at, completed_at
                FROM bat_on_demand_request
                """ + where, (rs, rowNum) -> new BatOnDemandStatus(
                rs.getString("execution_request_id"),
                rs.getString("standard_batch_id"),
                rs.getString("idempotency_key"),
                rs.getString("transaction_global_id"),
                rs.getString("business_date"),
                rs.getString("request_status"),
                nullableLong(rs, "cpf_execution_id"),
                nullableLong(rs, "spring_batch_execution_id"),
                readMap(rs.getString("result_json")),
                rs.getString("failure_code"),
                rs.getString("failure_message"),
                instant(rs.getTimestamp("requested_at")),
                instant(rs.getTimestamp("completed_at"))), arguments);
    }

    private Map<String, Object> readMap(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() { });
        } catch (JsonProcessingException ex) {
            return Map.of("raw", value);
        }
    }

    private Long nullableLong(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
