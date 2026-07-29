package com.cpf.admin.opr.service;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.util.CpfStrings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 운영 관제 화면에서 거래, 오류, 일반 감사, 정책 감사를 한 흐름으로 조회하는 서비스입니다.
 *
 * <p>CPF 거래 로그와 로그 정책 감사는 cpfDB, ADM 운영 행위 감사는 admDB에 있으므로
 * 이 서비스가 운영자용 조회 모델로 묶어서 반환합니다. 실제 원천 테이블은 분리해 두어
 * 감사 책임과 보존 정책을 명확히 유지합니다.</p>
 */
@Service
public class AdmObservabilityService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate cpfJdbcTemplate;
    private final CpfBatchOperationsPort batchOperations;
    private final JdbcTemplate admJdbcTemplate;

    public AdmObservabilityService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            CpfBatchOperationsPort batchOperations,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.batchOperations = batchOperations;
        this.admJdbcTemplate = admJdbcTemplate;
    }

    public Map<String, Object> traceByTransactionId(String transactionId, int limit) {
        return trace("TRANSACTION_ID", transactionId, transactionId, null, null, limit);
    }

    public Map<String, Object> traceByTraceId(String traceId, int limit) {
        return trace("TRACE_ID", traceId, null, traceId, null, limit);
    }

    public Map<String, Object> traceByBusinessTransactionId(String businessTransactionId, int limit) {
        return trace("BUSINESS_TRANSACTION_ID", businessTransactionId, null, null, businessTransactionId, limit);
    }

    public Map<String, Object> findPolicyAudits(
            String operatorId,
            String actionType,
            String targetType,
            String targetId,
            Long policyId,
            Long overrideId,
            int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean available = tableAvailable(cpfJdbcTemplate, "cpf_log_policy_audit");
        response.put("available", available);
        response.put("items", available
                ? queryPolicyAudits(operatorId, actionType, targetType, targetId, policyId, overrideId, limit)
                : List.of());
        response.put("source", "cpf_log_policy_audit");
        return response;
    }

    private Map<String, Object> trace(
            String queryType,
            String queryValue,
            String transactionId,
            String traceId,
            String businessTransactionId,
            int limit) {
        int cappedLimit = cappedLimit(limit);
        List<Map<String, Object>> transactionLogs = queryTransactionLogs(
                transactionId, traceId, businessTransactionId, null, cappedLimit);
        Map<String, Object> summary = transactionLogs.isEmpty() ? Map.of() : transactionLogs.getFirst();
        String resolvedTransactionId = firstText(transactionId, stringValue(summary, "TRANSACTION_ID", "transactionId"));
        String resolvedTraceId = firstText(traceId, stringValue(summary, "TRACE_ID", "traceId"));
        String resolvedBusinessTransactionId = firstText(
                businessTransactionId,
                stringValue(summary, "BUSINESS_TRANSACTION_ID", "businessTransactionId"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", true);
        response.put("queryType", queryType);
        response.put("queryValue", queryValue);
        response.put("transactionSummary", summary);
        response.put("resolvedKeys", resolvedKeys(
                resolvedTransactionId,
                resolvedTraceId,
                resolvedBusinessTransactionId));
        response.put("transactionLogs", transactionLogs);
        response.put("failureLogs", queryTransactionLogs(
                resolvedTransactionId,
                resolvedTraceId,
                resolvedBusinessTransactionId,
                "FAILURE",
                cappedLimit));
        response.put("auditLogs", queryAdmAuditLogs(
                resolvedTransactionId,
                resolvedTraceId,
                resolvedBusinessTransactionId,
                cappedLimit));
        response.put("policyAuditLogs", queryPolicyAudits(
                null,
                null,
                "ONLINE_TRANSACTION",
                resolvedBusinessTransactionId,
                null,
                null,
                cappedLimit));
        response.put("relatedBatchExecutions", queryBatchExecutions(resolvedTransactionId, cappedLimit));
        response.put("counts", counts(response));
        return response;
    }

    private List<Map<String, Object>> queryTransactionLogs(
            String transactionId,
            String traceId,
            String businessTransactionId,
            String logType,
            int limit) {
        if (!tableAvailable(cpfJdbcTemplate, "cpf_transaction_log")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT LOG_IDX, TRANSACTION_ID, TRACE_ID, SPAN_ID, MODULE_ID, MENU_ID,
                       BUSINESS_TRANSACTION_ID, BUSINESS_TRANSACTION_NAME, LOG_TYPE,
                       HTTP_METHOD, URI, HTTP_STATUS, RESPONSE_CODE, ERROR_CODE,
                       EXEC_USER, CHANNEL_CODE, START_TIME, END_TIME, DURATION_MS
                FROM cpf_transaction_log
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "TRANSACTION_ID", transactionId);
        appendLike(sql, args, "TRACE_ID", traceId);
        appendLike(sql, args, "BUSINESS_TRANSACTION_ID", businessTransactionId);
        appendEquals(sql, args, "LOG_TYPE", logType);
        sql.append(" ORDER BY LOG_IDX DESC");
        return AdmJdbcQueries.queryForList(
                cpfJdbcTemplate,
                sql.toString(),
                args,
                cappedLimit(limit));
    }

    private List<Map<String, Object>> queryAdmAuditLogs(
            String transactionId,
            String traceId,
            String businessTransactionId,
            int limit) {
        if (!tableAvailable(admJdbcTemplate, "adm_audit_log")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT AUDIT_ID, TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, BUTTON_ID,
                       ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, BEFORE_DATA, AFTER_DATA,
                       DIFF_DATA, CLIENT_IP, RETENTION_UNTIL, IMMUTABLE_YN, CREATED_AT
                FROM adm_audit_log
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "TRANSACTION_ID", transactionId);
        appendLike(sql, args, "TRACE_ID", traceId);
        if (CpfStrings.hasText(businessTransactionId)) {
            sql.append(" AND (TARGET_ID = ? OR TARGET_ID LIKE ?)");
            args.add(businessTransactionId.trim());
            args.add("%" + businessTransactionId.trim() + "%");
        }
        sql.append(" ORDER BY AUDIT_ID DESC");
        return AdmJdbcQueries.queryForList(
                admJdbcTemplate,
                sql.toString(),
                args,
                cappedLimit(limit));
    }

    private List<Map<String, Object>> queryPolicyAudits(
            String operatorId,
            String actionType,
            String targetType,
            String targetId,
            Long policyId,
            Long overrideId,
            int limit) {
        if (!tableAvailable(cpfJdbcTemplate, "cpf_log_policy_audit")) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT audit_id, policy_id, override_id, action_type, target_type, target_id,
                       reason, before_data, after_data, diff_data, operator_id, client_ip,
                       created_at, updated_at
                FROM cpf_log_policy_audit
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendEquals(sql, args, "operator_id", operatorId);
        appendEquals(sql, args, "action_type", actionType);
        appendEquals(sql, args, "target_type", targetType);
        appendEquals(sql, args, "target_id", targetId);
        if (policyId != null) {
            sql.append(" AND policy_id = ?");
            args.add(policyId);
        }
        if (overrideId != null) {
            sql.append(" AND override_id = ?");
            args.add(overrideId);
        }
        sql.append(" ORDER BY audit_id DESC");
        return AdmJdbcQueries.queryForList(
                cpfJdbcTemplate,
                sql.toString(),
                args,
                cappedLimit(limit));
    }

    private List<Map<String, Object>> queryBatchExecutions(String transactionId, int limit) {
        if (!CpfStrings.hasText(transactionId)) {
            return List.of();
        }
        try {
            return batchOperations.findExecutions(
                    null, transactionId.trim(), null, null, null, cappedLimit(limit));
        } catch (RuntimeException ex) {
            // 관제 보조 조회 실패가 원 거래/로그 조회 전체를 오염시키지 않도록 격리합니다.
            return List.of();
        }
    }

    private Map<String, Object> resolvedKeys(
            String transactionId,
            String traceId,
            String businessTransactionId) {
        Map<String, Object> keys = new LinkedHashMap<>();
        keys.put("transactionId", transactionId);
        keys.put("traceId", traceId);
        keys.put("businessTransactionId", businessTransactionId);
        return keys;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> counts(Map<String, Object> response) {
        Map<String, Object> counts = new LinkedHashMap<>();
        for (String key : List.of("transactionLogs", "failureLogs", "auditLogs", "policyAuditLogs", "relatedBatchExecutions")) {
            Object value = response.get(key);
            counts.put(key, value instanceof List<?> list ? list.size() : 0);
        }
        return counts;
    }

    private boolean tableAvailable(JdbcTemplate jdbcTemplate, String tableName) {
        return AdmJdbcQueries.tableExists(jdbcTemplate, tableName);
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            args.add("%" + value.trim() + "%");
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private int cappedLimit(int limit) {
        return Math.max(1, Math.min(limit, 500));
    }

    private String stringValue(Map<String, Object> row, String... names) {
        for (String name : names) {
            Object value = row.get(name);
            if (value == null) {
                value = row.get(name.toLowerCase());
            }
            if (value != null && CpfStrings.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (CpfStrings.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
