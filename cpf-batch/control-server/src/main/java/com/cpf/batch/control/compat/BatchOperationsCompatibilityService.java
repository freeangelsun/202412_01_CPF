package com.cpf.batch.control.compat;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.common.calendar.CmnBusinessCalendar;
import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashMap;

/** Existing ADM/BAT owner contract implemented by the new Control Server. */
@Service
public class BatchOperationsCompatibilityService implements CpfBatchOperationsPort {
    private final JdbcTemplate jdbc;
    private final CmnBusinessCalendar calendar;
    private final TransactionTemplate tx;
    private final CpfVendorSqlCatalog sql;

    public BatchOperationsCompatibilityService(
            JdbcTemplate jdbc,
            CmnBusinessCalendar calendar,
            PlatformTransactionManager transactionManager,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.calendar = calendar;
        this.tx = new TransactionTemplate(transactionManager);
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Override
    public List<Map<String, Object>> findJobs() {
        return jdbc.queryForList(sql.required("compat-jobs"));
    }

    @Override
    public Map<String, Object> findJobDetail(String jobId) {
        return one("compat-job-detail", requireText(jobId, "jobId"));
    }

    @Override
    public List<Map<String, Object>> findSchedules() {
        return jdbc.queryForList(sql.required("compat-schedules"));
    }

    @Override
    public List<Map<String, Object>> findExecutions(
            String jobId,
            String transactionId,
            Long springInstanceId,
            String workerId,
            String serverInstanceId,
            int limit) {
        return findExecutions(
                jobId,
                transactionId,
                springInstanceId,
                workerId,
                serverInstanceId,
                null,
                null,
                limit);
    }

    @Override
    public List<Map<String, Object>> findExecutions(
            String jobId,
            String transactionId,
            Long springInstanceId,
            String workerId,
            String serverInstanceId,
            String fromDate,
            String toDate,
            int limit) {
        LocalDateTime from = hasText(fromDate) ? LocalDate.parse(fromDate).atStartOfDay() : null;
        LocalDateTime to = hasText(toDate)
                ? LocalDate.parse(toDate).plusDays(1).atStartOfDay()
                : null;
        return jdbc.queryForList(
                sql.required("compat-executions-search"),
                jobId, jobId,
                transactionId, transactionId,
                springInstanceId, springInstanceId,
                workerId, workerId,
                serverInstanceId, serverInstanceId,
                from, from,
                to, to,
                clamp(limit));
    }

    @Override
    public Map<String, Object> findExecutionDetail(long executionId) {
        Map<String, Object> result =
                new LinkedHashMap<>(one("compat-execution-detail", executionId));
        result.put("steps", findStepExecutions(executionId, null, 1000));
        return result;
    }

    @Override
    public List<Map<String, Object>> findInstances() {
        return jdbc.queryForList(sql.required("compat-instances"));
    }

    @Override
    public List<Map<String, Object>> findWorkers(int heartbeatTimeoutSeconds) {
        return jdbc.queryForList(sql.required("compat-workers"));
    }

    @Override
    public List<Map<String, Object>> findStepExecutions(
            Long executionId,
            String jobId,
            int limit) {
        return jdbc.queryForList(
                sql.required("compat-step-executions"),
                executionId, executionId, jobId, jobId, clamp(limit));
    }

    @Override
    public List<Map<String, Object>> findRelations(String jobId) {
        return jdbc.queryForList(sql.required("compat-relations"), jobId, jobId, jobId);
    }

    @Override
    public List<Map<String, Object>> findExecutionTargets(
            String jobId,
            String status,
            int limit) {
        return jdbc.queryForList(
                sql.required("compat-execution-targets"),
                jobId, jobId, status, status, clamp(limit));
    }

    @Override
    public List<Map<String, Object>> findLocks(String jobId) {
        return jdbc.queryForList(sql.required("compat-locks"), jobId, jobId);
    }

    @Override
    public Map<String, Object> releaseLock(String lockKey, String user, String reason) {
        requireText(lockKey, "lockKey");
        requireText(user, "requestUser");
        requireText(reason, "reason");
        return tx.execute(status -> {
            Map<String, Object> before = one("compat-lock-for-update", lockKey);
            int changed = jdbc.update(sql.required("compat-lock-delete-expired"), lockKey);
            audit(
                    Objects.toString(before.get("job_id"), "SYSTEM"),
                    null,
                    "RELEASE_LOCK",
                    user,
                    reason,
                    before,
                    Map.of("released", changed == 1));
            return Map.of("lockKey", lockKey, "released", changed == 1);
        });
    }

    @Override
    public List<Map<String, Object>> findGhostCandidates(int timeoutSeconds) {
        return jdbc.queryForList(
                sql.required("compat-ghost-candidates"), Math.max(timeoutSeconds, 10));
    }

    @Override
    public Map<String, Object> actGhostExecution(
            long executionId,
            String action,
            String user,
            String reason) {
        requireText(action, "actionType");
        requireText(user, "requestUser");
        requireText(reason, "reason");
        String normalized = action.toUpperCase(Locale.ROOT);
        if (!Set.of("FAIL", "ABANDON", "RELEASE_LOCK").contains(normalized)) {
            throw new IllegalArgumentException("unsupported ghost action");
        }
        return tx.execute(status -> {
            Map<String, Object> before = one("compat-execution-lock", executionId);
            String jobId = Objects.toString(before.get("job_id"), "");
            if ("RELEASE_LOCK".equals(normalized)) {
                jdbc.update(sql.required("compat-lock-delete-job-expired"), jobId);
            } else {
                jdbc.update(
                        sql.required("compat-execution-finish-ghost"),
                        "FAIL".equals(normalized) ? "FAILED" : "ABANDONED",
                        executionId);
            }
            audit(
                    jobId,
                    executionId,
                    "GHOST_" + normalized,
                    user,
                    reason,
                    before,
                    Map.of("action", normalized));
            return Map.of(
                    "executionId", executionId,
                    "action", normalized,
                    "status", "ACCEPTED");
        });
    }

    @Override
    public List<Map<String, Object>> findOperationLogs(
            String jobId,
            Long executionId,
            int limit) {
        return jdbc.queryForList(
                sql.required("compat-operation-logs"),
                jobId, jobId, executionId, executionId, clamp(limit));
    }

    @Override
    public List<Map<String, Object>> simulateSchedule(
            String scheduleId,
            String baseDate,
            int days) {
        Map<String, Object> schedule =
                one("compat-schedule-detail", requireText(scheduleId, "scheduleId"));
        LocalDate start = hasText(baseDate) ? LocalDate.parse(baseDate) : LocalDate.now();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int index = 0; index < Math.max(1, Math.min(days, 90)); index++) {
            LocalDate date = start.plusDays(index);
            result.add(Map.of(
                    "date", date.toString(),
                    "businessDay", calendar.isBusinessDay(
                            Objects.toString(schedule.get("calendar_id"), "DEFAULT"),
                            date)));
        }
        return result;
    }

