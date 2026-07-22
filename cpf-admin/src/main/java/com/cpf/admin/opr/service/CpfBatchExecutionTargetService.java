package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.CpfBatchScheduleCandidate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 자동 배치 실행 대상 메타를 기록합니다.
 *
 * <p>스케줄러가 어떤 실행 대상을 만들었고, 실제 실행 이력과 어떻게 연결됐는지 ADM에서 조회할 수 있게 합니다.</p>
 */
@Service
public class CpfBatchExecutionTargetService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate cpfJdbcTemplate;

    public CpfBatchExecutionTargetService(@Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
    }

    public long createWaitingTarget(CpfBatchScheduleCandidate candidate, String requestUser) {
        cpfJdbcTemplate.update("""
                INSERT INTO cpf_batch_execution_target (
                    job_id, schedule_id, business_date, planned_run_at,
                    dispatch_status, dispatch_reason, created_by, updated_by
                ) VALUES (?, ?, ?, ?, 'WAITING', 'SCHEDULER_DUE', ?, ?)
                """,
                candidate.jobId(),
                candidate.scheduleId(),
                candidate.businessDate(),
                candidate.plannedRunAt(),
                requestUser,
                requestUser);
        Long targetId = cpfJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (targetId == null) {
            throw new IllegalStateException("배치 실행 대상 ID를 확인할 수 없습니다.");
        }
        return targetId;
    }

    public void markDispatched(long targetId, Long executionId, String requestUser) {
        cpfJdbcTemplate.update("""
                UPDATE cpf_batch_execution_target
                SET execution_id = ?,
                    dispatch_status = 'DISPATCHED',
                    dispatch_reason = 'SCHEDULER_DISPATCHED',
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE target_id = ?
                """, executionId, requestUser, targetId);
    }

    public void markFailed(long targetId, String reason, String requestUser) {
        cpfJdbcTemplate.update("""
                UPDATE cpf_batch_execution_target
                SET dispatch_status = 'FAILED',
                    dispatch_reason = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE target_id = ?
                """, reason, requestUser, targetId);
    }
}
