package com.cpf.batch.control.compat;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.common.calendar.CmnBusinessCalendar;
import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.batch.api.CpfBatchOperations;
import com.cpf.batch.api.CpfBatchExecutionRequest;
import com.cpf.batch.api.CpfBatchExecutionResult;
import com.cpf.core.api.context.CpfContexts;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import com.cpf.batch.api.CpfBatchRiskCommand;
import com.cpf.batch.api.CpfBatchOptimisticVersion;
import com.cpf.data.api.CpfDataRow;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashMap;

/** Existing ADM/BAT owner contract implemented by the new Control Server. */
@Service
public class BatchOperationsCompatibilityService implements CpfBatchOperationsPort, CpfBatchOperations {
    private static final int GHOST_ACTION_HEARTBEAT_TIMEOUT_SECONDS = 120;
    private static final Set<String> GHOST_ACTIVE_STATUSES = Set.of("RUNNING", "CLAIMED", "CLAIMING");
    private final JdbcTemplate jdbc;
    private final CmnBusinessCalendar calendar;
    private final TransactionTemplate tx;
    private final CpfVendorSqlCatalog sql;
    private final CpfBatchRiskCommandCoordinator riskCommands;

    public BatchOperationsCompatibilityService(
            JdbcTemplate jdbc,
            CmnBusinessCalendar calendar,
            PlatformTransactionManager transactionManager,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(jdbc, calendar, transactionManager, sqlCatalogProvider, null);
    }

    @Autowired
    public BatchOperationsCompatibilityService(
            JdbcTemplate jdbc,
            CmnBusinessCalendar calendar,
            PlatformTransactionManager transactionManager,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            CpfBatchRiskCommandCoordinator riskCommands) {
        this.jdbc = jdbc;
        this.calendar = calendar;
        this.tx = new TransactionTemplate(transactionManager);
        this.sql = sqlCatalogProvider.forModule("bat");
        this.riskCommands = riskCommands;
    }

    @Override
    public CpfBatchExecutionResult launch(CpfBatchExecutionRequest request) {
        Objects.requireNonNull(request, "request");
        String standardBatchId = requireText(request.standardBatchId(), "standardBatchId");
        String jobId = request.requiredJobId();
        String businessDate = requireText(request.businessDate(), "businessDate");
        if (!businessDate.matches("\\d{8}")) throw new IllegalArgumentException("businessDate must be yyyyMMdd");
        String idempotencyKey = requireText(request.idempotencyKey(), "idempotencyKey");
        String user = request.normalizedRequestUser("SYSTEM");
        String reason = request.normalizedReason("On-demand batch request");
        String transactionId = CpfContexts.requireCurrent().transactionId();

        CpfBatchExecutionResult replay = findOnDemandByIdempotency(standardBatchId, idempotencyKey);
        if (replay != null) return replay;

        String requestId = UUID.randomUUID().toString();
        try {
            jdbc.update(sql.required("on-demand-insert"), requestId, standardBatchId, idempotencyKey,
                    transactionId, businessDate, request.normalizedJobParameters(), reason, user, user, user);
        } catch (DataIntegrityViolationException duplicate) {
            CpfBatchExecutionResult existing = findOnDemandByIdempotency(standardBatchId, idempotencyKey);
            if (existing != null) return existing;
            throw duplicate;
        }

        try {
            CpfDataRow execution = createExecution(null, jobId, request.normalizedJobParameters(), user, reason);
            long executionId = ((Number) execution.get("execution_id")).longValue();
            jdbc.update(sql.required("on-demand-link-execution"), executionId, user, requestId);
            return status(requestId);
        } catch (RuntimeException failure) {
            jdbc.update(sql.required("on-demand-mark-failed"), "BATCH_LAUNCH_FAILED",
                    SensitiveTextSanitizer.sanitize(failure.getMessage()), user, requestId);
            throw failure;
        }
    }

    @Override
    public CpfBatchExecutionResult status(String executionRequestId) {
        String id = requireText(executionRequestId, "executionRequestId");
        List<Map<String,Object>> rows = jdbc.queryForList(sql.required("on-demand-find-by-request"), id);
        if (rows.isEmpty()) throw new IllegalArgumentException("On-demand batch request not found: " + id);
        return toBatchExecutionResult(rows.getFirst());
    }

