package cpf.xyz.edu.controller;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Batch 개발 표준을 학습하는 EDU API입니다.
 *
 * <p>각 API는 신규 개발자가 어떤 배치 유형을 언제 선택해야 하는지 이해할 수 있도록 실행 예시와 운영 연결 기준을 함께 반환합니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu/batch")
@Tag(name = "XYZ-EDU 13. Batch", description = "Spring Batch 유형별 개발 샘플")
public class XyzBatchEducationController {
    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;
    private final JdbcTemplate pfwJdbcTemplate;

    public XyzBatchEducationController(ObjectProvider<JobLauncher> jobLauncherProvider,
                                       ObjectProvider<Map<String, Job>> jobsProvider,
                                       @Qualifier("pfwDataSource") ObjectProvider<DataSource> pfwDataSourceProvider) {
        this.jobLauncher = jobLauncherProvider.getIfAvailable();
        this.jobs = jobsProvider.getIfAvailable(Map::of);
        DataSource pfwDataSource = pfwDataSourceProvider.getIfAvailable();
        this.pfwJdbcTemplate = pfwDataSource == null ? null : new JdbcTemplate(pfwDataSource);
    }

    @PostMapping("/tasklet/run")
    @CpfTransaction(id = "XYZ13EDU0001", name = "XYZBatchTaskletRun")
    @Operation(summary = "Tasklet Job 실행 샘플", description = "단건 처리나 파일 정리처럼 한 번에 끝나는 배치 유형을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runTasklet(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_TASKLET_JOB", requestUser));
    }

    @PostMapping("/chunk/run")
    @CpfTransaction(id = "XYZ13EDU0002", name = "XYZBatchChunkRun")
    @Operation(summary = "Chunk Job 실행 샘플", description = "대용량 데이터를 읽기/처리/쓰기 단위로 나누어 처리하는 배치 유형을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runChunk(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_CHUNK_JOB", requestUser));
    }

    @PostMapping("/retry/run")
    @CpfTransaction(id = "XYZ13EDU0003", name = "XYZBatchRetryRun")
    @Operation(summary = "실패 재처리 Job 실행 샘플", description = "실패 데이터 적재와 재수행 정책을 설명하는 배치 유형을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runRetry(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_RETRY_JOB", requestUser));
    }

    @GetMapping("/retry-policy")
    @CpfTransaction(id = "XYZ13EDU0004", name = "XYZBatchRetryPolicy")
    @Operation(summary = "skip/retry 정책 설명", description = "배치 실패 재처리와 skip/retry 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> retryPolicy() {
        return ResponseEntity.ok(Map.of(
                "when", "외부 API 일시 오류, 잠금 경합, 네트워크 순간 장애처럼 재시도 가치가 있는 오류에 사용합니다.",
                "skip", "데이터 자체가 잘못된 건은 실패 데이터 테이블에 적재하고 skip 대상과 사유를 남깁니다.",
                "retry", "동일 파라미터 재시도 횟수와 backoff를 제한하고 멱등성을 먼저 보장합니다.",
                "adm", "ADM 배치 상세 화면에서는 실패 사유, 재수행 가능 여부, 실패 데이터 위치를 보여줘야 합니다."));
    }

    @GetMapping("/lock-policy")
    @CpfTransaction(id = "XYZ13EDU0005", name = "XYZBatchLockPolicy")
    @Operation(summary = "중복 실행 방지 lock 설명", description = "동일 job/parameter 중복 실행을 막는 운영 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> lockPolicy() {
        return ResponseEntity.ok(Map.of(
                "lockTable", "pfw_batch_lock",
                "key", "job_id + job_parameters_hash",
                "owner", "batch_instance_id 또는 WAS ID",
                "expire", "비정상 종료에 대비해 lock 만료 시간을 반드시 둡니다.",
                "guide", "동일 파라미터 재수행은 이전 실행 상태가 FAILED, STOPPED, ABANDONED일 때만 허용합니다."));
    }

    @GetMapping("/checkpoint-restart")
    @CpfTransaction(id = "XYZ13EDU0006", name = "XYZBatchCheckpointRestart")
    @Operation(summary = "checkpoint/restart 설명", description = "대용량 배치의 재시작 기준과 checkpoint 저장 원칙을 설명합니다.")
    public ResponseEntity<Map<String, Object>> checkpointRestart() {
        return ResponseEntity.ok(Map.of(
                "checkpoint", "마지막 정상 처리 key, 파일 offset, page 조건을 JobExecutionContext 또는 업무 재처리 테이블에 저장합니다.",
                "restart", "재시작 시 이미 처리한 데이터는 건너뛰고 처리 결과 테이블의 unique key로 멱등성을 보장합니다.",
                "failureData", "처리 불가 데이터는 원문, 오류코드, 오류메시지, 재처리 가능 여부를 별도 테이블에 남깁니다."));
    }

    @GetMapping("/adm-link")
    @CpfTransaction(id = "XYZ13EDU0007", name = "XYZBatchAdmLink")
    @Operation(summary = "ADM 배치 관제 연동 설명", description = "EDU 배치가 ADM 배치 관제와 연결되는 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> admLink() {
        return ResponseEntity.ok(Map.of(
                "metadata", List.of("BATCH_*", "pfw_batch_job", "pfw_batch_schedule", "pfw_batch_job_relation", "pfw_batch_execution_target", "pfw_batch_execution", "pfw_batch_step_execution", "pfw_batch_operation_log"),
                "buttons", List.of("조회", "등록", "수동 실행", "실패 재수행", "중지", "스케줄 활성화", "스케줄 비활성화", "수행 시뮬레이션", "관계 조회", "수행 대상 조회"),
                "audit", "실행, 재수행, 중지, 스케줄 변경은 감사 사유와 before/after diff를 남깁니다.",
                "swagger", "ADM-Batch와 XYZ-EDU Batch tag를 함께 확인하면 개발/운영 양쪽 예제를 볼 수 있습니다."));
    }

    @GetMapping("/schedule-policy")
    @CpfTransaction(id = "XYZ13EDU0008", name = "XYZBatchSchedulePolicy")
    @Operation(summary = "배치 스케줄 정책 설명", description = "영업일 전용 수행, 수행 가능 시간, 선행/트리거 관계, 수행 대상 인스턴스 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> schedulePolicy() {
        return ResponseEntity.ok(Map.of(
                "businessDayOnly", "pfw_batch_schedule.business_day_only_yn='Y'이면 pfw_business_day_calendar 기준 영업일만 수행 후보가 됩니다.",
                "availableTime", "available_start_time과 available_end_time은 운영자가 허용 시간대를 확인하고 시뮬레이션할 때 사용합니다.",
                "simulation", "GET /adm/api/batch/schedules/{scheduleId}/simulation은 기준일과 조회 일수로 수행 가능 후보일을 반환합니다.",
                "relation", "pfw_batch_job_relation은 PREDECESSOR, SUCCESSOR, TRIGGER 관계와 필요 상태를 관리합니다.",
                "target", "pfw_batch_execution_target은 수행 대기/배정/완료 대상 인스턴스와 업무 기준일을 관리합니다.",
                "notification", "pfw_notification_rule과 pfw_notification_delivery_log는 배치 실패나 보안 이벤트 알림 기준을 관리합니다."));
    }

    private Map<String, Object> runJob(String jobId, String requestUser) {
        Map<String, Object> response = new LinkedHashMap<>();
        Job job = resolveJob(jobId);
        if (jobLauncher == null || job == null) {
            response.put("enabled", false);
            response.put("jobId", jobId);
            response.put("message", "Spring Batch 인프라 또는 Job bean이 없어 실행하지 않았습니다.");
            response.put("guide", "spring.datasource, spring.batch.jdbc 설정과 JobRepository 구성을 확인하세요.");
            return response;
        }
        try {
            JobParameters parameters = new JobParametersBuilder()
                    // 같은 Job을 반복 실행할 수 있도록 요청 시각을 파라미터에 포함합니다.
                    .addLong("requestTime", System.currentTimeMillis())
                    .addString("requestUser", TextUtils.defaultIfBlank(requestUser, "XYZ_EDU"))
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(job, parameters);
            response.put("enabled", true);
            response.put("jobId", jobId);
            response.put("executionId", execution.getId());
            response.put("status", execution.getStatus().name());
            response.put("exitStatus", execution.getExitStatus().getExitCode());
            response.put("pfwBatchExecution", recordPfwBatchExecution(jobId, requestUser, execution));
            response.put("guide", "실무에서는 이 실행 결과를 pfw_batch_execution과 ADM 배치 관제 화면에서 함께 추적합니다.");
            return response;
        } catch (Exception ex) {
            response.put("enabled", true);
            response.put("jobId", jobId);
            response.put("status", "FAILED");
            response.put("message", ex.getMessage());
            response.put("guide", "실패 데이터 적재, 재수행 가능 여부, 운영자 감사 사유를 함께 설계해야 합니다.");
            return response;
        }
    }

    private Job resolveJob(String jobId) {
        Job direct = jobs.get(jobId);
        if (direct != null) {
            return direct;
        }
        return jobs.values().stream()
                .filter(job -> jobId.equals(job.getName()))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> recordPfwBatchExecution(String jobId, String requestUser, JobExecution execution) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (pfwJdbcTemplate == null) {
            result.put("linked", false);
            result.put("reason", "PFW datasource가 없어 CPF 운영 메타 기록을 생략했습니다.");
            return result;
        }

        String user = TextUtils.defaultIfBlank(requestUser, "XYZ_EDU");
        try {
            long readCount = sumStepCounts(execution, StepCountType.READ);
            long writeCount = sumStepCounts(execution, StepCountType.WRITE);
            long skipCount = sumStepCounts(execution, StepCountType.SKIP);
            pfwJdbcTemplate.update("""
                    INSERT INTO pfw_batch_execution (
                        job_id, schedule_id, job_parameters, execution_status, spring_batch_execution_id,
                        batch_instance_id, start_time, end_time, read_count, write_count, skip_count,
                        error_message, requested_by, created_by, updated_by
                    ) VALUES (?, NULL, ?, ?, ?, 'local-batch-01', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    jobId,
                    execution.getJobParameters().toString(),
                    execution.getStatus().name(),
                    execution.getId(),
                    toTimestamp(execution.getStartTime()),
                    toTimestamp(execution.getEndTime()),
                    readCount,
                    writeCount,
                    skipCount,
                    execution.getFailureExceptions().isEmpty() ? null : execution.getAllFailureExceptions().toString(),
                    user,
                    user,
                    user);
            Long pfwExecutionId = pfwJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            if (pfwExecutionId != null) {
                recordPfwStepExecutions(pfwExecutionId, execution, user);
            }
            result.put("linked", true);
            result.put("pfwExecutionId", pfwExecutionId);
            result.put("springBatchExecutionId", execution.getId());
            result.put("readCount", readCount);
            result.put("writeCount", writeCount);
            result.put("skipCount", skipCount);
            return result;
        } catch (DataAccessException ex) {
            result.put("linked", false);
            result.put("reason", ex.getMostSpecificCause().getMessage());
            return result;
        }
    }

    private void recordPfwStepExecutions(Long pfwExecutionId, JobExecution execution, String user) {
        for (StepExecution step : execution.getStepExecutions()) {
            pfwJdbcTemplate.update("""
                    INSERT INTO pfw_batch_step_execution (
                        execution_id, step_name, execution_status, start_time, end_time,
                        read_count, write_count, skip_count, error_message, step_log, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    pfwExecutionId,
                    step.getStepName(),
                    step.getStatus().name(),
                    toTimestamp(step.getStartTime()),
                    toTimestamp(step.getEndTime()),
                    step.getReadCount(),
                    step.getWriteCount(),
                    step.getSkipCount(),
                    step.getFailureExceptions().isEmpty() ? null : step.getFailureExceptions().toString(),
                    "commit=" + step.getCommitCount()
                            + ", rollback=" + step.getRollbackCount()
                            + ", readSkip=" + step.getReadSkipCount()
                            + ", processSkip=" + step.getProcessSkipCount()
                            + ", writeSkip=" + step.getWriteSkipCount(),
                    user,
                    user);
        }
    }

    private long sumStepCounts(JobExecution execution, StepCountType type) {
        return execution.getStepExecutions().stream()
                .mapToLong(step -> switch (type) {
                    case READ -> step.getReadCount();
                    case WRITE -> step.getWriteCount();
                    case SKIP -> step.getSkipCount();
                })
                .sum();
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private enum StepCountType {
        READ,
        WRITE,
        SKIP
    }
}