    @Override
    public Map<String, Object> registerJob(
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
    public Map<String, Object> requestRun(
            String jobId,
            String parameters,
            String user,
            String reason) {
        return createExecution(null, jobId, parameters, user, reason);
    }

    @Override
    public Map<String, Object> requestScheduledRun(
            String scheduleId,
            String jobId,
            String parameters,
            String user,
            String reason) {
        requireText(scheduleId, "scheduleId");
        return createExecution(scheduleId, jobId, parameters, user, reason);
    }

    @Override
    public Map<String, Object> requestRetry(long executionId, String user, String reason) {
        Map<String, Object> previous = one("compat-execution-detail", executionId);
        return createExecution(
                Objects.toString(previous.get("schedule_id"), null),
                Objects.toString(previous.get("job_id"), ""),
                Objects.toString(previous.get("job_parameters"), "{}"),
                user,
                reason);
    }

    @Override
    public Map<String, Object> requestStop(long executionId, String user, String reason) {
        requireText(user, "requestUser");
        requireText(reason, "reason");
        Map<String, Object> before = one("compat-execution-detail", executionId);
        int changed = jdbc.update(sql.required("compat-execution-stop"), executionId);
        audit(
                Objects.toString(before.get("job_id"), ""),
                executionId,
                "STOP_REQUEST",
                user,
                reason,
                before,
                Map.of("updated", changed));
        return Map.of("executionId", executionId, "stopRequested", changed == 1);
    }

    @Override
    public Map<String, Object> updateScheduleEnabled(
            String scheduleId,
            boolean enabled,
            String user,
            String reason) {
        requireText(user, "requestUser");
        requireText(reason, "reason");
        Map<String, Object> before = one("compat-schedule-detail", scheduleId);
        int changed = jdbc.update(
                sql.required("compat-schedule-update-enabled"),
                enabled ? "Y" : "N",
                user,
                scheduleId);
        audit(
                Objects.toString(before.get("job_id"), ""),
                null,
                "SCHEDULE_ENABLE",
                user,
                reason,
                before,
                Map.of("enabled", enabled));
        return Map.of(
                "scheduleId", scheduleId,
                "updated", changed == 1,
                "enabled", enabled);
    }

    @Override
    public List<Map<String, Object>> runSchedulerOnce(String user) {
        requireText(user, "requestUser");
        return jdbc.queryForList(sql.required("compat-scheduler-due"));
    }

    private Map<String, Object> createExecution(
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
        return one("compat-execution-detail", executionId);
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

    private Map<String, Object> one(String statementKey, Object... arguments) {
        List<Map<String, Object>> rows =
                jdbc.queryForList(sql.required(statementKey), arguments);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("BAT resource not found");
        }
        return rows.getFirst();
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
}