    private CpfBatchExecutionResult findOnDemandByIdempotency(String standardBatchId, String idempotencyKey) {
        List<Map<String,Object>> rows = jdbc.queryForList(
                sql.required("on-demand-find-by-idempotency"), standardBatchId, idempotencyKey);
        return rows.isEmpty() ? null : toBatchExecutionResult(rows.getFirst());
    }

    private static CpfBatchExecutionResult toBatchExecutionResult(Map<String,Object> row) {
        Long cpfExecutionId = number(row.get("cpf_execution_id"));
        Long springExecutionId = number(row.get("spring_batch_execution_id"));
        String status = Objects.toString(row.get("request_status"), "UNKNOWN");
        boolean executed = !"REQUESTED".equalsIgnoreCase(status);
        return CpfBatchExecutionResult.of(
                Objects.toString(row.get("execution_request_id"), null),
                executed,
                Objects.toString(row.get("job_id"), Objects.toString(row.get("standard_batch_id"), null)),
                cpfExecutionId, springExecutionId, status,
                Objects.toString(row.get("failure_message"), status),
                Map.of(
                        "standardBatchId", Objects.toString(row.get("standard_batch_id"), ""),
                        "businessDate", Objects.toString(row.get("business_date"), ""),
                        "transactionId", Objects.toString(row.get("transaction_id"), "")));
    }

