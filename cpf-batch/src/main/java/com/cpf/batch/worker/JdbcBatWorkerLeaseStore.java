package com.cpf.batch.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.core.common.logging.SensitiveDataMasker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * MariaDB row lock과 lease token을 사용해 여러 BAT 프로세스의 중복 claim을 차단합니다.
 */
@Repository
public class JdbcBatWorkerLeaseStore implements BatWorkerLeaseStore {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    public JdbcBatWorkerLeaseStore(
            @Qualifier("batJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Qualifier("batTransactionManager") PlatformTransactionManager transactionManager,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.objectMapper = objectMapper;
    }

    @Override
    public void register(BatWorkerIdentity identity, BatWorkerProperties properties) {
        jdbcTemplate.update("""
                INSERT INTO bat_worker (
                    worker_id, server_instance_id, host_name, process_id, thread_name,
                    worker_version, capabilities_json, max_concurrency, queue_capacity,
                    control_status, worker_status, active_yn, last_heartbeat_at,
                    description, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'RUNNING', 'IDLE', 'Y', CURRENT_TIMESTAMP(3), ?, 'BAT', 'BAT')
                ON DUPLICATE KEY UPDATE
                    server_instance_id = VALUES(server_instance_id),
                    host_name = VALUES(host_name),
                    process_id = VALUES(process_id),
                    thread_name = VALUES(thread_name),
                    worker_version = VALUES(worker_version),
                    capabilities_json = VALUES(capabilities_json),
                    max_concurrency = VALUES(max_concurrency),
                    queue_capacity = VALUES(queue_capacity),
                    worker_status = 'IDLE',
                    active_yn = 'Y',
                    last_heartbeat_at = CURRENT_TIMESTAMP(3),
                    updated_by = 'BAT',
                    updated_at = CURRENT_TIMESTAMP
                """,
                identity.workerId(), identity.instanceId(), identity.hostName(), identity.processId(),
                Thread.currentThread().getName(), identity.version(), json(properties.capabilities()),
                properties.maxConcurrency(), properties.queueCapacity(),
                "독립 BAT worker 등록");
    }

    @Override
    public String heartbeat(BatWorkerIdentity identity, String workerStatus, BatWorkerLease currentLease) {
        jdbcTemplate.update("""
                UPDATE bat_worker
                SET worker_status = ?,
                    active_yn = 'Y',
                    last_heartbeat_at = CURRENT_TIMESTAMP(3),
                    current_job_id = ?,
                    current_execution_id = ?,
                    updated_by = 'BAT',
                    updated_at = CURRENT_TIMESTAMP
                WHERE worker_id = ?
                """,
                workerStatus,
                currentLease == null ? null : currentLease.jobId(),
                currentLease == null ? null : currentLease.executionId(),
                identity.workerId());
        List<String> controls = jdbcTemplate.query(
                "SELECT control_status FROM bat_worker WHERE worker_id = ?",
                (rs, rowNum) -> rs.getString(1), identity.workerId());
        return controls.isEmpty() ? "STOPPED" : controls.getFirst();
    }

    @Override
    public int recoverExpiredLeases(String requestUser) {
        Integer recovered = transactionTemplate.execute(status -> {
            List<Long> executionIds = jdbcTemplate.queryForList("""
                    SELECT execution_id
                    FROM bat_execution_lease
                    WHERE lease_status IN ('CLAIMED', 'RUNNING')
                      AND lease_until <= CURRENT_TIMESTAMP(3)
                    FOR UPDATE
                    """, Long.class);
            if (executionIds.isEmpty()) {
                return 0;
            }
            for (Long executionId : executionIds) {
                jdbcTemplate.update("""
                        UPDATE bat_execution
                        SET execution_status = 'READY', worker_id = NULL, server_instance_id = NULL,
                            error_message = 'worker lease 만료로 재대기 전환', updated_by = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE execution_id = ? AND execution_status IN ('CLAIMED', 'STARTING', 'RUNNING')
                        """, requestUser, executionId);
                jdbcTemplate.update("""
                        UPDATE bat_execution_lease
                        SET lease_status = 'EXPIRED', updated_by = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE execution_id = ? AND lease_status IN ('CLAIMED', 'RUNNING')
                        """, requestUser, executionId);
            }
            return executionIds.size();
        });
        return recovered == null ? 0 : recovered;
    }

    @Override
    public Optional<BatWorkerLease> claim(BatWorkerIdentity identity, BatWorkerProperties properties) {
        return transactionTemplate.execute(status -> {
            String capabilities = String.join(",", properties.capabilities());
            List<Map<String, Object>> candidates = jdbcTemplate.queryForList("""
                    SELECT e.execution_id, e.job_id, e.job_parameters,
                           COALESCE(l.attempt_no, 0) AS previous_attempt,
                           COALESCE(l.takeover_count, 0) AS previous_takeover
                    FROM bat_execution e
                    LEFT JOIN bat_execution_lease l ON l.execution_id = e.execution_id
                    WHERE e.execution_status IN ('READY', 'REQUESTED')
                      AND (e.required_worker_version IS NULL OR e.required_worker_version = ?)
                      AND (e.required_capability IS NULL OR ? = '*' OR FIND_IN_SET(e.required_capability, ?) > 0)
                      AND (l.execution_id IS NULL OR l.lease_status IN ('RELEASED', 'EXPIRED'))
                    ORDER BY e.execution_id
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                    """, identity.version(), capabilities, capabilities);
            if (candidates.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> row = candidates.getFirst();
            long executionId = ((Number) row.get("execution_id")).longValue();
            int previousAttempt = ((Number) row.get("previous_attempt")).intValue();
            int previousTakeover = ((Number) row.get("previous_takeover")).intValue();
            String token = UUID.randomUUID().toString();
            jdbcTemplate.update("""
                    INSERT INTO bat_execution_lease (
                        execution_id, worker_id, lease_token, lease_status, claimed_at, lease_until,
                        last_heartbeat_at, attempt_no, takeover_count, created_by, updated_by
                    ) VALUES (?, ?, ?, 'CLAIMED', CURRENT_TIMESTAMP(3),
                              TIMESTAMPADD(SECOND, ?, CURRENT_TIMESTAMP(3)), CURRENT_TIMESTAMP(3), ?, ?, 'BAT', 'BAT')
                    ON DUPLICATE KEY UPDATE
                        takeover_count = takeover_count + IF(lease_status = 'EXPIRED', 1, 0),
                        worker_id = VALUES(worker_id), lease_token = VALUES(lease_token), lease_status = 'CLAIMED',
                        claimed_at = CURRENT_TIMESTAMP(3), lease_until = VALUES(lease_until),
                        last_heartbeat_at = CURRENT_TIMESTAMP(3), attempt_no = attempt_no + 1,
                        released_at = NULL, failure_message = NULL, updated_by = 'BAT', updated_at = CURRENT_TIMESTAMP
                    """, executionId, identity.workerId(), token, properties.leaseSeconds(),
                    previousAttempt + 1, previousTakeover);
            int updated = jdbcTemplate.update("""
                    UPDATE bat_execution
                    SET execution_status = 'CLAIMED', worker_id = ?, server_instance_id = ?,
                        start_time = COALESCE(start_time, CURRENT_TIMESTAMP(3)),
                        last_heartbeat_at = CURRENT_TIMESTAMP(3), updated_by = 'BAT', updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ? AND execution_status IN ('READY', 'REQUESTED')
                    """, identity.workerId(), identity.instanceId(), executionId);
            if (updated != 1) {
                status.setRollbackOnly();
                return Optional.empty();
            }
            return Optional.of(new BatWorkerLease(
                    executionId,
                    String.valueOf(row.get("job_id")),
                    row.get("job_parameters") == null ? null : String.valueOf(row.get("job_parameters")),
                    token,
                    identity.workerId(),
                    Instant.now().plusSeconds(properties.leaseSeconds()),
                    previousAttempt + 1,
                    previousTakeover));
        });
    }

    @Override
    public boolean markRunning(BatWorkerLease lease) {
        int leaseUpdated = jdbcTemplate.update("""
                UPDATE bat_execution_lease
                SET lease_status = 'RUNNING', last_heartbeat_at = CURRENT_TIMESTAMP(3), updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ? AND worker_id = ? AND lease_token = ? AND lease_status = 'CLAIMED'
                """, lease.executionId(), lease.workerId(), lease.leaseToken());
        if (leaseUpdated != 1) {
            return false;
        }
        jdbcTemplate.update("""
                UPDATE bat_execution
                SET execution_status = 'RUNNING', last_heartbeat_at = CURRENT_TIMESTAMP(3), updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ? AND worker_id = ?
                """, lease.executionId(), lease.workerId());
        return true;
    }

    @Override
    public boolean renew(BatWorkerLease lease, int leaseSeconds) {
        int updated = jdbcTemplate.update("""
                UPDATE bat_execution_lease
                SET lease_until = TIMESTAMPADD(SECOND, ?, CURRENT_TIMESTAMP(3)),
                    last_heartbeat_at = CURRENT_TIMESTAMP(3), updated_by = 'BAT', updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ? AND worker_id = ? AND lease_token = ?
                  AND lease_status IN ('CLAIMED', 'RUNNING') AND lease_until > CURRENT_TIMESTAMP(3)
                """, leaseSeconds, lease.executionId(), lease.workerId(), lease.leaseToken());
        if (updated == 1) {
            jdbcTemplate.update("""
                    UPDATE bat_execution
                    SET last_heartbeat_at = CURRENT_TIMESTAMP(3), updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ? AND worker_id = ?
                    """, lease.executionId(), lease.workerId());
        }
        return updated == 1;
    }

    @Override
    public boolean complete(
            BatWorkerLease lease,
            String executionStatus,
            Long springBatchExecutionId,
            String failureMessage) {
        Integer updated = transactionTemplate.execute(status -> {
            int leaseUpdated = jdbcTemplate.update("""
                    UPDATE bat_execution_lease
                    SET lease_status = 'RELEASED', released_at = CURRENT_TIMESTAMP(3),
                        failure_message = ?, updated_by = 'BAT', updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ? AND worker_id = ? AND lease_token = ?
                      AND lease_status IN ('CLAIMED', 'RUNNING')
                    """, SensitiveDataMasker.mask(failureMessage, 1000),
                    lease.executionId(), lease.workerId(), lease.leaseToken());
            if (leaseUpdated != 1) {
                status.setRollbackOnly();
                return 0;
            }
            return jdbcTemplate.update("""
                    UPDATE bat_execution
                    SET execution_status = ?, spring_batch_execution_id = COALESCE(?, spring_batch_execution_id),
                        end_time = CURRENT_TIMESTAMP(3), error_message = ?, last_heartbeat_at = CURRENT_TIMESTAMP(3),
                        updated_by = 'BAT', updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ? AND worker_id = ?
                    """, executionStatus, springBatchExecutionId,
                    SensitiveDataMasker.mask(failureMessage, 1000), lease.executionId(), lease.workerId());
        });
        return updated != null && updated == 1;
    }

    @Override
    public void markStopped(BatWorkerIdentity identity, String workerStatus) {
        jdbcTemplate.update("""
                UPDATE bat_worker
                SET worker_status = ?, control_status = 'STOPPED', active_yn = 'N',
                    current_job_id = NULL, current_execution_id = NULL,
                    updated_by = 'BAT', updated_at = CURRENT_TIMESTAMP
                WHERE worker_id = ?
                """, workerStatus, identity.workerId());
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("worker capability JSON 직렬화에 실패했습니다.", ex);
        }
    }
}
