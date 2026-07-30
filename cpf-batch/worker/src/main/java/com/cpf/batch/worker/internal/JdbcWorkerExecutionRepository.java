package com.cpf.batch.worker.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
public class JdbcWorkerExecutionRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public JdbcWorkerExecutionRepository(JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    public Work load(long executionId) {
        return jdbc.queryForObject(sql.required("worker-execution-load"),
            (rs,n) -> new Work(
                    rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                    rs.getDate(6)==null?null:rs.getDate(6).toLocalDate(), rs.getString(7),
                    rs.getInt(8), rs.getLong(9), rs.getString(10), rs.getString(11),
                    rs.getString(12), rs.getString(13)),
            executionId);
    }

    /** 실행 상태와 Attempt 원장을 같은 DB Transaction으로 시작합니다. */
    @Transactional
    public boolean startAttempt(JdbcWorkerLeaseRepository.Lease lease, Work work) {
        int running = jdbc.update(sql.required("worker-execution-mark-running"),
                lease.workerId(), null, null, lease.executionId(), lease.workerId(), lease.leaseToken(),
                lease.fencingToken());
        if (running != 1) {
            return false;
        }
        int attemptNo = work.restartAttempt() + 1;
        int inserted = jdbc.update(sql.required("worker-attempt-insert"),
                work.executionId(), attemptNo, work.definitionVersion(), work.definitionChecksum(),
                lease.workerId(), lease.fencingToken());
        if (inserted != 1) {
            throw new IllegalStateException("Worker attempt ledger insert failed");
        }
        return true;
    }

    /** Terminal 결과와 Attempt 원장을 원자적으로 확정합니다. */
    @Transactional
    public boolean completeAttempt(
            JdbcWorkerLeaseRepository.Lease lease, Work work, String status, String message) {
        String sanitized = SensitiveTextSanitizer.sanitize(message);
        int execution = jdbc.update(sql.required("worker-execution-finish"),
                status, sanitized, lease.executionId(), lease.workerId(), lease.leaseToken(),
                lease.fencingToken());
        if (execution != 1) {
            return false;
        }
        int attempt = jdbc.update(sql.required("worker-attempt-finish"),
                status, sanitized, work.executionId(), work.restartAttempt() + 1,
                lease.workerId(), lease.fencingToken());
        if (attempt != 1) {
            throw new IllegalStateException("Worker attempt ledger completion failed");
        }
        return true;
    }

    /** 재시도 가능한 실패를 원장에 남기고 Execution을 READY로 되돌립니다. */
    @Transactional
    public boolean requeueAttempt(
            JdbcWorkerLeaseRepository.Lease lease, Work work, String status, String message) {
        String sanitized = SensitiveTextSanitizer.sanitize(message);
        int execution = jdbc.update(sql.required("worker-execution-requeue-retryable"),
                sanitized, lease.executionId(), lease.workerId(), lease.leaseToken(),
                lease.fencingToken());
        if (execution != 1) {
            return false;
        }
        int attempt = jdbc.update(sql.required("worker-attempt-finish"),
                status, sanitized, work.executionId(), work.restartAttempt() + 1,
                lease.workerId(), lease.fencingToken());
        if (attempt != 1) {
            throw new IllegalStateException("Worker retry attempt ledger completion failed");
        }
        return true;
    }

    public boolean markRunning(JdbcWorkerLeaseRepository.Lease lease) {
        return jdbc.update(sql.required("worker-execution-mark-running"),
            lease.workerId(),null,null,lease.executionId(),lease.workerId(),lease.leaseToken(),
                lease.fencingToken()) == 1;
    }

    public boolean recordSpringExecution(
            JdbcWorkerLeaseRepository.Lease lease, Long springExecutionId, Long springInstanceId) {
        return jdbc.update(sql.required("worker-execution-record-spring"),
            springExecutionId,springInstanceId,lease.executionId(),lease.workerId(),lease.leaseToken(),
                lease.fencingToken()) == 1;
    }

    public boolean finish(JdbcWorkerLeaseRepository.Lease lease,String status,String message) {
        return jdbc.update(sql.required("worker-execution-finish"),
            status,SensitiveTextSanitizer.sanitize(message),lease.executionId(),lease.workerId(),
                lease.leaseToken(),lease.fencingToken()) == 1;
    }

    public record Work(
            long executionId,
            String jobId,
            String parametersJson,
            String transactionId,
            String segmentId,
            LocalDate businessDate,
            String requestedBy,
            int restartAttempt,
            long definitionVersion,
            String definitionChecksum,
            String executorType,
            String executorReference,
            String definitionJson) {}
}
