package com.cpf.batch.scheduler;

import com.cpf.batch.api.BatchJobDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

/**
 * Published Definition Projection Outbox를 기존 Scheduler Data Plane에 반영합니다.
 *
 * <p>Claim/Lease/Fencing을 사용해 다중 Scheduler에서 동일 Event를 한 인스턴스만 처리합니다.
 * CRON/CALENDAR/BUSINESS_DAY만 시간 기반 Schedule로 투영하며 FILE/MESSAGE/MANUAL/DEPENDENCY는
 * 각 전용 Trigger Consumer가 실행하도록 bat_schedule을 비활성 상태로 유지합니다.</p>
 */
@Component
public class BatchProjectionScheduleSynchronizer {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final SchedulerCoordinator coordinator;

    public BatchProjectionScheduleSynchronizer(
            JdbcTemplate jdbc, ObjectMapper mapper, SchedulerCoordinator coordinator) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.coordinator = Objects.requireNonNull(coordinator, "coordinator");
    }

    @Scheduled(fixedDelayString = "${cpf.batch.scheduler.projection-sync-ms:1000}")
    @Transactional
    public void synchronize() {
        long fencingToken = coordinator.fencingToken();
        if (fencingToken <= 0) return;
        var leader = coordinator.assertLeader(fencingToken);
        Optional<Outbox> claimed = claim(leader.instanceId(), leader.fencingToken());
        claimed.ifPresent(event -> apply(event, leader.instanceId(), leader.fencingToken()));
    }

    private Optional<Outbox> claim(String instanceId, long fencingToken) {
        List<Outbox> candidates = jdbc.query("""
                SELECT outbox_id,job_id,definition_version,event_type,payload_hash,event_payload,attempt_count
                  FROM bat_job_runtime_projection_outbox
                 WHERE delivery_status IN ('PENDING','RETRY')
                   AND (next_attempt_at IS NULL OR next_attempt_at<=CURRENT_TIMESTAMP)
                   AND (lease_until IS NULL OR lease_until<CURRENT_TIMESTAMP)
                 ORDER BY created_at
                """, (rs, row) -> new Outbox(
                        rs.getString("outbox_id"), rs.getString("job_id"),
                        rs.getLong("definition_version"), rs.getString("event_type"),
                        rs.getString("payload_hash"), rs.getString("event_payload"),
                        rs.getInt("attempt_count")));
        for (Outbox event : candidates) {
            int changed = jdbc.update("""
                    UPDATE bat_job_runtime_projection_outbox
                       SET delivery_status='CLAIMED',lease_owner=?,lease_until=?,
                           fencing_token=?,attempt_count=attempt_count+1,last_error_code=NULL
                     WHERE outbox_id=?
                       AND delivery_status IN ('PENDING','RETRY')
                       AND (lease_until IS NULL OR lease_until<CURRENT_TIMESTAMP)
                    """, instanceId, Timestamp.from(Instant.now().plusSeconds(30)),
                    fencingToken, event.outboxId());
            if (changed == 1) return Optional.of(event);
        }
        return Optional.empty();
    }

    private void apply(Outbox event, String instanceId, long fencingToken) {
        coordinator.assertLeader(fencingToken);
        try {
            if ("JOB_DEFINITION_RETIRED".equals(event.eventType())) {
                disableSchedule(event.jobId(), event.definitionVersion(), instanceId);
            } else if ("JOB_DEFINITION_PUBLISHED".equals(event.eventType())) {
                BatchJobDefinition definition = mapper.readValue(
                        event.payload(), BatchJobDefinition.class);
                if (definition.state() != BatchJobDefinition.State.PUBLISHED
                        || definition.definitionVersion() != event.definitionVersion()
                        || !definition.checksum().equalsIgnoreCase(event.payloadHash())) {
                    throw new IllegalStateException("Published Projection Outbox contract mismatch");
                }
                project(definition, instanceId);
            } else {
                throw new IllegalArgumentException("Unsupported Projection event: " + event.eventType());
            }
            int delivered = jdbc.update("""
                    UPDATE bat_job_runtime_projection_outbox
                       SET delivery_status='DELIVERED',delivered_at=CURRENT_TIMESTAMP,
                           lease_owner=NULL,lease_until=NULL,last_error_code=NULL
                     WHERE outbox_id=? AND delivery_status='CLAIMED'
                       AND lease_owner=? AND fencing_token=?
                    """, event.outboxId(), instanceId, fencingToken);
            if (delivered != 1) throw new IllegalStateException("Projection ACK was fenced");
        } catch (Exception failure) {
            jdbc.update("""
                    UPDATE bat_job_runtime_projection_outbox
                       SET delivery_status='RETRY',lease_owner=NULL,lease_until=NULL,
                           next_attempt_at=?,last_error_code=?
                     WHERE outbox_id=? AND lease_owner=? AND fencing_token=?
                    """, Timestamp.from(Instant.now().plusSeconds(backoff(event.attemptCount() + 1))),
                    failure.getClass().getSimpleName(), event.outboxId(), instanceId, fencingToken);
        }
    }

    private void project(BatchJobDefinition definition, String actor) {
        projectJob(definition, actor);
        String scheduleId = scheduleId(definition.jobId());
        boolean timeBased = switch (definition.trigger().type()) {
            case CRON, CALENDAR, BUSINESS_DAY -> true;
            default -> false;
        };
        if (!definition.trigger().enabled() || !timeBased) {
            disableSchedule(definition.jobId(), definition.definitionVersion(), actor);
            return;
        }
        CronExpression cron = CronExpression.parse(definition.trigger().expression());
        ZoneId zone = ZoneId.of(definition.trigger().timezone());
        ZonedDateTime next = cron.next(ZonedDateTime.now(zone));
        if (next == null) throw new IllegalArgumentException("No next fire for Batch Definition");
        String businessDay = definition.trigger().type() == BatchJobDefinition.TriggerType.BUSINESS_DAY
                ? "Y" : "N";
        String holidayPolicy = definition.trigger().misfirePolicy() == BatchJobDefinition.MisfirePolicy.SKIP
                ? "SKIP" : "NEXT_BUSINESS_DAY";
        int existing = Optional.ofNullable(jdbc.queryForObject(
                "SELECT COUNT(*) FROM bat_schedule WHERE schedule_id=?",
                Integer.class, scheduleId)).orElse(0);
        if (existing == 0) {
            try {
                jdbc.update("""
                        INSERT INTO bat_schedule(
                          schedule_id,job_id,definition_version,definition_checksum,cron_expression,calendar_id,
                          business_day_only_yn,holiday_policy,available_start_time,available_end_time,
                          run_date_pattern,timezone,enabled_yn,next_fire_at,created_by,created_at,updated_by,updated_at)
                        VALUES (?,?,?,? ,?,'DEFAULT',?,?,NULL,NULL,'yyyyMMdd',?,'Y',?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
                        """, scheduleId, definition.jobId(), definition.definitionVersion(), definition.checksum(),
                        definition.trigger().expression(), businessDay, holidayPolicy,
                        definition.trigger().timezone(), Timestamp.from(next.toInstant()), actor, actor);
                return;
            } catch (DuplicateKeyException concurrent) {
                // 다른 Scheduler가 먼저 생성한 경우 아래 CAS Update로 수렴합니다.
            }
        }
        jdbc.update("""
                UPDATE bat_schedule
                   SET job_id=?,definition_version=?,definition_checksum=?,cron_expression=?,
                       business_day_only_yn=?,holiday_policy=?,timezone=?,enabled_yn='Y',next_fire_at=?,
                       updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE schedule_id=?
                """, definition.jobId(), definition.definitionVersion(), definition.checksum(),
                definition.trigger().expression(), businessDay, holidayPolicy,
                definition.trigger().timezone(), Timestamp.from(next.toInstant()), actor, scheduleId);
    }

    private void projectJob(BatchJobDefinition definition, String actor) {
        int updated = jdbc.update("""
                UPDATE bat_job
                   SET job_name=?,job_type=?,published_definition_version=?,published_definition_checksum=?,
                       executor_reference=?,definition_published_at=CURRENT_TIMESTAMP,description=?,
                       restartable_yn=?,use_yn='Y',updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE job_id=?
                """, definition.jobName(), definition.executorType().name(),
                definition.definitionVersion(), definition.checksum(), definition.executorReference(),
                definition.description(), definition.recoveryPolicy().restartable() ? "Y" : "N",
                actor, definition.jobId());
        if (updated == 0) {
            try {
                jdbc.update("""
                        INSERT INTO bat_job
                        (job_id,job_name,job_type,published_definition_version,published_definition_checksum,
                         executor_reference,definition_published_at,description,restartable_yn,use_yn,
                         created_by,created_at,updated_by,updated_at)
                        VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP,?,?,'Y',?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
                        """, definition.jobId(), definition.jobName(), definition.executorType().name(),
                        definition.definitionVersion(), definition.checksum(), definition.executorReference(),
                        definition.description(), definition.recoveryPolicy().restartable() ? "Y" : "N",
                        actor, actor);
            } catch (DuplicateKeyException concurrent) {
                int converged = jdbc.update("""
                        UPDATE bat_job
                           SET job_name=?,job_type=?,published_definition_version=?,published_definition_checksum=?,
                               executor_reference=?,definition_published_at=CURRENT_TIMESTAMP,description=?,
                               restartable_yn=?,use_yn='Y',updated_by=?,updated_at=CURRENT_TIMESTAMP
                         WHERE job_id=?
                        """, definition.jobName(), definition.executorType().name(),
                        definition.definitionVersion(), definition.checksum(), definition.executorReference(),
                        definition.description(), definition.recoveryPolicy().restartable() ? "Y" : "N",
                        actor, definition.jobId());
                if (converged != 1) {
                    throw new IllegalStateException("Published Job Projection conflict: "
                            + definition.jobId(), concurrent);
                }
            }
        }
    }

    private void disableSchedule(String jobId, long definitionVersion, String actor) {
        jdbc.update("""
                UPDATE bat_schedule SET enabled_yn='N',updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE schedule_id=? AND (definition_version=? OR definition_version IS NULL)
                """, actor, scheduleId(jobId), definitionVersion);
        jdbc.update("""
                UPDATE bat_job SET use_yn='N',updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE job_id=? AND published_definition_version=?
                """, actor, jobId, definitionVersion);
    }

    private static String scheduleId(String jobId) {
        String value = "DEF_" + jobId;
        if (value.length() > 100) {
            throw new IllegalArgumentException("Generated scheduleId exceeds 100 characters");
        }
        return value;
    }

    private static long backoff(int attempt) {
        return Math.min(300L, 1L << Math.min(8, Math.max(0, attempt - 1)));
    }

    private record Outbox(
            String outboxId,
            String jobId,
            long definitionVersion,
            String eventType,
            String payloadHash,
            String payload,
            int attemptCount) {}
}
