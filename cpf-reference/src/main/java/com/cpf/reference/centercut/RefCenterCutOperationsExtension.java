package com.cpf.reference.centercut;

import com.cpf.core.api.batch.CpfCenterCutOperationsExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REF EDU 전용 Center-Cut 조회 확장.
 *
 * <p>플랫폼 ADM이 refDB를 직접 조회하지 않도록 REF가 자기 DB ownership을 유지한 채
 * {@link CpfCenterCutOperationsExtension}을 통해 조회 기능만 노출한다.</p>
 */
@Component
public class RefCenterCutOperationsExtension implements CpfCenterCutOperationsExtension {

    private static final String JOB = "CPF_REF_CENTER_CUT_SAMPLE_JOB";
    private final JdbcTemplate jdbc;

    public RefCenterCutOperationsExtension(@Qualifier("refJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean supports(String centerCutJobId) {
        return JOB.equals(centerCutJobId);
    }

    @Override
    public Map<String, Object> findSummary(String centerCutJobId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("centerCutJobId", centerCutJobId);
        result.put("adapterType", "REF_SAMPLE");
        result.putAll(one("""
                SELECT COUNT(*) totalCount,
                       SUM(CASE WHEN status_code='READY' THEN 1 ELSE 0 END) readyCount,
                       SUM(CASE WHEN status_code='RUNNING' THEN 1 ELSE 0 END) runningCount,
                       SUM(CASE WHEN status_code='SUCCESS' THEN 1 ELSE 0 END) successCount,
                       SUM(CASE WHEN status_code='FAILED' THEN 1 ELSE 0 END) failedCount,
                       SUM(CASE WHEN status_code='SKIPPED' THEN 1 ELSE 0 END) skippedCount,
                       SUM(CASE WHEN status_code='RETRY_REQUESTED' THEN 1 ELSE 0 END) retryRequestedCount,
                       SUM(CASE WHEN status_code='STOP_REQUESTED' THEN 1 ELSE 0 END) stopRequestedCount,
                       MAX(started_at) lastStartedAt,
                       MAX(completed_at) lastCompletedAt
                  FROM ref_center_cut_sample_target
                 WHERE center_cut_job_id=?
                """, centerCutJobId));

        Map<String, Object> summary = one("""
                SELECT COUNT(*) totalCount,
                       SUM(CASE WHEN result_status='SUCCESS' THEN 1 ELSE 0 END) successCount,
                       SUM(CASE WHEN result_status='FAILED' THEN 1 ELSE 0 END) failedCount,
                       MAX(created_at) lastCreatedAt
                  FROM ref_center_cut_sample_result
                 WHERE center_cut_job_id=?
                """, centerCutJobId);
        summary.forEach((key, value) -> result.put(
                "result" + Character.toUpperCase(key.charAt(0)) + key.substring(1), value));
        return result;
    }

    @Override
    public List<Map<String, Object>> findTargets(String centerCutJobId, String status, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(centerCutJobId);
        String statusClause = "";
        if (status != null && !status.isBlank()) {
            statusClause = " AND status_code=?";
            args.add(status.trim());
        }
        args.add(limit);

        String sql = """
                SELECT target_id targetId,
                       center_cut_job_id centerCutJobId,
                       business_key businessKey,
                       business_date businessDate,
                       status_code statusCode,
                       retry_count retryCount,
                       transaction_id transactionId,
                       parent_segment_id parentSegmentId,
                       transaction_segment_id transactionSegmentId,
                       started_at startedAt,
                       completed_at completedAt,
                       last_error_message lastErrorMessage,
                       CASE WHEN target_payload IS NULL THEN NULL
                            ELSE CONCAT('[MASKED target payload length=', CHAR_LENGTH(target_payload), ']') END targetPayloadMasked,
                       CHAR_LENGTH(target_payload) targetPayloadLength,
                       created_at createdAt,
                       updated_at updatedAt
                  FROM ref_center_cut_sample_target
                 WHERE center_cut_job_id=?
                """ + statusClause + " ORDER BY target_id LIMIT ?";
        return query(sql, args.toArray());
    }

    @Override
    public List<Map<String, Object>> findResults(String centerCutJobId, String status, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(centerCutJobId);
        String statusClause = "";
        if (status != null && !status.isBlank()) {
            statusClause = " AND result_status=?";
            args.add(status.trim());
        }
        args.add(limit);

        String sql = """
                SELECT result_id resultId,
                       center_cut_job_id centerCutJobId,
                       target_id targetId,
                       business_key businessKey,
                       result_status resultStatus,
                       result_message resultMessage,
                       transaction_id transactionId,
                       parent_segment_id parentSegmentId,
                       transaction_segment_id transactionSegmentId,
                       CASE WHEN result_payload IS NULL THEN NULL
                            ELSE CONCAT('[MASKED result payload length=', CHAR_LENGTH(result_payload), ']') END resultPayloadMasked,
                       CHAR_LENGTH(result_payload) resultPayloadLength,
                       created_at createdAt,
                       updated_at updatedAt
                  FROM ref_center_cut_sample_result
                 WHERE center_cut_job_id=?
                """ + statusClause + " ORDER BY result_id DESC LIMIT ?";
        return query(sql, args.toArray());
    }

    @Override
    public Map<String, Object> findResultDetail(String resultId) {
        Long id;
        try {
            id = Long.valueOf(resultId);
        } catch (RuntimeException ex) {
            return Map.of();
        }
        return one("""
                SELECT result_id resultId,
                       center_cut_job_id centerCutJobId,
                       target_id targetId,
                       business_key businessKey,
                       result_status resultStatus,
                       result_message resultMessage,
                       transaction_id transactionId,
                       parent_segment_id parentSegmentId,
                       transaction_segment_id transactionSegmentId,
                       CASE WHEN result_payload IS NULL THEN NULL
                            ELSE CONCAT('[MASKED result payload length=', CHAR_LENGTH(result_payload), ']') END resultPayloadMasked,
                       CHAR_LENGTH(result_payload) resultPayloadLength,
                       created_at createdAt,
                       updated_at updatedAt
                  FROM ref_center_cut_sample_result
                 WHERE result_id=?
                """, id);
    }

    private List<Map<String, Object>> query(String sql, Object... args) {
        try {
            return jdbc.queryForList(sql, args);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("REF Center-Cut query failed", ex);
        }
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = query(sql, args);
        return rows.isEmpty() ? new LinkedHashMap<>() : new LinkedHashMap<>(rows.get(0));
    }
}
