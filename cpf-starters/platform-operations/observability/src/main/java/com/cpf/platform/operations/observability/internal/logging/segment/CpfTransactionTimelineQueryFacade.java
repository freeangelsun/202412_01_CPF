package com.cpf.platform.operations.observability.internal.logging.segment;

import com.cpf.platform.operations.observability.api.logging.CpfTransactionTimelineQueryPort;
import com.cpf.security.api.CpfMaskingRuntime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CPF 소유 거래 구간 스키마를 조회하고 외부에는 테이블 독립적인 결과만 반환합니다.
 */
@Component
public class CpfTransactionTimelineQueryFacade implements CpfTransactionTimelineQueryPort {
    private final JdbcTemplate jdbcTemplate;

    public CpfTransactionTimelineQueryFacade(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        // CPF DB를 사용하지 않는 업무 앱에서도 공개 조회 포트 자체는 안전하게 기동되어야 합니다.
        this.jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
    }

    @Override
    public GroupQueryResult findGroups(Map<String, String> criteria) {
        Map<String, String> safeCriteria = criteria == null ? Map.of() : criteria;
        int limit = limit(safeCriteria.get("limit"));
        String sort = sort(safeCriteria.get("sort"));
        if (!tableAvailable()) {
            return new GroupQueryResult(false, List.of(), limit, sort, "CPF 거래 구간 저장소를 사용할 수 없습니다.");
        }
        QueryParts query = buildGroupQuery(safeCriteria, sort);
        List<Map<String, Object>> rows = enrichGroupRows(
                queryForListLimited(query.sql(), query.args(), limit),
                query).stream()
                .map(this::maskGroupRow)
                .toList();
        return new GroupQueryResult(true, rows, limit, sort, null);
    }

