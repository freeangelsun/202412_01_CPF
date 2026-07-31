package com.cpf.batch.execution;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** CPF 승인·감사 Control Plane과 Spring Batch Metadata ID를 연결하는 JDBC Adapter입니다. */
public final class JdbcBatchExecutionControlPlaneAdapter implements BatchExecutionLedgerPort, BatchFencingPort {
    private final JdbcTemplate jdbc;

    public JdbcBatchExecutionControlPlaneAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional
    public String reserve(BatchApprovedLaunchRequest request) {
        List<String> existing = jdbc.query(
                "select cpf_execution_id from CPF_BATCH_EXECUTION_CONTROL where idempotency_key = ?",
                (rs, rowNum) -> rs.getString(1), request.idempotencyKey());
        if (!existing.isEmpty()) return existing.getFirst();
        String executionId = "BAT-" + UUID.randomUUID();
        try {
            jdbc.update("""
                    insert into CPF_BATCH_EXECUTION_CONTROL
                    (cpf_execution_id, job_id, definition_version, approval_id, operator_id, reason,
                     idempotency_key, fencing_token, control_status, unknown_reason, unknown_detail,
                     created_at, updated_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, 'RESERVED', null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """, executionId, request.definition().jobId(), request.definition().definitionVersion(),
                    request.approvalId(), request.operatorId(), request.reason(), request.idempotencyKey(),
                    request.fencingToken());
            return executionId;
        } catch (DuplicateKeyException race) {
            return jdbc.queryForObject(
                    "select cpf_execution_id from CPF_BATCH_EXECUTION_CONTROL where idempotency_key = ?",
                    String.class, request.idempotencyKey());
        }
    }

    @Override
    @Transactional
    public void bind(BatchExecutionLink link) {
        assertCurrent(link.jobId(), link.cpfExecutionId(), link.fencingToken());
        int updated = jdbc.update("""
                update CPF_BATCH_EXECUTION_LINK
                   set spring_job_instance_id = ?, spring_job_execution_id = ?, spring_step_execution_id = ?,
                       spring_status = ?, updated_at = CURRENT_TIMESTAMP
                 where cpf_execution_id = ? and link_key = ?
                """, link.jobInstanceId(), link.jobExecutionId(), link.stepExecutionId(), link.status(),
                link.cpfExecutionId(), linkKey(link));
        if (updated == 0) {
            try {
                jdbc.update("""
                        insert into CPF_BATCH_EXECUTION_LINK
                        (cpf_execution_id, link_key, job_id, definition_version, spring_job_instance_id,
                         spring_job_execution_id, spring_step_execution_id, spring_status,
                         fencing_token, created_at, updated_at)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """, link.cpfExecutionId(), linkKey(link), link.jobId(), link.definitionVersion(), link.jobInstanceId(),
                        link.jobExecutionId(), link.stepExecutionId(), link.status(), link.fencingToken());
            } catch (DuplicateKeyException duplicate) {
                jdbc.update("""
                        update CPF_BATCH_EXECUTION_LINK
                           set spring_job_instance_id = ?, spring_status = ?, updated_at = CURRENT_TIMESTAMP
                         where cpf_execution_id = ? and link_key = ?
                        """, link.jobInstanceId(), link.status(), link.cpfExecutionId(), linkKey(link));
            }
        }
        jdbc.update("""
                update CPF_BATCH_EXECUTION_CONTROL
                   set job_instance_id = ?, job_execution_id = ?, control_status = ?, updated_at = CURRENT_TIMESTAMP
                 where cpf_execution_id = ? and fencing_token = ?
                """, link.jobInstanceId(), link.jobExecutionId(), link.status(),
                link.cpfExecutionId(), link.fencingToken());
    }

    @Override
    @Transactional
    public void recordUnknown(String cpfExecutionId, String reasonCode, String detail) {
        int updated = jdbc.update("""
                update CPF_BATCH_EXECUTION_CONTROL
                   set control_status = 'UNKNOWN_RESULT', unknown_reason = ?, unknown_detail = ?,
                       updated_at = CURRENT_TIMESTAMP
                 where cpf_execution_id = ?
                """, bounded(reasonCode, 100), bounded(detail, 4000), cpfExecutionId);
        if (updated != 1) throw new IllegalStateException("BATCH_EXECUTION_CONTROL_NOT_FOUND:" + cpfExecutionId);
    }

    @Override
    public List<BatchExecutionLink> findByCpfExecutionId(String cpfExecutionId) {
        return jdbc.query("""
                select cpf_execution_id, job_id, definition_version, spring_job_instance_id,
                       spring_job_execution_id, spring_step_execution_id, spring_status,
                       fencing_token, updated_at
                  from CPF_BATCH_EXECUTION_LINK
                 where cpf_execution_id = ?
                 order by spring_job_execution_id, spring_step_execution_id
                """, this::mapLink, cpfExecutionId);
    }

    @Override
    public void assertCurrent(String jobId, String cpfExecutionId, long fencingToken) {
        List<Boolean> current = jdbc.query("""
                select case when job_id = ? and fencing_token = ? then 1 else 0 end
                  from CPF_BATCH_EXECUTION_CONTROL
                 where cpf_execution_id = ?
                """, (rs, rowNum) -> rs.getInt(1) == 1, jobId, fencingToken, cpfExecutionId);
        if (current.size() != 1 || !current.getFirst()) {
            throw new SecurityException("BATCH_STALE_FENCING_TOKEN:" + cpfExecutionId);
        }
    }

    private BatchExecutionLink mapLink(ResultSet rs, int rowNum) throws SQLException {
        Number stepId = (Number) rs.getObject("spring_step_execution_id");
        return new BatchExecutionLink(
                rs.getString("cpf_execution_id"), rs.getString("job_id"), rs.getLong("definition_version"),
                rs.getLong("spring_job_instance_id"), rs.getLong("spring_job_execution_id"),
                stepId == null ? null : stepId.longValue(), rs.getString("spring_status"),
                rs.getLong("fencing_token"), rs.getTimestamp("updated_at").toInstant());
    }

    private static String linkKey(BatchExecutionLink link) {
        return link.jobExecutionId() + ":" + (link.stepExecutionId() == null ? "JOB" : link.stepExecutionId());
    }

    private static String bounded(String value, int maximum) {
        String cleaned = value == null ? "" : value.replaceAll("(?i)(password|token|secret)\\s*[=:]\\s*[^,;\\s]+", "$1=<masked>");
        return cleaned.length() <= maximum ? cleaned : cleaned.substring(0, maximum);
    }
}
