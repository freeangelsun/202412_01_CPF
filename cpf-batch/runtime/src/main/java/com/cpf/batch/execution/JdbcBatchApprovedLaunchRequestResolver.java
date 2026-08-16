package com.cpf.batch.execution;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

/** Scheduler Trigger를 사전 승인된 immutable Launch Request로 해석합니다. */
public final class JdbcBatchApprovedLaunchRequestResolver implements BatchApprovedLaunchRequestResolver {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CpfVendorSqlCatalog sql;

    public JdbcBatchApprovedLaunchRequestResolver(
            JdbcTemplate jdbc, ObjectMapper mapper, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Override
    public BatchApprovedLaunchRequest resolve(TriggerContext context) {
        String json = jdbc.queryForObject(
                sql.required("execution-approved-launch-find-trigger"),
                String.class,
                context.jobId(),
                context.definitionVersion(),
                context.definitionChecksum());
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
        String json = jdbc.queryForObject(
                sql.required("execution-approved-launch-find-manual"), String.class, context.approvalId());
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