    @Override
    public List<Map<String, Object>> findSegments(String transactionId) {
        if (!hasText(transactionId) || !tableAvailable()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT transaction_segment_id AS transactionSegmentId,
                       transaction_id AS transactionId,
                       execution_id AS executionId,
                       parent_segment_id AS parentSegmentId,
                       transaction_role AS transactionRole,
                       module_code AS moduleCode,
                       source_module_code AS sourceModuleCode,
                       target_module_code AS targetModuleCode,
                       direction,
                       call_depth AS callDepth,
                       sequence_no AS sequenceNo,
                       api_path AS apiPath,
                       transaction_name AS transactionName,
                       started_at AS startedAt,
                       ended_at AS endedAt,
                       duration_ms AS durationMs,
                       status,
                       failure_yn AS failureYn,
                       failure_code AS failureCode,
                       failure_message_masked AS failureMessageMasked,
                       request_header_snapshot_masked AS requestHeaderSnapshotMasked,
                       response_header_snapshot_masked AS responseHeaderSnapshotMasked,
                       extension_header_snapshot_masked AS extensionHeaderSnapshotMasked,
                       customer_no_masked AS customerNoMasked,
                       member_no_masked AS memberNoMasked,
                       user_id_masked AS userIdMasked,
                       operator_id_masked AS operatorIdMasked,
                       client_id AS clientId,
                       original_system_code AS originalSystemCode,
                       system_code AS systemCode,
                       caller_system_code AS callerSystemCode,
                       target_system_code AS targetSystemCode,
                       caller_channel AS callerChannel,
                       current_channel AS currentChannel,
                       original_channel AS originalChannel,
                       target_channel AS targetChannel,
                       target_operation_id AS targetOperationId,
                       external_institution_code AS externalInstitutionCode,
                       external_transaction_id AS externalTransactionId,
                       selected_instance_id AS selectedInstanceId,
                       attempt_no AS attemptNo,
                       retry_yn AS retryYn,
                       failover_yn AS failoverYn,
                       circuit_state AS circuitState,
                       downstream_http_status AS downstreamHttpStatus,
                       result_state AS resultState,
                       unknown_result_id AS unknownResultId
                  FROM CPF_TRANSACTION_SEGMENT
                 WHERE transaction_id = ?
                 ORDER BY started_at, sequence_no, segment_id
                """, transactionId.trim()).stream()
                .map(this::maskSegmentRow)
                .toList();
    }

    @Override
    public List<Map<String, Object>> findLineage(String transactionId, int limit) {
        if (!hasText(transactionId) || jdbcTemplate == null) return List.of();
        String tx = transactionId.trim();
        int max = boundedLimit(limit);
        List<Map<String, Object>> rows = new ArrayList<>();

        // Canonical extension/ingress table: used by owners living outside cpfDB (BAT/ADM/etc.).
        if (lineageTableAvailable()) {
            rows.addAll(queryForListLimited("""
                    SELECT transaction_id AS transactionId, segment_id AS segmentId,
                           parent_segment_id AS parentSegmentId, attempt_no AS attempt,
                           trace_id AS traceId, span_id AS spanId, request_id AS requestId,
                           idempotency_key AS idempotencyKey, tenant_id AS tenantId,
                           current_channel AS currentChannel, actor_id_masked AS actorIdMasked,
                           instance_id AS instanceId, was_id AS wasId, agent_id AS agentId,
                           worker_id AS workerId, target_system_code AS remoteSystem, operation_id AS operation,
                           message_id AS messageId, consumer_group AS consumerGroup, dlq_id AS dlqId,
                           batch_job_instance_id AS batchJobInstanceId,
                           batch_job_execution_id AS batchJobExecutionId,
                           batch_step_execution_id AS batchStepExecutionId, partition_id AS partitionId,
                           file_id AS fileId, source_type AS sourceType, source_ref_id AS sourceRefId,
                           lifecycle_state AS lifecycleState, failure_stage AS failureStage,
                           unknown_yn AS unknownYn, reconcile_state AS reconcileState,
                           occurred_at AS occurredAt, freshness_at AS freshnessAt
                      FROM CPF_TRANSACTION_LINEAGE
                     WHERE transaction_id = ?
                     ORDER BY occurred_at, segment_id, attempt_no
                    """, List.of(tx), max));
        }

        // Existing canonical stores are queried directly so the one-shot view is useful even before
        // optional cross-database lineage exporters are enabled.
        appendIfTable(rows, "CPF_TRANSACTION_SEGMENT", """
                SELECT transaction_id AS transactionId, transaction_segment_id AS segmentId,
                       parent_segment_id AS parentSegmentId, execution_id AS executionId, COALESCE(attempt_no,1) AS attempt,
                       NULL AS traceId, NULL AS spanId, NULL AS requestId, NULL AS idempotencyKey,
                       NULL AS tenantId, current_channel AS currentChannel, operator_id_masked AS actorIdMasked,
                       selected_instance_id AS instanceId, NULL AS wasId, NULL AS agentId, NULL AS workerId,
                       external_institution_code AS remoteSystem, transaction_name AS operation,
                       NULL AS messageId, NULL AS consumerGroup, NULL AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       NULL AS partitionId, NULL AS fileId,
                       CASE WHEN transaction_role='EXTERNAL' THEN 'REMOTE' ELSE 'LOCAL' END AS sourceType,
                       transaction_segment_id AS sourceRefId, status AS lifecycleState,
                       failure_code AS failureStage, CASE WHEN result_state='UNKNOWN' OR unknown_result_id IS NOT NULL THEN 'Y' ELSE 'N' END AS unknownYn,
                       result_state AS reconcileState, started_at AS occurredAt, updated_at AS freshnessAt
                  FROM CPF_TRANSACTION_SEGMENT WHERE transaction_id = ? ORDER BY started_at, sequence_no
                """, tx, max);
        appendIfTable(rows, "CPF_TRANSACTION_LOG", """
                SELECT transaction_id AS transactionId, span_id AS segmentId,
                       parent_span_id AS parentSegmentId, COALESCE(sequence_no,1) AS attempt,
                       trace_id AS traceId, span_id AS spanId, correlation_id AS requestId,
                       idempotency_key AS idempotencyKey, NULL AS tenantId, current_channel AS currentChannel,
                       NULL AS actorIdMasked, instance_id AS instanceId, was_id AS wasId,
                       NULL AS agentId, NULL AS workerId, target_channel AS remoteSystem,
                       COALESCE(execution_method, business_transaction_name) AS operation,
                       NULL AS messageId, NULL AS consumerGroup, NULL AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       NULL AS partitionId, NULL AS fileId, 'TRACE' AS sourceType,
                       log_idx AS sourceRefId,
                       CASE WHEN error_code IS NULL THEN 'COMPLETED' ELSE 'FAILED' END AS lifecycleState,
                       error_code AS failureStage, 'N' AS unknownYn, NULL AS reconcileState,
                       COALESCE(start_time,created_at) AS occurredAt, updated_at AS freshnessAt
                  FROM CPF_TRANSACTION_LOG WHERE transaction_id = ? ORDER BY start_time, log_idx
                """, tx, max);
        appendIfTable(rows, "CPF_BROKER_OUTBOX", """
                SELECT transaction_id AS transactionId, segment_id AS segmentId, NULL AS parentSegmentId,
                       GREATEST(attempt_count,1) AS attempt, NULL AS traceId, NULL AS spanId, NULL AS requestId,
                       idempotency_key AS idempotencyKey, NULL AS tenantId, NULL AS channel, NULL AS actorIdMasked,
                       NULL AS instanceId, NULL AS wasId, NULL AS agentId, worker_id AS workerId,
                       broker_name AS remoteSystem, topic AS operation, message_id AS messageId,
                       consumer_module AS consumerGroup, NULL AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       partition_key AS partitionId, NULL AS fileId, 'MESSAGE' AS sourceType,
                       outbox_id AS sourceRefId, outbox_status AS lifecycleState,
                       failure_message AS failureStage, 'N' AS unknownYn, NULL AS reconcileState,
                       occurred_at AS occurredAt, updated_at AS freshnessAt
                  FROM CPF_BROKER_OUTBOX WHERE transaction_id = ? ORDER BY occurred_at, outbox_id
                """, tx, max);
        appendIfTable(rows, "CPF_BROKER_DLQ", """
                SELECT transaction_id AS transactionId, segment_id AS segmentId, NULL AS parentSegmentId,
                       GREATEST(replay_count,1) AS attempt, NULL AS traceId, NULL AS spanId, NULL AS requestId,
                       NULL AS idempotencyKey, NULL AS tenantId, NULL AS channel, NULL AS actorIdMasked,
                       NULL AS instanceId, NULL AS wasId, NULL AS agentId, NULL AS workerId,
                       NULL AS remoteSystem, topic AS operation, message_id AS messageId,
                       NULL AS consumerGroup, dlq_id AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       NULL AS partitionId, NULL AS fileId, 'DLQ' AS sourceType,
                       dlq_id AS sourceRefId, replay_status AS lifecycleState,
                       failure_reason AS failureStage, 'N' AS unknownYn, replay_status AS reconcileState,
                       created_at AS occurredAt, updated_at AS freshnessAt
                  FROM CPF_BROKER_DLQ WHERE transaction_id = ? ORDER BY created_at, dlq_id
                """, tx, max);
        appendIfTable(rows, "CPF_FILE_TRANSFER_HISTORY", """
                SELECT transaction_id AS transactionId, segment_id AS segmentId, NULL AS parentSegmentId,
                       1 AS attempt, NULL AS traceId, NULL AS spanId, NULL AS requestId,
                       duplicate_key AS idempotencyKey, NULL AS tenantId, NULL AS channel, NULL AS actorIdMasked,
                       NULL AS instanceId, NULL AS wasId, NULL AS agentId, NULL AS workerId,
                       endpoint_code AS remoteSystem, transfer_operation AS operation,
                       NULL AS messageId, NULL AS consumerGroup, NULL AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       NULL AS partitionId, transfer_id AS fileId, 'FILE' AS sourceType,
                       transfer_id AS sourceRefId, transfer_status AS lifecycleState,
                       result_detail AS failureStage, 'N' AS unknownYn, NULL AS reconcileState,
                       created_at AS occurredAt, updated_at AS freshnessAt
                  FROM CPF_FILE_TRANSFER_HISTORY WHERE transaction_id = ? ORDER BY created_at, history_id
                """, tx, max);
        appendIfTable(rows, "CPF_UNKNOWN_RESULT", """
                SELECT transaction_id AS transactionId, segment_id AS segmentId, NULL AS parentSegmentId,
                       GREATEST(attempt_count,1) AS attempt, NULL AS traceId, NULL AS spanId,
                       external_key AS requestId, NULL AS idempotencyKey, NULL AS tenantId, NULL AS channel,
                       NULL AS actorIdMasked, lease_owner AS instanceId, NULL AS wasId, NULL AS agentId,
                       NULL AS workerId, NULL AS remoteSystem, unknown_type AS operation,
                       NULL AS messageId, NULL AS consumerGroup, NULL AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       NULL AS partitionId, NULL AS fileId, 'UNKNOWN' AS sourceType,
                       unknown_id AS sourceRefId, unknown_status AS lifecycleState,
                       failure_code AS failureStage, 'Y' AS unknownYn, unknown_status AS reconcileState,
                       detected_at AS occurredAt, updated_at AS freshnessAt
                  FROM CPF_UNKNOWN_RESULT WHERE transaction_id = ? ORDER BY detected_at, unknown_seq
                """, tx, max);
        appendIfTable(rows, "OPS_SERVICE_CALL_HISTORY", """
                SELECT transaction_id AS transactionId, call_id AS segmentId,
                       NULL AS parentSegmentId, GREATEST(COALESCE(retry_count,0)+1,1) AS attempt,
                       trace_id AS traceId, NULL AS spanId, NULL AS requestId, NULL AS idempotencyKey,
                       NULL AS tenantId, NULL AS channel, NULL AS actorIdMasked, instance_id AS instanceId,
                       NULL AS wasId, NULL AS agentId, NULL AS workerId, service_id AS remoteSystem,
                       endpoint_code AS operation, NULL AS messageId, NULL AS consumerGroup, NULL AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       NULL AS partitionId, NULL AS fileId, 'REMOTE' AS sourceType,
                       call_id AS sourceRefId, call_status AS lifecycleState,
                       failure_code AS failureStage, CASE WHEN call_status='UNKNOWN' THEN 'Y' ELSE 'N' END AS unknownYn,
                       call_status AS reconcileState, created_at AS occurredAt, updated_at AS freshnessAt
                  FROM OPS_SERVICE_CALL_HISTORY WHERE transaction_id = ? ORDER BY created_at, call_id
                """, tx, max);
        appendIfTable(rows, "SEC_TOKEN_AUDIT_LOG", """
                SELECT transaction_id AS transactionId, TOKEN_AUDIT_ID AS segmentId,
                       NULL AS parentSegmentId, 1 AS attempt, TRACE_ID AS traceId, NULL AS spanId,
                       NULL AS requestId, NULL AS idempotencyKey, NULL AS tenantId, NULL AS channel,
                       NULL AS actorIdMasked, NULL AS instanceId, NULL AS wasId, NULL AS agentId,
                       NULL AS workerId, ISSUER AS remoteSystem, TOKEN_TYPE AS operation,
                       NULL AS messageId, NULL AS consumerGroup, NULL AS dlqId,
                       NULL AS batchJobInstanceId, NULL AS batchJobExecutionId, NULL AS batchStepExecutionId,
                       NULL AS partitionId, NULL AS fileId, 'AUDIT' AS sourceType,
                       TOKEN_AUDIT_ID AS sourceRefId, ACTIVE_YN AS lifecycleState,
                       FAILURE_REASON AS failureStage, 'N' AS unknownYn, NULL AS reconcileState,
                       CREATED_AT AS occurredAt, CREATED_AT AS freshnessAt
                  FROM SEC_TOKEN_AUDIT_LOG WHERE transaction_id = ? ORDER BY CREATED_AT, TOKEN_AUDIT_ID
                """, tx, max);

        LinkedHashMap<String, Map<String,Object>> unique = new LinkedHashMap<>();
        for (Map<String,Object> raw : rows) {
            Map<String,Object> safe = maskLineageRow(raw);
            String key = stringValue(value(safe,"sourceType"))+"|"+stringValue(value(safe,"sourceRefId"))+"|"+stringValue(value(safe,"segmentId"));
            unique.putIfAbsent(key, safe);
        }
        return unique.values().stream()
                .sorted(java.util.Comparator.comparing(row -> Objects.toString(value(row,"occurredAt"), "")))
                .limit(max).toList();
    }

    @Override
    public Map<String, Object> sourceFreshness(String transactionId) {
        String tx = transactionId == null ? "" : transactionId.trim();
        if (tx.isEmpty() || jdbcTemplate == null) {
            return classifySourceFreshness(tx, List.of(), java.util.Set.of("LOCAL"));
        }
        try {
            return classifySourceFreshness(tx, findLineage(tx, 500), java.util.Set.of());
        } catch (RuntimeException ex) {
            // A source-query failure is operationally different from a source that never applied.
            // Fail closed for the always-applicable LOCAL source while leaving unrelated sources N/A.
            return classifySourceFreshness(tx, List.of(), java.util.Set.of("LOCAL"));
        }
    }

    static Map<String, Object> classifySourceFreshness(
            String transactionId,
            List<Map<String,Object>> lineage,
            java.util.Set<String> failedSources) {
        String tx = transactionId == null ? "" : transactionId.trim();
        List<Map<String,Object>> safeLineage = lineage == null ? List.of() : lineage;
        LinkedHashSet<String> failures = new LinkedHashSet<>(
                failedSources == null ? java.util.Set.of() : failedSources);
        LinkedHashMap<String, Map<String,Object>> observed = new LinkedHashMap<>();
        for (Map<String,Object> row : safeLineage) {
            String type = Objects.toString(value(row,"sourceType"), "UNKNOWN").trim().toUpperCase(java.util.Locale.ROOT);
            String queryState = Objects.toString(value(row,"queryState"), "").trim().toUpperCase(java.util.Locale.ROOT);
            if ("QUERY_FAILED".equals(queryState) || "TABLE_UNAVAILABLE".equals(queryState)) {
                failures.add(type);
                continue;
            }
            Map<String,Object> current = observed.computeIfAbsent(type, ignored -> new LinkedHashMap<>());
            current.put("sourceType", type);
            current.put("eventCount", ((Number)current.getOrDefault("eventCount",0)).intValue()+1);
            Object freshness=value(row,"freshnessAt");
            Object previous=current.get("freshnessAt");
            if (freshness != null && (previous == null || String.valueOf(freshness).compareTo(String.valueOf(previous))>0)) {
                current.put("freshnessAt",freshness);
            }
        }

        List<String> canonical = List.of("LOCAL", "REMOTE", "MESSAGE", "DLQ", "BATCH", "FILE", "TRACE", "AUDIT", "UNKNOWN");
        LinkedHashSet<String> orderedTypes = new LinkedHashSet<>(canonical);
        orderedTypes.addAll(observed.keySet());
        orderedTypes.addAll(failures);

        List<Map<String,Object>> states = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<String> staleSources = new ArrayList<>();
        List<String> notApplicable = new ArrayList<>();

        for (String type : orderedTypes) {
            Map<String,Object> current = observed.get(type);
            boolean applicable = "LOCAL".equals(type) || current != null || failures.contains(type);
            LinkedHashMap<String,Object> state = new LinkedHashMap<>();
            state.put("sourceType", type);
            state.put("applicability", applicable ? "APPLICABLE" : "NOT_APPLICABLE");
            if (!applicable) {
                state.put("availability", "NOT_APPLICABLE");
                state.put("eventCount", 0);
                notApplicable.add(type);
            } else if (failures.contains(type)) {
                state.put("availability", "FAILED");
                state.put("eventCount", current == null ? 0 : current.getOrDefault("eventCount",0));
                if (current != null && current.get("freshnessAt") != null) state.put("freshnessAt", current.get("freshnessAt"));
                failed.add(type);
            } else if (current == null) {
                state.put("availability", "MISSING");
                state.put("eventCount", 0);
                missing.add(type);
            } else {
                boolean sourceStale = staleTimestamp(current.get("freshnessAt"));
                state.putAll(current);
                state.put("applicability", "APPLICABLE");
                state.put("availability", sourceStale ? "STALE" : "AVAILABLE");
                if (sourceStale) staleSources.add(type);
            }
            states.add(Map.copyOf(state));
        }
        return Map.of(
                "transactionId", tx,
                "partial", !missing.isEmpty() || !failed.isEmpty(),
                "stale", !staleSources.isEmpty(),
                "missingSources", List.copyOf(missing),
                "failedSources", List.copyOf(failed),
                "staleSources", List.copyOf(staleSources),
                "notApplicableSources", List.copyOf(notApplicable),
                "sources", List.copyOf(states));
    }

    private void appendIfTable(List<Map<String,Object>> target, String table, String sql, String transactionId, int limit) {
        String sourceType = sourceTypeForTable(table);
        if (!namedTableAvailable(table)) return;
        try {
            target.addAll(queryForListLimited(sql, List.of(transactionId), limit));
        } catch (RuntimeException failure) {
            // 조회 실패는 N/A가 아니다. 운영자가 부분 실패를 식별하도록 명시적인 source marker를 남긴다.
            target.add(Map.of(
                    "sourceType", sourceType,
                    "sourceRefId", "QUERY:" + table,
                    "queryState", "QUERY_FAILED",
                    "failureStage", failure.getClass().getSimpleName(),
                    "freshnessAt", java.time.Instant.now()));
        }
    }

    private static String sourceTypeForTable(String table) {
        return switch (table) {
            case "CPF_TRANSACTION_LOG" -> "TRACE";
            case "CPF_BROKER_OUTBOX" -> "MESSAGE";
            case "CPF_BROKER_DLQ" -> "DLQ";
            case "CPF_FILE_TRANSFER_HISTORY" -> "FILE";
            case "CPF_UNKNOWN_RESULT" -> "UNKNOWN";
            case "OPS_SERVICE_CALL_HISTORY" -> "REMOTE";
            case "SEC_TOKEN_AUDIT_LOG" -> "AUDIT";
            default -> "LOCAL";
        };
    }

    private static boolean staleTimestamp(Object value) {
        if (value == null) return true;
        try {
            java.time.Instant instant;
            if (value instanceof java.sql.Timestamp ts) instant=ts.toInstant();
            else if (value instanceof java.time.Instant i) instant=i;
            else if (value instanceof java.time.LocalDateTime ldt) instant=ldt.atZone(java.time.ZoneId.systemDefault()).toInstant();
            else instant=java.time.Instant.parse(String.valueOf(value));
            return instant.isBefore(java.time.Instant.now().minusSeconds(300));
        } catch (RuntimeException ignored) { return true; }
    }

    private boolean namedTableAvailable(String table) {
        if (jdbcTemplate == null || table == null || !table.matches("[A-Za-z0-9_]+")) return false;
        DataSource dataSource=jdbcTemplate.getDataSource(); if(dataSource==null)return false;
        try(Connection connection=dataSource.getConnection()){
            String catalog=connection.getCatalog(), schema=currentSchema(connection);
            for(String candidate:List.of(table,table.toUpperCase(java.util.Locale.ROOT))){
                try(ResultSet rs=connection.getMetaData().getTables(catalog,schema,candidate,new String[]{"TABLE"})){if(rs.next())return true;}
            }
            return false;
        } catch(SQLException ex){return false;}
    }

    @Override
    public List<Map<String, Object>> findExternalCandidates(String transactionId, int limit) {
        if (!hasText(transactionId) || !tableAvailable()) {
            return List.of();
        }
        return queryForListLimited("""
                SELECT transaction_segment_id AS transactionSegmentId,
                       module_code AS moduleCode,
                       external_institution_code AS externalInstitutionCode,
                       external_transaction_id AS externalTransactionId,
                       api_path AS apiPath,
                       status,
                       failure_yn AS failureYn,
                       failure_code AS failureCode,
                       failure_message_masked AS failureMessageMasked,
                       selected_instance_id AS selectedInstanceId,
                       attempt_no AS attemptNo,
                       retry_yn AS retryYn,
                       failover_yn AS failoverYn,
                       circuit_state AS circuitState,
                       downstream_http_status AS downstreamHttpStatus,
                       result_state AS resultState,
                       unknown_result_id AS unknownResultId,
                       started_at AS startedAt,
                       ended_at AS endedAt,
                       duration_ms AS durationMs
                  FROM CPF_TRANSACTION_SEGMENT
                 WHERE transaction_id = ?
                   AND (transaction_role = 'EXTERNAL' OR external_institution_code IS NOT NULL)
                 ORDER BY started_at, sequence_no
                """, List.of(transactionId.trim()), boundedLimit(limit)).stream()
                .map(this::maskExternalRow)
                .toList();
    }

    private QueryParts buildGroupQuery(Map<String, String> criteria, String sort) {
        StringBuilder sql = new StringBuilder("""
                WITH filtered_segments AS (
                    SELECT CPF_TRANSACTION_SEGMENT.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY transaction_id
                               ORDER BY started_at, sequence_no, segment_id
                           ) AS cpf_row_no
                      FROM CPF_TRANSACTION_SEGMENT
                     WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendCriteria(sql, args, criteria);
        sql.append("""
                )
                SELECT transaction_id AS transactionId,
                       MIN(started_at) AS startedAt,
                       MAX(ended_at) AS endedAt,
                       SUM(COALESCE(duration_ms, 0)) AS totalDurationMs,
                       COUNT(*) AS segmentCount,
                       SUM(CASE WHEN transaction_role = 'EXTERNAL' THEN 1 ELSE 0 END) AS externalCallCount,
                       MAX(CASE WHEN cpf_row_no = 1 THEN module_code END) AS originModuleCode,
                       MAX(CASE WHEN failure_yn = 'Y' THEN module_code ELSE NULL END) AS failedModuleCode,
                       MAX(CASE WHEN failure_yn = 'Y' THEN transaction_segment_id ELSE NULL END) AS failedSegmentId,
                       MAX(CASE WHEN failure_yn = 'Y' THEN transaction_name ELSE NULL END) AS failedSegmentName,
                       MAX(CASE WHEN failure_yn = 'Y' THEN failure_code ELSE NULL END) AS failureCode,
                       MAX(CASE WHEN failure_yn = 'Y' THEN failure_message_masked ELSE NULL END) AS failureMessageMasked,
                       CASE WHEN SUM(CASE WHEN failure_yn = 'Y' THEN 1 ELSE 0 END) > 0 THEN 'FAILED' ELSE 'SUCCESS' END AS overallStatus,
                       CASE WHEN SUM(CASE WHEN failure_yn = 'Y' THEN 1 ELSE 0 END) > 0 THEN 'Y' ELSE 'N' END AS failureYn,
                       MAX(customer_no_masked) AS customerNoMasked,
                       MAX(member_no_masked) AS memberNoMasked,
                       MAX(user_id_masked) AS userIdMasked,
                       MAX(operator_id_masked) AS operatorIdMasked,
                       MAX(client_id) AS clientId,
                       MAX(original_system_code) AS originalSystemCode,
                       MAX(system_code) AS systemCode,
                       MAX(caller_system_code) AS callerSystemCode,
                       MAX(target_system_code) AS targetSystemCode,
                       MAX(caller_channel) AS callerChannel,
                       MAX(current_channel) AS currentChannel,
                       MAX(original_channel) AS originalChannel,
                       MAX(target_channel) AS targetChannel,
                       MAX(target_operation_id) AS targetOperationId,
                       MAX(external_institution_code) AS externalInstitutionCode,
                       MAX(external_transaction_id) AS externalTransactionId,
                       MAX(transaction_name) AS transactionName,
                       MAX(api_path) AS apiPath
                  FROM filtered_segments
                 GROUP BY transaction_id
                """);
        if (hasText(criteria.get("originModuleCode"))) {
            sql.append(" HAVING MAX(CASE WHEN cpf_row_no = 1 THEN module_code END) = ?");
            args.add(criteria.get("originModuleCode").trim().toUpperCase(java.util.Locale.ROOT));
        }
        sql.append(orderBy(sort));

        StringBuilder detailSql = new StringBuilder("""
                SELECT transaction_id AS transactionId,
                       module_code AS moduleCode,
                       transaction_role AS transactionRole
                  FROM CPF_TRANSACTION_SEGMENT
                 WHERE 1 = 1
                """);
        List<Object> detailArgs = new ArrayList<>();
        appendCriteria(detailSql, detailArgs, criteria);
        return new QueryParts(sql.toString(), args, detailSql.toString(), detailArgs);
    }

    private void appendCriteria(StringBuilder sql, List<Object> args, Map<String, String> criteria) {
        appendLike(sql, args, "transaction_id", criteria.get("transactionId"));
        appendLike(sql, args, "transaction_segment_id", first(criteria, "transactionSegmentId", "segmentId", "failedSegmentId"));
        appendLike(sql, args, "module_code", first(criteria, "includedModuleCode", "moduleCode"));
        appendEquals(sql, args, "source_module_code", criteria.get("sourceModuleCode"));
        appendEquals(sql, args, "target_module_code", criteria.get("targetModuleCode"));
        appendEquals(sql, args, "transaction_role", criteria.get("transactionRole"));
        appendEquals(sql, args, "direction", criteria.get("direction"));
        appendEquals(sql, args, "status", criteria.get("status"));
        appendEquals(sql, args, "failure_yn", criteria.get("failureYn"));
        appendEquals(sql, args, "module_code", criteria.get("failedModuleCode"));
        appendLike(sql, args, "failure_code", criteria.get("failureCode"));
        appendLike(sql, args, "customer_no_masked", criteria.get("customerNo"));
        appendLike(sql, args, "member_no_masked", criteria.get("memberNo"));
        appendLike(sql, args, "user_id_masked", criteria.get("userId"));
        appendLike(sql, args, "operator_id_masked", criteria.get("operatorId"));
        appendLike(sql, args, "client_id", criteria.get("clientId"));
        appendEquals(sql, args, "original_system_code", criteria.get("originalSystemCode"));
        appendEquals(sql, args, "system_code", criteria.get("systemCode"));
        appendEquals(sql, args, "caller_system_code", criteria.get("callerSystemCode"));
        appendEquals(sql, args, "target_system_code", criteria.get("targetSystemCode"));
        appendEquals(sql, args, "caller_channel", criteria.get("callerChannel"));
        appendEquals(sql, args, "current_channel", criteria.get("currentChannel"));
        appendEquals(sql, args, "original_channel", criteria.get("originalChannel"));
        appendEquals(sql, args, "target_channel", criteria.get("targetChannel"));
        appendLike(sql, args, "target_operation_id", criteria.get("targetOperationId"));
        appendEquals(sql, args, "external_institution_code", criteria.get("externalInstitutionCode"));
        appendLike(sql, args, "external_transaction_id", criteria.get("externalTransactionId"));
        appendLike(sql, args, "api_path", criteria.get("apiPath"));
        appendLike(sql, args, "transaction_name", criteria.get("transactionName"));
        appendLike(sql, args, "request_header_snapshot_masked", criteria.get("standardHeaderValue"));
        appendLike(sql, args, "response_header_snapshot_masked", criteria.get("responseHeaderValue"));
        appendLike(sql, args, "extension_header_snapshot_masked", first(criteria, "extensionHeaderValue", "extHeaderValue"));
        appendDateTime(sql, args, "started_at", ">=", criteria.get("startedAtFrom"));
        appendDateTime(sql, args, "started_at", "<=", criteria.get("startedAtTo"));
        appendLong(sql, args, "duration_ms", ">=", criteria.get("durationMsFrom"));
        appendLong(sql, args, "duration_ms", "<=", criteria.get("durationMsTo"));
    }

    private List<Map<String, Object>> enrichGroupRows(List<Map<String, Object>> groups, QueryParts query) {
        if (groups.isEmpty()) {
            return groups;
        }
        List<String> transactionIds = groups.stream()
                .map(row -> stringValue(value(row, "transactionId")))
                .filter(this::hasText)
                .toList();
        if (transactionIds.isEmpty()) {
            return groups;
        }
        String placeholders = String.join(",", transactionIds.stream().map(ignored -> "?").toList());
        List<Object> detailArgs = new ArrayList<>(query.detailArgs());
        detailArgs.addAll(transactionIds);
        String detailSql = query.detailSql()
                + " AND transaction_id IN (" + placeholders + ")"
                + " ORDER BY transaction_id, started_at, sequence_no, segment_id";
        List<Map<String, Object>> details = jdbcTemplate.queryForList(detailSql, detailArgs.toArray());
        Map<String, LinkedHashSet<String>> modules = new LinkedHashMap<>();
        Map<String, LinkedHashSet<String>> roles = new LinkedHashMap<>();
        for (Map<String, Object> detail : details) {
            String transactionId = stringValue(value(detail, "transactionId"));
            if (!hasText(transactionId)) {
                continue;
            }
            addIfPresent(modules.computeIfAbsent(transactionId, ignored -> new LinkedHashSet<>()),
                    stringValue(value(detail, "moduleCode")));
            addIfPresent(roles.computeIfAbsent(transactionId, ignored -> new LinkedHashSet<>()),
                    stringValue(value(detail, "transactionRole")));
        }
        List<Map<String, Object>> enriched = new ArrayList<>(groups.size());
        for (Map<String, Object> group : groups) {
            Map<String, Object> row = new LinkedHashMap<>(group);
            String transactionId = stringValue(value(group, "transactionId"));
            row.put("moduleFlowText", joinOrNull(modules.get(transactionId), " -> "));
            row.put("rolesText", joinOrNull(roles.get(transactionId), " / "));
            enriched.add(row);
        }
        return enriched;
    }

    private void addIfPresent(LinkedHashSet<String> values, String value) {
        if (hasText(value)) {
            values.add(value);
        }
    }

    private String joinOrNull(LinkedHashSet<String> values, String delimiter) {
        return values == null || values.isEmpty() ? null : String.join(delimiter, values);
    }

    private Map<String, Object> maskGroupRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        mask(result, "externalTransactionId", 500);
        return result;
    }

