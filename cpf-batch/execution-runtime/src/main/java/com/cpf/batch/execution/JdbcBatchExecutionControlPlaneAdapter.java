package com.cpf.batch.execution;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.api.BatchExecutionReservation;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** CPF 승인·감사 Control Plane과 Spring Batch Metadata를 연결하는 JDBC Adapter입니다. */
public final class JdbcBatchExecutionControlPlaneAdapter implements BatchExecutionLedgerPort, BatchFencingPort {
    private static final String RESERVATION_COLUMNS = """
            cpf_execution_id, job_id, definition_version, approval_id,
            idempotency_scope, idempotency_key, request_hash, plan_checksum,
            fencing_token, control_status, job_instance_id, job_execution_id,
            reconcile_attempts, reconcile_after, updated_at
            """;

    private final JdbcTemplate jdbc;

    public JdbcBatchExecutionControlPlaneAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public String reserve(BatchApprovedLaunchRequest request) {
        request.plan().verifyIntegrity();
        Optional<BatchExecutionReservation> existing = findByScopeAndKey(
                request.idempotencyScope(), request.idempotencyKey());
        if (existing.isPresent()) {
            requireSameRequest(existing.get(), request);
            assertCurrent(request.definition().jobId(), existing.get().cpfExecutionId(), request.fencingToken());
            return existing.get().cpfExecutionId();
        }

        claimLatestEpoch(request.definition().jobId(), request.fencingToken());
        String executionId = "BAT-" + UUID.randomUUID();
        try {
            jdbc.update("""
                    insert into CPF_BATCH_EXECUTION_CONTROL
                    (cpf_execution_id, job_id, definition_version, approval_id, operator_id, reason,
                     idempotency_scope, idempotency_key, request_hash, plan_checksum,
                     fencing_token, control_status, control_version, reconcile_attempts,
                     unknown_reason, unknown_detail, reconcile_after, last_error_code, last_error_detail,
                     created_at, updated_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'RESERVED', 1, 0,
                            null, null, null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    executionId,
                    request.definition().jobId(),
                    request.definition().definitionVersion(),
                    request.approvalId(),
                    request.operatorId(),
                    request.reason(),
                    request.idempotencyScope(),
                    request.idempotencyKey(),
                    request.requestHash(),
                    request.plan().checksum(),
                    request.fencingToken());
            assertCurrent(request.definition().jobId(), executionId, request.fencingToken());
            return executionId;
        } catch (DuplicateKeyException race) {
            BatchExecutionReservation winner = findByScopeAndKey(
                            request.idempotencyScope(), request.idempotencyKey())
                    .orElseThrow(() -> new IllegalStateException("BATCH_IDEMPOTENCY_RACE_UNRESOLVED", race));
            requireSameRequest(winner, request);
            assertCurrent(request.definition().jobId(), winner.cpfExecutionId(), request.fencingToken());
            return winner.cpfExecutionId();
        }
    }

    @Override
    @Transactional
    public void transition(
            String cpfExecutionId,
            Set<BatchControlState> expected,
            BatchControlState target,
            String reasonCode,
            String detail,
            Instant reconcileAfter) {
        if (expected == null || expected.isEmpty()) throw new IllegalArgumentException("expected states are required");
        if (target == null) throw new IllegalArgumentException("target state is required");
        List<BatchControlState> ordered = expected.stream().sorted().toList();
        String placeholders = String.join(",", java.util.Collections.nCopies(ordered.size(), "?"));
        List<Object> parameters = new ArrayList<>();
        parameters.add(target.name());
        parameters.add(target == BatchControlState.UNKNOWN_RESULT ? bounded(reasonCode, 100) : null);
        parameters.add(target == BatchControlState.UNKNOWN_RESULT ? bounded(detail, 4000) : null);
        parameters.add(bounded(reasonCode, 100));
        parameters.add(bounded(detail, 4000));
        parameters.add(reconcileAfter == null ? null : Timestamp.from(reconcileAfter));
        parameters.add(cpfExecutionId);
        for (BatchControlState state : ordered) parameters.add(state.name());
        int updated = jdbc.update("""
                update CPF_BATCH_EXECUTION_CONTROL
                   set control_status = ?,
                       unknown_reason = ?,
                       unknown_detail = ?,
                       last_error_code = ?,
                       last_error_detail = ?,
                       reconcile_after = ?,
                       reconcile_attempts = case when ? = 'UNKNOWN_RESULT' then reconcile_attempts + 1 else reconcile_attempts end,
                       control_version = control_version + 1,
                       updated_at = CURRENT_TIMESTAMP
                 where cpf_execution_id = ?
                   and control_status in (%s)
                """.formatted(placeholders), expandTransitionParameters(parameters, target));
        if (updated != 1) {
            BatchExecutionReservation current = findReservation(cpfExecutionId)
                    .orElseThrow(() -> new IllegalStateException("BATCH_EXECUTION_CONTROL_NOT_FOUND:" + cpfExecutionId));
            if (current.state() == target) return;
            throw new IllegalStateException("BATCH_INVALID_STATE_TRANSITION:" + current.state() + "->" + target);
        }
    }

    /** target 상태를 SQL case parameter에도 넣기 위한 parameter 배열입니다. */
    private static Object[] expandTransitionParameters(List<Object> parameters, BatchControlState target) {
        List<Object> expanded = new ArrayList<>(parameters.size() + 1);
        expanded.addAll(parameters.subList(0, 6));
        expanded.add(target.name());
        expanded.addAll(parameters.subList(6, parameters.size()));
        return expanded.toArray();
    }

    @Override
    @Transactional
    public void bind(BatchExecutionLink link) {
        lockCurrentEpoch(link.jobId(), link.cpfExecutionId(), link.fencingToken());
        String key = linkKey(link);
        int updated = jdbc.update("""
                update CPF_BATCH_EXECUTION_LINK
                   set spring_job_instance_id = ?, spring_job_execution_id = ?, spring_step_execution_id = ?,
                       spring_status = ?, updated_at = CURRENT_TIMESTAMP
                 where cpf_execution_id = ? and link_key = ?
                   and job_id = ? and definition_version = ? and fencing_token = ?
                """,
                link.jobInstanceId(), link.jobExecutionId(), link.stepExecutionId(), link.status(),
                link.cpfExecutionId(), key, link.jobId(), link.definitionVersion(), link.fencingToken());
        if (updated == 0) {
            try {
                jdbc.update("""
                        insert into CPF_BATCH_EXECUTION_LINK
                        (cpf_execution_id, link_key, job_id, definition_version, spring_job_instance_id,
                         spring_job_execution_id, spring_step_execution_id, spring_status,
                         fencing_token, created_at, updated_at)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                        link.cpfExecutionId(), key, link.jobId(), link.definitionVersion(), link.jobInstanceId(),
                        link.jobExecutionId(), link.stepExecutionId(), link.status(), link.fencingToken());
            } catch (DuplicateKeyException duplicate) {
                updated = jdbc.update("""
                        update CPF_BATCH_EXECUTION_LINK
                           set spring_job_instance_id = ?, spring_job_execution_id = ?, spring_step_execution_id = ?,
                               spring_status = ?, updated_at = CURRENT_TIMESTAMP
                         where cpf_execution_id = ? and link_key = ?
                           and job_id = ? and definition_version = ? and fencing_token = ?
                        """,
                        link.jobInstanceId(), link.jobExecutionId(), link.stepExecutionId(), link.status(),
                        link.cpfExecutionId(), key, link.jobId(), link.definitionVersion(), link.fencingToken());
                if (updated != 1) {
                    throw new SecurityException("BATCH_LINK_IMMUTABLE_FIELD_CONFLICT:" + link.cpfExecutionId());
                }
            }
        }

