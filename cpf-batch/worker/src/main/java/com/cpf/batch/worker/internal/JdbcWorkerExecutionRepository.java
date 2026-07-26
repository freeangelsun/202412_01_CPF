package com.cpf.batch.worker.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class JdbcWorkerExecutionRepository {
    private final JdbcTemplate jdbc;
    public JdbcWorkerExecutionRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    public Work load(long executionId) {
        return jdbc.queryForObject("""
            SELECT execution_id,job_id,job_parameters,transaction_id,transaction_segment_id,business_date,requested_by
              FROM bat_execution WHERE execution_id=?
            """, (rs,n) -> new Work(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getDate(6)==null?null:rs.getDate(6).toLocalDate(),rs.getString(7)), executionId);
    }

    public void markRunning(long executionId,String workerId,Long springExecutionId,Long springInstanceId) {
        jdbc.update("""
            UPDATE bat_execution SET execution_status='RUNNING',worker_id=?,spring_batch_execution_id=?,spring_batch_job_instance_id=?,
                   start_time=COALESCE(start_time,CURRENT_TIMESTAMP(3)),last_heartbeat_at=CURRENT_TIMESTAMP(3),updated_at=CURRENT_TIMESTAMP
             WHERE execution_id=?
            """, workerId,springExecutionId,springInstanceId,executionId);
    }

    public void finish(long executionId,String status,String message) {
        jdbc.update("""
            UPDATE bat_execution SET execution_status=?,end_time=CURRENT_TIMESTAMP(3),error_message=?,
                   last_heartbeat_at=CURRENT_TIMESTAMP(3),updated_at=CURRENT_TIMESTAMP WHERE execution_id=?
            """, status,SensitiveTextSanitizer.sanitize(message),executionId);
    }

    public record Work(long executionId,String jobId,String parametersJson,String transactionId,
                       String segmentId,LocalDate businessDate,String requestedBy) {}
}
