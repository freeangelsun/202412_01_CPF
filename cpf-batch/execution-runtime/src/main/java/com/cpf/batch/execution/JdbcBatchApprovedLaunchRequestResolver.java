package com.cpf.batch.execution;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

/** Scheduler Trigger를 사전 승인된 immutable Launch Request로 해석합니다. */
public final class JdbcBatchApprovedLaunchRequestResolver implements BatchApprovedLaunchRequestResolver {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public JdbcBatchApprovedLaunchRequestResolver(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public BatchApprovedLaunchRequest resolve(TriggerContext context) {
        String json = jdbc.queryForObject("""
                select launch_request_json
                  from CPF_BATCH_APPROVED_LAUNCH
                 where job_id = ? and definition_version = ? and definition_checksum = ?
                   and approval_status = 'APPROVED'
                   and effective_from <= CURRENT_TIMESTAMP
                   and (effective_until is null or effective_until > CURRENT_TIMESTAMP)
                """, String.class, context.jobId(), context.definitionVersion(), context.definitionChecksum());
        if (json == null || json.isBlank()) throw new IllegalStateException("BATCH_APPROVED_LAUNCH_NOT_FOUND");
        try {
            BatchApprovedLaunchRequest approved = mapper.readValue(json, BatchApprovedLaunchRequest.class);
            Map<String, Object> parameters = new LinkedHashMap<>(approved.parameters());
            parameters.put("businessDate", context.businessDate().toString());
            parameters.put("scheduledAt", context.scheduledAt().toString());
            parameters.put("scheduleId", context.scheduleId());
            return new BatchApprovedLaunchRequest(
                    approved.definition(), approved.plan(), parameters, approved.approvalId(),
                    approved.operatorId(), approved.reason(), context.idempotencyKey(), context.fencingToken());
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("BATCH_APPROVED_LAUNCH_JSON_INVALID", failure);
        }
    }

    @Override
    public BatchApprovedLaunchRequest resolve(ManualContext context) {
        String json = jdbc.queryForObject("""
                select launch_request_json
                  from CPF_BATCH_APPROVED_LAUNCH
                 where approval_id = ? and approval_status = 'APPROVED'
                   and effective_from <= CURRENT_TIMESTAMP
                   and (effective_until is null or effective_until > CURRENT_TIMESTAMP)
                """, String.class, context.approvalId());
        if (json == null || json.isBlank()) throw new IllegalStateException("BATCH_APPROVAL_NOT_ACTIVE");
        try {
            BatchApprovedLaunchRequest approved = mapper.readValue(json, BatchApprovedLaunchRequest.class);
            Map<String, Object> parameters = new LinkedHashMap<>(approved.parameters());
            parameters.putAll(context.parameters());
            return new BatchApprovedLaunchRequest(
                    approved.definition(), approved.plan(), parameters, approved.approvalId(),
                    context.operatorId(), context.reason(), context.idempotencyKey(), context.fencingToken());
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("BATCH_APPROVED_LAUNCH_JSON_INVALID", failure);
        }
    }
}