        // Step link는 Job 전체 상태를 덮지 않습니다. JobExecution link만 Control 상태를 갱신합니다.
        if (link.stepExecutionId() == null) {
            BatchControlState state = mapSpringStatus(link.status());
            int controlUpdated = jdbc.update("""
                    update CPF_BATCH_EXECUTION_CONTROL
                       set job_instance_id = ?, job_execution_id = ?, control_status = ?,
                           control_version = control_version + 1, updated_at = CURRENT_TIMESTAMP
                     where cpf_execution_id = ? and job_id = ? and definition_version = ?
                       and plan_checksum is not null and fencing_token = ?
                       and exists (
                           select 1 from CPF_BATCH_EXECUTION_EPOCH epoch
                            where epoch.job_id = CPF_BATCH_EXECUTION_CONTROL.job_id
                              and epoch.current_fencing_token = CPF_BATCH_EXECUTION_CONTROL.fencing_token
                       )
                    """,
                    link.jobInstanceId(), link.jobExecutionId(), state.name(),
                    link.cpfExecutionId(), link.jobId(), link.definitionVersion(), link.fencingToken());
            if (controlUpdated != 1) {
                throw new SecurityException("BATCH_CONTROL_BIND_FENCE_CONFLICT:" + link.cpfExecutionId());
            }
        }
    }

    @Override
    public Optional<BatchExecutionReservation> findReservation(String cpfExecutionId) {
        List<BatchExecutionReservation> rows = jdbc.query(
                "select " + RESERVATION_COLUMNS + " from CPF_BATCH_EXECUTION_CONTROL where cpf_execution_id = ?",
                this::mapReservation,
                cpfExecutionId);
        return rows.stream().findFirst();
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
                select case when control.job_id = ?
                                  and control.fencing_token = ?
                                  and epoch.current_fencing_token = control.fencing_token
                                  and control.control_status not in ('ABANDONED','REJECTED')
                                 then 1 else 0 end
                  from CPF_BATCH_EXECUTION_CONTROL control
                  join CPF_BATCH_EXECUTION_EPOCH epoch on epoch.job_id = control.job_id
                 where control.cpf_execution_id = ?
                """, (rs, rowNum) -> rs.getInt(1) == 1, jobId, fencingToken, cpfExecutionId);
        if (current.size() != 1 || !current.getFirst()) {
            throw new SecurityException("BATCH_STALE_FENCING_TOKEN:" + cpfExecutionId);
        }
    }

    /**
     * 신뢰된 Control Plane이 발급한 단조 증가 fencing token을 Job별 최신 epoch로 승격합니다.
     * 낮은 token은 즉시 거부하고, 동일 token은 멱등 재사용합니다.
     */
    private void claimLatestEpoch(String jobId, long fencingToken) {
        int advanced = jdbc.update("""
                update CPF_BATCH_EXECUTION_EPOCH
                   set current_fencing_token = ?, epoch_version = epoch_version + 1,
                       updated_at = CURRENT_TIMESTAMP
                 where job_id = ? and current_fencing_token < ?
                """, fencingToken, jobId, fencingToken);
        if (advanced == 0) {
            try {
                jdbc.update("""
                        insert into CPF_BATCH_EXECUTION_EPOCH
                        (job_id, current_fencing_token, epoch_version, updated_at)
                        values (?, ?, 1, CURRENT_TIMESTAMP)
                        """, jobId, fencingToken);
            } catch (DuplicateKeyException concurrentClaim) {
                // 다른 인스턴스가 먼저 생성했으므로 아래 exact token 검증으로 판정합니다.
            }
        }
        Long current = jdbc.queryForObject(
                "select current_fencing_token from CPF_BATCH_EXECUTION_EPOCH where job_id = ?",
                Long.class, jobId);
        if (current == null || current.longValue() != fencingToken) {
            throw new SecurityException("BATCH_STALE_FENCING_EPOCH:" + jobId + ":" + fencingToken);
        }
    }

    /** bind Transaction 동안 epoch row를 잠가 commit 직전까지 token 교체를 차단합니다. */
    private void lockCurrentEpoch(String jobId, String cpfExecutionId, long fencingToken) {
        List<Long> current = jdbc.query(
                "select current_fencing_token from CPF_BATCH_EXECUTION_EPOCH where job_id = ? for update",
                (rs, rowNum) -> rs.getLong(1), jobId);
        if (current.size() != 1 || current.getFirst() != fencingToken) {
            throw new SecurityException("BATCH_STALE_FENCING_EPOCH:" + cpfExecutionId);
        }
        assertCurrent(jobId, cpfExecutionId, fencingToken);
    }

    private Optional<BatchExecutionReservation> findByScopeAndKey(String scope, String key) {
        List<BatchExecutionReservation> rows = jdbc.query(
                "select " + RESERVATION_COLUMNS
                        + " from CPF_BATCH_EXECUTION_CONTROL where idempotency_scope = ? and idempotency_key = ?",
                this::mapReservation,
                scope,
                key);
        return rows.stream().findFirst();
    }

    private static void requireSameRequest(BatchExecutionReservation existing, BatchApprovedLaunchRequest request) {
        boolean same = existing.jobId().equals(request.definition().jobId())
                && existing.definitionVersion() == request.definition().definitionVersion()
                && existing.approvalId().equals(request.approvalId())
                && existing.idempotencyScope().equals(request.idempotencyScope())
                && existing.requestHash().equals(request.requestHash())
                && existing.planChecksum().equals(request.plan().checksum());
        if (!same) {
            throw new CpfBatchIdempotencyConflictException(
                    "Existing execution immutable request differs: " + existing.cpfExecutionId());
        }
    }

    private BatchExecutionReservation mapReservation(ResultSet rs, int rowNum) throws SQLException {
        return new BatchExecutionReservation(
                rs.getString("cpf_execution_id"),
                rs.getString("job_id"),
                rs.getLong("definition_version"),
                rs.getString("approval_id"),
                rs.getString("idempotency_scope"),
                rs.getString("idempotency_key"),
                rs.getString("request_hash").toLowerCase(Locale.ROOT),
                rs.getString("plan_checksum").toLowerCase(Locale.ROOT),
                rs.getLong("fencing_token"),
                BatchControlState.valueOf(rs.getString("control_status")),
                nullableLong(rs, "job_instance_id"),
                nullableLong(rs, "job_execution_id"),
                rs.getInt("reconcile_attempts"),
                instant(rs, "reconcile_after"),
                rs.getTimestamp("updated_at").toInstant());
    }

    private BatchExecutionLink mapLink(ResultSet rs, int rowNum) throws SQLException {
        return new BatchExecutionLink(
                rs.getString("cpf_execution_id"),
                rs.getString("job_id"),
                rs.getLong("definition_version"),
                nullableLong(rs, "spring_job_instance_id"),
                nullableLong(rs, "spring_job_execution_id"),
                nullableLong(rs, "spring_step_execution_id"),
                rs.getString("spring_status"),
                rs.getLong("fencing_token"),
                rs.getTimestamp("updated_at").toInstant());
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static String linkKey(BatchExecutionLink link) {
        if (link.jobExecutionId() == null) throw new IllegalArgumentException("jobExecutionId is required");
        return link.jobExecutionId() + ":" + (link.stepExecutionId() == null ? "JOB" : link.stepExecutionId());
    }

    private static BatchControlState mapSpringStatus(String status) {
        if (status == null) return BatchControlState.UNKNOWN_RESULT;
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "STARTING", "STARTED", "UNKNOWN" -> BatchControlState.STARTED;
            case "STOPPING" -> BatchControlState.STOPPING;
            case "STOPPED" -> BatchControlState.STOPPED;
            case "COMPLETED" -> BatchControlState.COMPLETED;
            case "ABANDONED" -> BatchControlState.ABANDONED;
            case "FAILED" -> BatchControlState.FAILED;
            default -> BatchControlState.UNKNOWN_RESULT;
        };
    }

    private static String bounded(String value, int maximum) {
        String cleaned = value == null ? "" : value
                .replaceAll("(?i)(password|token|secret|authorization|cookie|session(?:id)?)\\s*[=:]\\s*[^,;\\s]+", "$1=<masked>")
                .replaceAll("[\\r\\n\\t]+", " ")
                .trim();
        return cleaned.length() <= maximum ? cleaned : cleaned.substring(0, maximum);
    }
}