    private static Long number(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    @Override
    public List<CpfDataRow> findJobs() {
        return rows(jdbc.queryForList(sql.required("compat-jobs")));
    }

    @Override
    public CpfDataRow findJobDetail(String jobId) {
        return row(one("compat-job-detail", requireText(jobId, "jobId")));
    }

    @Override
    public List<CpfDataRow> findSchedules() {
        return rows(jdbc.queryForList(sql.required("compat-schedules")));
    }

    @Override
    public List<CpfDataRow> findExecutions(
            String jobId,
            String transactionId,
            Long springInstanceId,
            String workerId,
            String instanceId,
            int limit) {
        return findExecutions(
                jobId,
                transactionId,
                springInstanceId,
                workerId,
                instanceId,
                null,
                null,
                limit);
    }

    @Override
    public List<CpfDataRow> findExecutions(
            String jobId,
            String transactionId,
            Long springInstanceId,
            String workerId,
            String instanceId,
            String fromDate,
            String toDate,
            int limit) {
        LocalDateTime from = hasText(fromDate) ? LocalDate.parse(fromDate).atStartOfDay() : null;
        LocalDateTime to = hasText(toDate)
                ? LocalDate.parse(toDate).plusDays(1).atStartOfDay()
                : null;
        return rows(jdbc.queryForList(
                sql.required("compat-executions-search"),
                jobId, jobId,
                transactionId, transactionId,
                springInstanceId, springInstanceId,
                workerId, workerId,
                instanceId, instanceId,
                from, from,
                to, to,
                clamp(limit)));
    }

    @Override
    public CpfDataRow findExecutionDetail(long executionId) {
        CpfDataRow result = row(one("compat-execution-detail", executionId));
        result.put("steps", findStepExecutions(executionId, null, 1000));
        return result;
    }

    @Override
    public List<CpfDataRow> findInstances() {
        return rows(jdbc.queryForList(sql.required("compat-instances")));
    }

    @Override
    public List<CpfDataRow> findWorkers(int heartbeatTimeoutSeconds) {
        return rows(jdbc.queryForList(sql.required("compat-workers")));
    }

    @Override
    public List<CpfDataRow> findStepExecutions(
            Long executionId,
            String jobId,
            int limit) {
        return rows(jdbc.queryForList(
                sql.required("compat-step-executions"),
                executionId, executionId, jobId, jobId, clamp(limit)));
    }

    @Override
    public List<CpfDataRow> findRelations(String jobId) {
        return rows(jdbc.queryForList(sql.required("compat-relations"), jobId, jobId, jobId));
    }

    @Override
    public List<CpfDataRow> findExecutionTargets(
            String jobId,
            String status,
            int limit) {
        return rows(jdbc.queryForList(
                sql.required("compat-execution-targets"),
                jobId, jobId, status, status, clamp(limit)));
    }

    @Override
    public List<CpfDataRow> findLocks(String jobId) {
        return rows(jdbc.queryForList(sql.required("compat-locks"), jobId, jobId));
    }

    @Override
    public CpfDataRow releaseLock(String lockKey, String user, String reason) {
        throw expectedVersionRequired("releaseLock");
    }

    @Override
    public CpfDataRow releaseLock(
            String lockKey, String user, String reason, long expectedVersion) {
        throw riskMetadataRequired("releaseLock");
    }

    @Override
    public CpfDataRow releaseLock(String lockKey, CpfBatchRiskCommand command) {
        command.assertOperation("releaseLock", "bat_lock", lockKey);
        return riskCoordinator().executeRow(command, () -> releaseLockInternal(
                lockKey, command.requestUser(), command.reason(), command.requiredExpectedVersion()));
    }

    private CpfDataRow releaseLockInternal(
            String lockKey, String user, String reason, long expectedVersion) {
        requireText(lockKey, "lockKey");
        requireText(user, "requestUser");
        requireText(reason, "reason");
        return tx.execute(status -> {
            Map<String, Object> before = exactOne("compat-lock-for-update", lockKey);
            requireExpiredLock(before, lockKey);
            CpfBatchOptimisticVersion.assertMatches(before, expectedVersion, "lock:" + lockKey);
            int changed = jdbc.update(
                    sql.required("compat-lock-delete-expired"), lockKey, expectedVersion);
            requireSingleMutation(changed, "expired lock release", lockKey);
            Map<String, Object> after = Map.of(
                    "lockKey", lockKey,
                    "jobId", Objects.toString(before.get("job_id"), ""),
                    "ownerId", Objects.toString(before.get("owner_id"), ""),
                    "expectedVersion", expectedVersion,
                    "released", true);
            audit(
                    Objects.toString(before.get("job_id"), "SYSTEM"),
                    null,
                    "RELEASE_LOCK",
                    user,
                    reason,
                    before,
                    after);
            return CpfDataRow.copyOf(after);
        });
    }

    @Override
    public List<CpfDataRow> findGhostCandidates(int timeoutSeconds) {
        return rows(jdbc.queryForList(
                sql.required("compat-ghost-candidates"), Math.max(timeoutSeconds, 10)));
    }

    @Override
    public CpfDataRow actGhostExecution(
            long executionId, String action, String user, String reason) {
        throw expectedVersionRequired("actGhostExecution");
    }

    @Override
    public CpfDataRow actGhostExecution(
            long executionId,
            String action,
            String user,
            String reason,
            long expectedVersion) {
        throw riskMetadataRequired("actGhostExecution");
    }

    @Override
    public CpfDataRow actGhostExecution(
            long executionId, String action, CpfBatchRiskCommand command) {
        command.assertOperation("actGhostExecution", "bat_execution", String.valueOf(executionId));
        return riskCoordinator().executeRow(command, () -> actGhostExecutionInternal(
                executionId, action, command.requestUser(), command.reason(), command.requiredExpectedVersion()));
    }

    private CpfDataRow actGhostExecutionInternal(
            long executionId,
            String action,
            String user,
            String reason,
            long expectedVersion) {
        requireText(action, "actionType");
        requireText(user, "requestUser");
        requireText(reason, "reason");
        String normalized = action.toUpperCase(Locale.ROOT);
        if (!Set.of("FAIL", "ABANDON", "RELEASE_LOCK").contains(normalized)) {
            throw new IllegalArgumentException("unsupported ghost action");
        }
        return tx.execute(status -> {
            Map<String, Object> before = exactOne("compat-execution-lock", executionId);
            requireGhostCandidate(before, executionId);
            CpfBatchOptimisticVersion.assertMatches(
                    before, expectedVersion, "execution:" + executionId);
            String jobId = Objects.toString(before.get("job_id"), "");
            Map<String, Object> after = new LinkedHashMap<>();
            after.put("executionId", executionId);
            after.put("jobId", jobId);
            after.put("action", normalized);
            if ("RELEASE_LOCK".equals(normalized)) {
                Map<String, Object> lock = exactOne("compat-lock-expired-for-job-for-update", jobId);
                String lockKey = Objects.toString(lock.get("lock_key"), jobId);
                requireExpiredLock(lock, lockKey);
                long lockVersion = CpfBatchOptimisticVersion.read(lock, "lock:" + lockKey);
                int changed = jdbc.update(
                        sql.required("compat-lock-delete-expired"), lockKey, lockVersion);
                requireSingleMutation(changed, "ghost lock release", lockKey);
                after.put("releasedLockKey", lockKey);
                after.put("executionStatus", Objects.toString(before.get("execution_status"), ""));
                after.put("rowVersion", expectedVersion);
            } else {
                String targetStatus = "FAIL".equals(normalized) ? "FAILED" : "ABANDONED";
                int changed = jdbc.update(
                        sql.required("compat-execution-finish-ghost"),
                        targetStatus,
                        user,
                        executionId,
                        expectedVersion);
                requireSingleMutation(changed, "ghost execution transition", String.valueOf(executionId));
                after.put("executionStatus", targetStatus);
                after.put("rowVersion", Math.addExact(expectedVersion, 1));
            }
            after.put("result", "COMPLETED");
            audit(
                    jobId,
                    executionId,
                    "GHOST_" + normalized,
                    user,
                    reason,
                    before,
                    after);
            return CpfDataRow.copyOf(after);
        });
    }

    @Override
    public List<CpfDataRow> findOperationLogs(
            String jobId,
            Long executionId,
            int limit) {
        return rows(jdbc.queryForList(
                sql.required("compat-operation-logs"),
                jobId, jobId, executionId, executionId, clamp(limit)));
    }

    @Override
    public List<CpfDataRow> simulateSchedule(
            String scheduleId,
            String baseDate,
            int days) {
        Map<String, Object> schedule =
                one("compat-schedule-detail", requireText(scheduleId, "scheduleId"));
        LocalDate start = hasText(baseDate) ? LocalDate.parse(baseDate) : LocalDate.now();
        List<CpfDataRow> result = new java.util.ArrayList<>();
        for (int index = 0; index < Math.max(1, Math.min(days, 90)); index++) {
            LocalDate date = start.plusDays(index);
            result.add(CpfDataRow.of(
                    "date", date.toString(),
                    "businessDay", calendar.isBusinessDay(
                            Objects.toString(schedule.get("calendar_id"), "DEFAULT"),
                            date)));
        }
        return result;
    }

    @Override
    public CpfDataRow registerJob(
            String jobId,
            String jobName,
            String jobType,
            String description,
            String user) {
        requireText(jobId, "jobId");
        requireText(user, "requestUser");
        jdbc.update(
                sql.required("compat-job-upsert"),
                jobId,
                defaultText(jobName, jobId),
                defaultText(jobType, "TASKLET"),
                description,
                user,
                user);
        return findJobDetail(jobId);
    }

    @Override
    public CpfDataRow requestRun(
            String jobId,
            String parameters,
            String user,
            String reason) {
        throw riskMetadataRequired("requestRun");
    }

    @Override
    public CpfDataRow requestRun(
            String jobId, String parameters, CpfBatchRiskCommand command) {
        command.assertOperation("requestRun", "bat_job", jobId);
        return riskCoordinator().executeRow(command, () ->
                createExecution(null, jobId, parameters, command.requestUser(), command.reason()));
    }

    @Override
    public CpfDataRow requestScheduledRun(
            String scheduleId,
            String jobId,
            String parameters,
            String user,
            String reason) {
        requireText(scheduleId, "scheduleId");
        return createExecution(scheduleId, jobId, parameters, user, reason);
    }

    @Override
    public CpfDataRow requestRetry(long executionId, String user, String reason) {
        throw expectedVersionRequired("requestRetry");
    }

    @Override
    public CpfDataRow requestRetry(
            long executionId, String user, String reason, long expectedVersion) {
        throw riskMetadataRequired("requestRetry");
    }

    @Override
    public CpfDataRow requestRetry(long executionId, CpfBatchRiskCommand command) {
        command.assertOperation("requestRetry", "bat_execution", String.valueOf(executionId));
        return riskCoordinator().executeRow(command, () -> requestRetryInternal(
                executionId, command.requestUser(), command.reason(), command.requiredExpectedVersion()));
    }

    private CpfDataRow requestRetryInternal(
            long executionId, String user, String reason, long expectedVersion) {
        requireText(user, "requestUser");
        requireText(reason, "reason");
        return tx.execute(status -> {
            Map<String, Object> previous = exactOne("compat-execution-lock", executionId);
            CpfBatchOptimisticVersion.assertMatches(
                    previous, expectedVersion, "execution:" + executionId);
            CpfDataRow created = createExecution(
                    Objects.toString(previous.get("schedule_id"), null),
                    Objects.toString(previous.get("job_id"), ""),
                    Objects.toString(previous.get("job_parameters"), "{}"),
                    user,
                    reason);
            created.put("retriedFromExecutionId", executionId);
            created.put("sourceExpectedVersion", expectedVersion);
            return created;
        });
    }

    @Override
    public CpfDataRow requestStop(long executionId, String user, String reason) {
        throw expectedVersionRequired("requestStop");
    }

    @Override
    public CpfDataRow requestStop(
            long executionId, String user, String reason, long expectedVersion) {
        throw riskMetadataRequired("requestStop");
    }

    @Override
    public CpfDataRow requestStop(long executionId, CpfBatchRiskCommand command) {
        command.assertOperation("requestStop", "bat_execution", String.valueOf(executionId));
        return riskCoordinator().executeRow(command, () -> requestStopInternal(
                executionId, command.requestUser(), command.reason(), command.requiredExpectedVersion()));
    }

    private CpfDataRow requestStopInternal(
            long executionId, String user, String reason, long expectedVersion) {
        requireText(user, "requestUser");
        requireText(reason, "reason");
        return tx.execute(status -> {
            Map<String, Object> before = exactOne("compat-execution-lock", executionId);
            CpfBatchOptimisticVersion.assertMatches(
                    before, expectedVersion, "execution:" + executionId);
            int changed = jdbc.update(
                    sql.required("compat-execution-stop"),
                    user,
                    executionId,
                    expectedVersion);
            requireSingleMutation(changed, "execution stop request", String.valueOf(executionId));
            Map<String, Object> after = one("compat-execution-detail", executionId);
            audit(
                    Objects.toString(before.get("job_id"), ""),
                    executionId,
                    "STOP_REQUEST",
                    user,
                    reason,
                    before,
                    after);
            return CpfDataRow.copyOf(after);
        });
    }

    @Override
    public CpfDataRow updateScheduleEnabled(
            String scheduleId, boolean enabled, String user, String reason) {
        throw expectedVersionRequired("updateScheduleEnabled");
    }

    @Override
    public CpfDataRow updateScheduleEnabled(
            String scheduleId,
            boolean enabled,
            String user,
            String reason,
            long expectedVersion) {
        throw riskMetadataRequired("updateScheduleEnabled");
    }

    @Override
    public CpfDataRow updateScheduleEnabled(
            String scheduleId, boolean enabled, CpfBatchRiskCommand command) {
        command.assertOperation("updateScheduleEnabled", "bat_schedule", scheduleId);
        return riskCoordinator().executeRow(command, () -> updateScheduleEnabledInternal(
                scheduleId, enabled, command.requestUser(), command.reason(), command.requiredExpectedVersion()));
    }

    private CpfDataRow updateScheduleEnabledInternal(
            String scheduleId,
            boolean enabled,
            String user,
            String reason,
            long expectedVersion) {
        requireText(scheduleId, "scheduleId");
        requireText(user, "requestUser");
        requireText(reason, "reason");
        return tx.execute(status -> {
            Map<String, Object> before = exactOne("compat-schedule-lock", scheduleId);
            CpfBatchOptimisticVersion.assertMatches(
                    before, expectedVersion, "schedule:" + scheduleId);
            int changed = jdbc.update(
                    sql.required("compat-schedule-update-enabled"),
                    enabled ? "Y" : "N",
                    user,
                    scheduleId,
                    expectedVersion);
            requireSingleMutation(changed, "schedule enable update", scheduleId);
            Map<String, Object> after = one("compat-schedule-detail", scheduleId);
            audit(
                    Objects.toString(before.get("job_id"), ""),
                    null,
                    enabled ? "SCHEDULE_ENABLE" : "SCHEDULE_DISABLE",
                    user,
                    reason,
                    before,
                    after);
            return CpfDataRow.copyOf(after);
        });
    }

    @Override
    public List<CpfDataRow> runSchedulerOnce(String user) {
        throw riskMetadataRequired("runSchedulerOnce");
    }

    @Override
    public List<CpfDataRow> runSchedulerOnce(CpfBatchRiskCommand command) {
        command.assertOperation("runSchedulerOnce", "bat_schedule", "DUE_SCHEDULES");
        return riskCoordinator().executeRows(command, () -> {
            requireText(command.requestUser(), "requestUser");
            return rows(jdbc.queryForList(sql.required("compat-scheduler-due")));
        });
    }

    private CpfDataRow createExecution(
            String scheduleId,
            String jobId,
            String parameters,
            String user,
            String reason) {
        requireText(jobId, "jobId");
        requireText(user, "requestUser");
        requireText(reason, "reason");
        one("compat-job-validate-enabled", jobId);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    sql.required("compat-execution-insert"),
                    Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, jobId);
            statement.setObject(2, scheduleId);
            statement.setObject(3, hasText(parameters) ? parameters : "{}");
            statement.setObject(4, user);
            statement.setObject(5, user);
            return statement;
        }, keyHolder);
        long executionId = requiredGeneratedExecutionId(keyHolder);
        audit(
                jobId,
                executionId,
                "RUN_REQUEST",
                user,
                reason,
                Map.of(),
                Map.of("executionId", executionId));
        return row(one("compat-execution-detail", executionId));
    }

    private long requiredGeneratedExecutionId(KeyHolder keyHolder) {
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null) {
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                if ("execution_id".equalsIgnoreCase(entry.getKey())
                        && entry.getValue() instanceof Number number) {
                    return number.longValue();
                }
            }
            for (Object value : keys.values()) {
                if (value instanceof Number number) {
                    return number.longValue();
                }
            }
        }
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("BAT execution generated key was not returned");
        }
        return key.longValue();
    }

    private void audit(
            String jobId,
            Long executionId,
            String operation,
            String user,
            String reason,
            Object before,
            Object after) {
        jdbc.update(
                sql.required("compat-operation-audit"),
                defaultText(jobId, "SYSTEM"),
                executionId,
                operation,
                user,
                reason,
                SensitiveTextSanitizer.sanitize(String.valueOf(before)),
                SensitiveTextSanitizer.sanitize(String.valueOf(after)),
                "S",
                "OK",
                user,
                user);
    }


    private Map<String, Object> exactOne(String statementKey, Object... arguments) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql.required(statementKey), arguments);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("BAT resource not found: " + statementKey);
        }
        if (rows.size() != 1) {
            throw new IllegalStateException(
                    "BAT operation requires exactly one locked row: " + statementKey + " count=" + rows.size());
        }
        return rows.getFirst();
    }

    private static IllegalArgumentException expectedVersionRequired(String operation) {
        return new IllegalArgumentException("expectedVersion is required for BAT operation: " + operation);
    }

    private static void requireSingleMutation(int changed, String operation, String target) {
        if (changed != 1) {
            throw new IllegalStateException(
                    "BAT " + operation + " conflicted or changed an unexpected number of rows: target="
                            + target + " changed=" + changed);
        }
    }

    private static void requireExpiredLock(Map<String, Object> lock, String target) {
        LocalDateTime expiresAt = toLocalDateTime(lock.get("expire_at"), "expire_at");
        if (!expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("BAT lock is not expired: " + target);
        }
    }

    private static void requireGhostCandidate(Map<String, Object> execution, long executionId) {
        String state = Objects.toString(execution.get("execution_status"), "")
                .trim().toUpperCase(Locale.ROOT);
        if (!GHOST_ACTIVE_STATUSES.contains(state)) {
            throw new IllegalStateException(
                    "BAT execution is no longer in a ghost-eligible state: executionId="
                            + executionId + " status=" + state);
        }
        LocalDateTime heartbeat = toLocalDateTime(execution.get("last_heartbeat_at"), "last_heartbeat_at");
        LocalDateTime cutoff = LocalDateTime.now()
                .minusSeconds(GHOST_ACTION_HEARTBEAT_TIMEOUT_SECONDS);
        if (!heartbeat.isBefore(cutoff)) {
            throw new IllegalStateException(
                    "BAT execution heartbeat recovered before ghost action: executionId=" + executionId);
        }
    }

    private static LocalDateTime toLocalDateTime(Object value, String fieldName) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        }
        if (value instanceof java.time.Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        if (value != null) {
            try {
                return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
            } catch (java.time.format.DateTimeParseException ignored) {
                // handled below
            }
        }
        throw new IllegalStateException("BAT " + fieldName + " is missing or invalid");
    }

    private Map<String, Object> one(String statementKey, Object... arguments) {
        List<Map<String, Object>> rows =
                jdbc.queryForList(sql.required(statementKey), arguments);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("BAT resource not found");
        }
        return rows.getFirst();
    }

    private static CpfDataRow row(Object source) {
        return CpfDataRow.copyOf(source);
    }

    private static List<CpfDataRow> rows(Object source) {
        return CpfDataRow.copyRows(source);
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(value, 1000));
    }

    private static String requireText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String defaultText(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }
    private CpfBatchRiskCommandCoordinator riskCoordinator() {
        if (riskCommands == null) {
            throw new IllegalStateException("BAT risk command coordinator is required for dangerous operations");
        }
        return riskCommands;
    }

    private static IllegalArgumentException riskMetadataRequired(String operation) {
        return new IllegalArgumentException(
                "approval/idempotency risk command is required for BAT operation: " + operation);
    }

}