    private Map<String, Object> maskSegmentRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        mask(result, "requestHeaderSnapshotMasked", 4000);
        mask(result, "responseHeaderSnapshotMasked", 4000);
        mask(result, "extensionHeaderSnapshotMasked", 4000);
        mask(result, "externalTransactionId", 500);
        return result;
    }

    private Map<String, Object> maskExternalRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.put("source", "CPF_SEGMENT_FALLBACK");
        mask(result, "externalTransactionId", 500);
        mask(result, "failureMessageMasked", 1000);
        return result;
    }

    private void mask(Map<String, Object> row, String key, int limit) {
        row.computeIfPresent(key, (ignored, value) -> CpfMaskingRuntime.mask(String.valueOf(value), limit));
    }

    private Map<String, Object> maskLineageRow(Map<String, Object> row) {
        Map<String, Object> safe = new LinkedHashMap<>(row);
        Object actor = value(safe, "actorIdMasked");
        if (actor != null) safe.put("actorIdMasked", CpfMaskingRuntime.mask(String.valueOf(actor), 128));
        return safe;
    }

    private boolean lineageTableAvailable() {
        try {
            if (jdbcTemplate == null) return false;
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CPF_TRANSACTION_LINEAGE WHERE 1 = 0", Long.class);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean tableAvailable() {
        if (jdbcTemplate == null) {
            return false;
        }
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return false;
        }
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            String schema = currentSchema(connection);
            for (String candidate : List.of("CPF_TRANSACTION_SEGMENT")) {
                try (ResultSet tables = connection.getMetaData()
                        .getTables(catalog, schema, candidate, new String[]{"TABLE"})) {
                    if (tables.next()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException ex) {
            return false;
        }
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            args.add('%' + value.trim() + '%');
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim().toUpperCase(java.util.Locale.ROOT));
        }
    }

    private void appendLong(StringBuilder sql, List<Object> args, String column, String operator, String value) {
        if (!hasText(value)) {
            return;
        }
        try {
            sql.append(" AND ").append(column).append(' ').append(operator).append(" ?");
            args.add(Long.parseLong(value.trim()));
        } catch (NumberFormatException ignored) {
            // 숫자 형식이 아닌 검색 조건은 안전하게 제외합니다.
        }
    }

    private void appendDateTime(StringBuilder sql, List<Object> args, String column, String operator, String value) {
        if (!hasText(value)) {
            return;
        }
        try {
            sql.append(" AND ").append(column).append(' ').append(operator).append(" ?");
            args.add(LocalDateTime.parse(value.trim()));
        } catch (DateTimeParseException ignored) {
            // ISO-8601 형식이 아닌 검색 조건은 안전하게 제외합니다.
        }
    }

    private String orderBy(String sort) {
        return switch (sort) {
            case "durationDesc" -> " ORDER BY totalDurationMs DESC, startedAt DESC";
            case "statusAsc" -> " ORDER BY overallStatus ASC, startedAt DESC";
            case "failedFirst" -> " ORDER BY failureYn DESC, startedAt DESC";
            case "moduleAsc" -> " ORDER BY originModuleCode ASC, startedAt DESC";
            default -> " ORDER BY startedAt DESC";
        };
    }

    private List<Map<String, Object>> queryForListLimited(String sql, List<?> args, int limit) {
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int index = 0; index < args.size(); index++) {
                statement.setObject(index + 1, args.get(index));
            }
            statement.setMaxRows(boundedLimit(limit));
            return statement;
        }, new ColumnMapRowMapper());
    }

    private String currentSchema(Connection connection) {
        try {
            return connection.getSchema();
        } catch (SQLException | AbstractMethodError ignored) {
            return null;
        }
    }

    private static Object value(Map<String, Object> row, String key) {
        Object exact = row.get(key);
        if (exact != null || row.containsKey(key)) {
            return exact;
        }
        return row.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int limit(String value) {
        try {
            return hasText(value) ? boundedLimit(Integer.parseInt(value.trim())) : 100;
        } catch (NumberFormatException ex) {
            return 100;
        }
    }

    private int boundedLimit(int value) {
        return Math.max(1, Math.min(500, value));
    }

    private String sort(String value) {
        if (!hasText(value)) {
            return "startedAtDesc";
        }
        return switch (value.trim()) {
            case "durationDesc", "statusAsc", "failedFirst", "moduleAsc" -> value.trim();
            default -> "startedAtDesc";
        };
    }

    private String first(Map<String, String> values, String... keys) {
        for (String key : keys) {
            if (hasText(values.get(key))) {
                return values.get(key);
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record QueryParts(
            String sql,
            List<Object> args,
            String detailSql,
            List<Object> detailArgs) {
    }
}
