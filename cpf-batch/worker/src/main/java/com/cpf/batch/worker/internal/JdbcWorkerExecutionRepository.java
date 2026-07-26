package com.cpf.batch.worker.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class JdbcWorkerExecutionRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public JdbcWorkerExecutionRepository(JdbcTemplate jdbc, Environment environment) {
        this.jdbc = jdbc;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    public Work load(long executionId) {
        return jdbc.queryForObject(sql.required("worker-execution-load"),
            (rs,n) -> new Work(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getDate(6)==null?null:rs.getDate(6).toLocalDate(),rs.getString(7)), executionId);
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

    public record Work(long executionId,String jobId,String parametersJson,String transactionId,
                       String segmentId,LocalDate businessDate,String requestedBy) {}
}
