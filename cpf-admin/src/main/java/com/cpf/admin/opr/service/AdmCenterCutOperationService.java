package com.cpf.admin.opr.service;

import com.cpf.common.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ADM Center-Cut 운영 관제 조회 기능을 제공합니다.
 *
 * <p>ADM은 Center-Cut 업무 처리를 직접 실행하지 않고, CPF/BAT 메타와 업무 DB adapter 상태를
 * 읽기 전용으로 조회합니다. 업무 payload 원문은 운영 화면에 노출하지 않고 길이와 마스킹 문구만
 * 반환해 민감정보 노출 가능성을 낮춥니다.</p>
 */
@Service
public class AdmCenterCutOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Logger log = LoggerFactory.getLogger(AdmCenterCutOperationService.class);
    private static final String REF_CENTER_CUT_SAMPLE_JOB_ID = "CPF_REF_CENTER_CUT_SAMPLE_JOB";
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    private final JdbcTemplate batJdbcTemplate;
    private final JdbcTemplate refJdbcTemplate;

    public AdmCenterCutOperationService(
            @Qualifier("batJdbcTemplate") JdbcTemplate batJdbcTemplate,
            @Qualifier("refJdbcTemplate") JdbcTemplate refJdbcTemplate) {
        this.batJdbcTemplate = batJdbcTemplate;
        this.refJdbcTemplate = refJdbcTemplate;
    }

    public List<Map<String, Object>> findJobs() {
        return queryOrEmpty(batJdbcTemplate, """
                SELECT c.center_cut_job_id AS centerCutJobId,
                       c.batch_job_id AS batchJobId,
                       c.center_cut_job_name AS centerCutJobName,
                       c.provider_key AS providerKey,
                       c.handler_key AS handlerKey,
                       c.chunk_size AS chunkSize,
                       c.retry_limit AS retryLimit,
                       c.use_yn AS useYn,
                       c.description AS description,
                       c.created_at AS createdAt,
                       c.updated_at AS updatedAt,
                       j.job_name AS batchJobName,
                       j.job_type AS batchJobType
                FROM bat_center_cut_job c
                LEFT JOIN bat_job j ON j.job_id = c.batch_job_id
                ORDER BY c.center_cut_job_id
                """);
    }

    public Map<String, Object> findJobDetail(String centerCutJobId) {
        String resolvedJobId = TextUtils.requireText(centerCutJobId, "centerCutJobId");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("job", findJob(resolvedJobId));
        result.put("parameters", findParameters(resolvedJobId));
        result.put("summary", findSummary(resolvedJobId));
        result.put("targets", findTargets(resolvedJobId, null, DEFAULT_LIMIT));
        result.put("results", findResults(resolvedJobId, null, DEFAULT_LIMIT));
        return result;
    }

    public List<Map<String, Object>> findParameters(String centerCutJobId) {
        String resolvedJobId = TextUtils.requireText(centerCutJobId, "centerCutJobId");
        return queryOrEmpty(batJdbcTemplate, """
                SELECT parameter_id AS parameterId,
                       center_cut_job_id AS centerCutJobId,
                       parameter_key AS parameterKey,
                       CASE
                           WHEN encrypted_yn = 'Y' THEN '[MASKED]'
                           ELSE parameter_value
                       END AS parameterValue,
                       encrypted_yn AS encryptedYn,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM bat_center_cut_parameter
                WHERE center_cut_job_id = ?
                ORDER BY parameter_key
                """, resolvedJobId);
    }

    public Map<String, Object> findSummary(String centerCutJobId) {
        String resolvedJobId = TextUtils.requireText(centerCutJobId, "centerCutJobId");
        String adapterType = resolveAdapterType(resolvedJobId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("centerCutJobId", resolvedJobId);
        result.put("adapterType", adapterType);

        if ("REF_SAMPLE".equals(adapterType)) {
            result.putAll(queryForMapOrEmpty(refJdbcTemplate, """
                    SELECT COUNT(*) AS totalCount,
                           SUM(CASE WHEN status_code = 'READY' THEN 1 ELSE 0 END) AS readyCount,
                           SUM(CASE WHEN status_code = 'RUNNING' THEN 1 ELSE 0 END) AS runningCount,
                           SUM(CASE WHEN status_code = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
                           SUM(CASE WHEN status_code = 'FAILED' THEN 1 ELSE 0 END) AS failedCount,
                           SUM(CASE WHEN status_code = 'SKIPPED' THEN 1 ELSE 0 END) AS skippedCount,
                           SUM(CASE WHEN status_code = 'RETRY_REQUESTED' THEN 1 ELSE 0 END) AS retryRequestedCount,
                           SUM(CASE WHEN status_code = 'STOP_REQUESTED' THEN 1 ELSE 0 END) AS stopRequestedCount,
                           MAX(started_at) AS lastStartedAt,
                           MAX(completed_at) AS lastCompletedAt
                    FROM ref_center_cut_sample_target
                    WHERE center_cut_job_id = ?
                    """, resolvedJobId));
            result.putAll(prefix("result", queryForMapOrEmpty(refJdbcTemplate, """
                    SELECT COUNT(*) AS totalCount,
                           SUM(CASE WHEN result_status = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
                           SUM(CASE WHEN result_status = 'FAILED' THEN 1 ELSE 0 END) AS failedCount,
                           MAX(created_at) AS lastCreatedAt
                    FROM ref_center_cut_sample_result
                    WHERE center_cut_job_id = ?
                    """, resolvedJobId)));
            return result;
        }

        result.putAll(queryForMapOrEmpty(batJdbcTemplate, """
                SELECT COUNT(*) AS totalCount,
                       SUM(CASE WHEN item_status = 'READY' THEN 1 ELSE 0 END) AS readyCount,
                       SUM(CASE WHEN item_status = 'RUNNING' THEN 1 ELSE 0 END) AS runningCount,
                       SUM(CASE WHEN item_status = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
                       SUM(CASE WHEN item_status = 'FAILED' THEN 1 ELSE 0 END) AS failedCount,
                       SUM(CASE WHEN item_status = 'SKIPPED' THEN 1 ELSE 0 END) AS skippedCount,
                       SUM(CASE WHEN item_status = 'RETRY_REQUESTED' THEN 1 ELSE 0 END) AS retryRequestedCount,
                       SUM(CASE WHEN item_status = 'STOP_REQUESTED' THEN 1 ELSE 0 END) AS stopRequestedCount,
                       MAX(started_at) AS lastStartedAt,
                       MAX(completed_at) AS lastCompletedAt
                FROM bat_center_cut_item
                WHERE center_cut_job_id = ?
                """, resolvedJobId));
        result.putAll(prefix("result", queryForMapOrEmpty(batJdbcTemplate, """
                SELECT COUNT(*) AS totalCount,
                       SUM(CASE WHEN result_status = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
                       SUM(CASE WHEN result_status = 'FAILED' THEN 1 ELSE 0 END) AS failedCount,
                       MAX(created_at) AS lastCreatedAt
                FROM bat_center_cut_result
                WHERE center_cut_job_id = ?
                """, resolvedJobId)));
        return result;
    }

    public List<Map<String, Object>> findTargets(String centerCutJobId, String statusCode, int limit) {
        String resolvedJobId = TextUtils.requireText(centerCutJobId, "centerCutJobId");
        int resolvedLimit = safeLimit(limit);
        if ("REF_SAMPLE".equals(resolveAdapterType(resolvedJobId))) {
            List<Object> args = new ArrayList<>();
            args.add(resolvedJobId);
            String statusCondition = "";
            if (TextUtils.hasText(statusCode)) {
                statusCondition = " AND status_code = ?";
                args.add(statusCode.trim());
            }
            args.add(resolvedLimit);
            return queryOrEmpty(refJdbcTemplate, """
                    SELECT target_id AS targetId,
                           center_cut_job_id AS centerCutJobId,
                           business_key AS businessKey,
                           business_date AS businessDate,
                           status_code AS statusCode,
                           retry_count AS retryCount,
                           parent_transaction_global_id AS parentTransactionGlobalId,
                           child_transaction_global_id AS childTransactionGlobalId,
                           started_at AS startedAt,
                           completed_at AS completedAt,
                           last_error_message AS lastErrorMessage,
                           CASE
                               WHEN target_payload IS NULL THEN NULL
                               ELSE CONCAT('[MASKED target payload length=', CHAR_LENGTH(target_payload), ']')
                           END AS targetPayloadMasked,
                           CHAR_LENGTH(target_payload) AS targetPayloadLength,
                           created_at AS createdAt,
                           updated_at AS updatedAt
                    FROM ref_center_cut_sample_target
                    WHERE center_cut_job_id = ?
                    """ + statusCondition + """
                    ORDER BY target_id
                    LIMIT ?
                    """, args.toArray());
        }

        List<Object> args = new ArrayList<>();
        args.add(resolvedJobId);
        String statusCondition = "";
        if (TextUtils.hasText(statusCode)) {
            statusCondition = " AND item_status = ?";
            args.add(statusCode.trim());
        }
        args.add(resolvedLimit);
        return queryOrEmpty(batJdbcTemplate, """
                SELECT center_cut_item_id AS targetId,
                       center_cut_job_id AS centerCutJobId,
                       business_key AS businessKey,
                       business_date AS businessDate,
                       item_status AS statusCode,
                       retry_count AS retryCount,
                       parent_transaction_global_id AS parentTransactionGlobalId,
                       child_transaction_global_id AS childTransactionGlobalId,
                       started_at AS startedAt,
                       completed_at AS completedAt,
                       last_error_message AS lastErrorMessage,
                       CASE
                           WHEN item_payload IS NULL THEN NULL
                           ELSE CONCAT('[MASKED item payload length=', CHAR_LENGTH(item_payload), ']')
                       END AS targetPayloadMasked,
                       CHAR_LENGTH(item_payload) AS targetPayloadLength,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM bat_center_cut_item
                WHERE center_cut_job_id = ?
                """ + statusCondition + """
                ORDER BY center_cut_item_id
                LIMIT ?
                """, args.toArray());
    }

    public List<Map<String, Object>> findResults(String centerCutJobId, String resultStatus, int limit) {
        String resolvedJobId = TextUtils.requireText(centerCutJobId, "centerCutJobId");
        int resolvedLimit = safeLimit(limit);
        if ("REF_SAMPLE".equals(resolveAdapterType(resolvedJobId))) {
            List<Object> args = new ArrayList<>();
            args.add(resolvedJobId);
            String statusCondition = "";
            if (TextUtils.hasText(resultStatus)) {
                statusCondition = " AND result_status = ?";
                args.add(resultStatus.trim());
            }
            args.add(resolvedLimit);
            return queryOrEmpty(refJdbcTemplate, """
                    SELECT result_id AS resultId,
                           target_id AS targetId,
                           center_cut_job_id AS centerCutJobId,
                           business_key AS businessKey,
                           result_status AS resultStatus,
                           result_message AS resultMessage,
                           CASE
                               WHEN result_payload IS NULL THEN NULL
                               ELSE CONCAT('[MASKED result payload length=', CHAR_LENGTH(result_payload), ']')
                           END AS resultPayloadMasked,
                           CHAR_LENGTH(result_payload) AS resultPayloadLength,
                           parent_transaction_global_id AS parentTransactionGlobalId,
                           child_transaction_global_id AS childTransactionGlobalId,
                           created_at AS createdAt,
                           updated_at AS updatedAt
                    FROM ref_center_cut_sample_result
                    WHERE center_cut_job_id = ?
                    """ + statusCondition + """
                    ORDER BY result_id
                    LIMIT ?
                    """, args.toArray());
        }

        List<Object> args = new ArrayList<>();
        args.add(resolvedJobId);
        String statusCondition = "";
        if (TextUtils.hasText(resultStatus)) {
            statusCondition = " AND r.result_status = ?";
            args.add(resultStatus.trim());
        }
        args.add(resolvedLimit);
        return queryOrEmpty(batJdbcTemplate, """
                SELECT r.center_cut_result_id AS resultId,
                       r.center_cut_item_id AS targetId,
                       r.center_cut_job_id AS centerCutJobId,
                       i.business_key AS businessKey,
                       r.result_status AS resultStatus,
                       r.result_message AS resultMessage,
                       CASE
                           WHEN r.result_payload IS NULL THEN NULL
                           ELSE CONCAT('[MASKED result payload length=', CHAR_LENGTH(r.result_payload), ']')
                       END AS resultPayloadMasked,
                       CHAR_LENGTH(r.result_payload) AS resultPayloadLength,
                       i.parent_transaction_global_id AS parentTransactionGlobalId,
                       r.child_transaction_global_id AS childTransactionGlobalId,
                       r.created_at AS createdAt,
                       r.updated_at AS updatedAt
                FROM bat_center_cut_result r
                LEFT JOIN bat_center_cut_item i ON i.center_cut_item_id = r.center_cut_item_id
                WHERE r.center_cut_job_id = ?
                """ + statusCondition + """
                ORDER BY r.center_cut_result_id
                LIMIT ?
                """, args.toArray());
    }

    public Map<String, Object> findResultDetail(String resultId) {
        Long resolvedResultId = parseId(resultId);
        if (resolvedResultId == null) {
            return Map.of("resultId", resultId, "found", false, "reason", "숫자형 resultId가 아닙니다.");
        }

        Map<String, Object> refResult = queryForMapOrEmpty(refJdbcTemplate, """
                SELECT result_id AS resultId,
                       target_id AS targetId,
                       center_cut_job_id AS centerCutJobId,
                       business_key AS businessKey,
                       result_status AS resultStatus,
                       result_message AS resultMessage,
                       CASE
                           WHEN result_payload IS NULL THEN NULL
                           ELSE CONCAT('[MASKED result payload length=', CHAR_LENGTH(result_payload), ']')
                       END AS resultPayloadMasked,
                       CHAR_LENGTH(result_payload) AS resultPayloadLength,
                       parent_transaction_global_id AS parentTransactionGlobalId,
                       child_transaction_global_id AS childTransactionGlobalId,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                FROM ref_center_cut_sample_result
                WHERE result_id = ?
                """, resolvedResultId);
        if (!refResult.isEmpty()) {
            refResult.put("adapterType", "REF_SAMPLE");
            refResult.put("found", true);
            return refResult;
        }

        Map<String, Object> batResult = queryForMapOrEmpty(batJdbcTemplate, """
                SELECT r.center_cut_result_id AS resultId,
                       r.center_cut_item_id AS targetId,
                       r.center_cut_job_id AS centerCutJobId,
                       i.business_key AS businessKey,
                       r.result_status AS resultStatus,
                       r.result_message AS resultMessage,
                       CASE
                           WHEN r.result_payload IS NULL THEN NULL
                           ELSE CONCAT('[MASKED result payload length=', CHAR_LENGTH(r.result_payload), ']')
                       END AS resultPayloadMasked,
                       CHAR_LENGTH(r.result_payload) AS resultPayloadLength,
                       i.parent_transaction_global_id AS parentTransactionGlobalId,
                       r.child_transaction_global_id AS childTransactionGlobalId,
                       r.created_at AS createdAt,
                       r.updated_at AS updatedAt
                FROM bat_center_cut_result r
                LEFT JOIN bat_center_cut_item i ON i.center_cut_item_id = r.center_cut_item_id
                WHERE r.center_cut_result_id = ?
                """, resolvedResultId);
        if (!batResult.isEmpty()) {
            batResult.put("adapterType", "BAT_STANDARD");
            batResult.put("found", true);
            return batResult;
        }
        return Map.of("resultId", resolvedResultId, "found", false);
    }

    Map<String, Object> maskPayloadForDisplay(String fieldName, String payload) {
        Map<String, Object> masked = new LinkedHashMap<>();
        masked.put(fieldName + "Masked", payload == null ? null : "[MASKED " + fieldName + " length=" + payload.length() + "]");
        masked.put(fieldName + "Length", payload == null ? null : payload.length());
        return masked;
    }

    private Map<String, Object> findJob(String centerCutJobId) {
        return queryForMapOrEmpty(batJdbcTemplate, """
                SELECT c.center_cut_job_id AS centerCutJobId,
                       c.batch_job_id AS batchJobId,
                       c.center_cut_job_name AS centerCutJobName,
                       c.provider_key AS providerKey,
                       c.handler_key AS handlerKey,
                       c.chunk_size AS chunkSize,
                       c.retry_limit AS retryLimit,
                       c.use_yn AS useYn,
                       c.description AS description,
                       c.created_at AS createdAt,
                       c.updated_at AS updatedAt,
                       j.job_name AS batchJobName,
                       j.job_type AS batchJobType
                FROM bat_center_cut_job c
                LEFT JOIN bat_job j ON j.job_id = c.batch_job_id
                WHERE c.center_cut_job_id = ?
                """, centerCutJobId);
    }

    private String resolveAdapterType(String centerCutJobId) {
        if (REF_CENTER_CUT_SAMPLE_JOB_ID.equals(centerCutJobId)) {
            return "REF_SAMPLE";
        }
        Map<String, String> parameterMap = parameterMap(centerCutJobId);
        String targetTable = parameterMap.getOrDefault("targettable", "");
        String resultTable = parameterMap.getOrDefault("resulttable", "");
        if (targetTable.toLowerCase(Locale.ROOT).contains("ref_center_cut_sample_target")
                || resultTable.toLowerCase(Locale.ROOT).contains("ref_center_cut_sample_result")) {
            return "REF_SAMPLE";
        }
        return "BAT_STANDARD";
    }

    private Map<String, String> parameterMap(String centerCutJobId) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map<String, Object> row : findParameters(centerCutJobId)) {
            String key = getString(row, "parameterKey");
            String value = getString(row, "parameterValue");
            if (TextUtils.hasText(key)) {
                result.put(key.toLowerCase(Locale.ROOT), value == null ? "" : value);
            }
        }
        return result;
    }

    private Map<String, Object> prefix(String prefix, Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(prefix + Character.toUpperCase(key.charAt(0)) + key.substring(1), value));
        return result;
    }

    private List<Map<String, Object>> queryOrEmpty(JdbcTemplate jdbcTemplate, String sql, Object... args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (DataAccessException ex) {
            log.warn("ADM Center-Cut 관제 조회에 실패했습니다. message={}", ex.getMessage());
            return List.of();
        }
    }

    private Map<String, Object> queryForMapOrEmpty(JdbcTemplate jdbcTemplate, String sql, Object... args) {
        List<Map<String, Object>> rows = queryOrEmpty(jdbcTemplate, sql, args);
        if (rows.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(rows.get(0));
    }

    private int safeLimit(int limit) {
        int resolvedLimit = limit <= 0 ? DEFAULT_LIMIT : limit;
        return Math.min(resolvedLimit, MAX_LIMIT);
    }

    private Long parseId(String value) {
        if (!TextUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String getString(Map<String, Object> row, String key) {
        Object direct = row.get(key);
        if (direct != null) {
            return String.valueOf(direct);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null) {
                return String.valueOf(entry.getValue());
            }
        }
        return "";
    }
}
