package cpf.xyz.edu.centercut;

import cpf.pfw.common.batch.centercut.CenterCutTargetProvider;
import cpf.pfw.common.batch.centercut.CpfCenterCutResult;
import cpf.pfw.common.batch.centercut.CpfCenterCutStatus;
import cpf.pfw.common.batch.centercut.CpfCenterCutTarget;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * XYZ 업무 DB 테이블을 PFW center-cut 표준 계약에 연결하는 adapter입니다.
 *
 * <p>PFW는 {@link CenterCutTargetProvider} 계약만 알고, 실제 대상/결과 저장소는
 * 업무 모듈인 XYZ가 소유합니다. 이 구조가 유지되어야 다른 업무 모듈도 PFW 수정 없이
 * 자기 업무 테이블을 center-cut에 연결할 수 있습니다.</p>
 */
@Repository("xyzCenterCutTargetProvider")
public class XyzCenterCutTargetRepository implements CenterCutTargetProvider {
    private final JdbcTemplate jdbcTemplate;

    public XyzCenterCutTargetRepository(@Qualifier("xyzJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CpfCenterCutTarget> findReadyTargets(String centerCutJobId, int limit) {
        String sql = """
                SELECT target_id, center_cut_job_id, business_key, business_date, target_payload,
                       parent_transaction_global_id, child_transaction_global_id, retry_count, status_code
                FROM xyz_center_cut_sample_target
                WHERE center_cut_job_id = ?
                  AND status_code = 'READY'
                  AND use_yn = 'Y'
                ORDER BY business_date ASC, target_id ASC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTarget(rs), centerCutJobId, Math.max(1, limit));
    }

    @Override
    public void markRunning(CpfCenterCutTarget target, String childTransactionGlobalId) {
        int updated = jdbcTemplate.update("""
                UPDATE xyz_center_cut_sample_target
                SET status_code = 'RUNNING',
                    child_transaction_global_id = ?,
                    started_at = CURRENT_TIMESTAMP,
                    updated_by = 'XYZ_CENTER_CUT',
                    updated_at = CURRENT_TIMESTAMP
                WHERE target_id = ?
                  AND center_cut_job_id = ?
                  AND status_code IN ('READY', 'RETRY_REQUESTED')
                """, childTransactionGlobalId, target.targetId(), target.centerCutJobId());
        if (updated == 0) {
            throw new IllegalStateException("center-cut 대상 상태를 RUNNING으로 변경하지 못했습니다. targetId=" + target.targetId());
        }
    }

    @Override
    public void markResult(CpfCenterCutTarget target, CpfCenterCutResult result) {
        String statusCode = result.status().name();
        String errorMessage = result.status() == CpfCenterCutStatus.FAILED ? result.message() : null;
        jdbcTemplate.update("""
                UPDATE xyz_center_cut_sample_target
                SET status_code = ?,
                    child_transaction_global_id = ?,
                    completed_at = CURRENT_TIMESTAMP,
                    last_error_message = ?,
                    updated_by = 'XYZ_CENTER_CUT',
                    updated_at = CURRENT_TIMESTAMP
                WHERE target_id = ?
                  AND center_cut_job_id = ?
                """, statusCode, result.childTransactionGlobalId(), errorMessage, target.targetId(), target.centerCutJobId());

        jdbcTemplate.update("""
                INSERT INTO xyz_center_cut_sample_result (
                    target_id, center_cut_job_id, business_key, result_status, result_payload,
                    result_message, parent_transaction_global_id, child_transaction_global_id,
                    created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'XYZ_CENTER_CUT', 'XYZ_CENTER_CUT')
                ON DUPLICATE KEY UPDATE
                    result_status = VALUES(result_status),
                    result_payload = VALUES(result_payload),
                    result_message = VALUES(result_message),
                    parent_transaction_global_id = VALUES(parent_transaction_global_id),
                    child_transaction_global_id = VALUES(child_transaction_global_id),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                target.targetId(),
                target.centerCutJobId(),
                target.businessKey(),
                statusCode,
                result.resultPayload(),
                result.message(),
                target.parentTransactionGlobalId(),
                result.childTransactionGlobalId());
    }

    public Map<String, Long> countResultsByStatus(String centerCutJobId) {
        return jdbcTemplate.query("""
                        SELECT result_status, COUNT(*) AS result_count
                        FROM xyz_center_cut_sample_result
                        WHERE center_cut_job_id = ?
                        GROUP BY result_status
                        """,
                rs -> {
                    Map<String, Long> counts = new java.util.LinkedHashMap<>();
                    while (rs.next()) {
                        counts.put(rs.getString("result_status"), rs.getLong("result_count"));
                    }
                    return counts;
                },
                centerCutJobId);
    }

    public List<Map<String, Object>> findResultSnapshots(String centerCutJobId) {
        return jdbcTemplate.queryForList("""
                SELECT target_id, business_key, result_status, result_message,
                       parent_transaction_global_id, child_transaction_global_id
                FROM xyz_center_cut_sample_result
                WHERE center_cut_job_id = ?
                ORDER BY target_id
                """, centerCutJobId);
    }

    public void resetSampleTargetsForSmoke() {
        jdbcTemplate.update("DELETE FROM xyz_center_cut_sample_result WHERE center_cut_job_id = ?", XyzCenterCutConstants.JOB_ID);
        jdbcTemplate.update("""
                UPDATE xyz_center_cut_sample_target
                SET status_code = 'READY',
                    child_transaction_global_id = NULL,
                    started_at = NULL,
                    completed_at = NULL,
                    last_error_message = NULL,
                    updated_by = 'XYZ_CENTER_CUT',
                    updated_at = CURRENT_TIMESTAMP
                WHERE center_cut_job_id = ?
                """, XyzCenterCutConstants.JOB_ID);
    }

    private CpfCenterCutTarget mapTarget(ResultSet rs) throws SQLException {
        Date businessDate = rs.getDate("business_date");
        return new CpfCenterCutTarget(
                rs.getString("target_id"),
                rs.getString("center_cut_job_id"),
                rs.getString("business_key"),
                businessDate == null ? LocalDate.now() : businessDate.toLocalDate(),
                rs.getString("target_payload"),
                rs.getString("parent_transaction_global_id"),
                rs.getString("child_transaction_global_id"),
                rs.getInt("retry_count"),
                CpfCenterCutStatus.valueOf(rs.getString("status_code").toUpperCase(Locale.ROOT)));
    }
}
