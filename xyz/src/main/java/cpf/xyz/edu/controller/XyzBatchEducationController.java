package cpf.xyz.edu.controller;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    public XyzBatchEducationController(ObjectProvider<JobLauncher> jobLauncherProvider,
                                       ObjectProvider<Map<String, Job>> jobsProvider) {
        this.jobLauncher = jobLauncherProvider.getIfAvailable();
        this.jobs = jobsProvider.getIfAvailable(Map::of);
    }

    @PostMapping("/tasklet/run")
    @FpsTransaction(id = "XYZ13EDU0001", name = "XYZBatchTaskletRun")
    @Operation(summary = "Tasklet Job 실행 샘플", description = "단건 처리나 파일 정리처럼 한 번에 끝나는 배치 유형을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runTasklet(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_TASKLET_JOB", requestUser));
    }

    @PostMapping("/chunk/run")
    @FpsTransaction(id = "XYZ13EDU0002", name = "XYZBatchChunkRun")
    @Operation(summary = "Chunk Job 실행 샘플", description = "대용량 데이터를 읽기/처리/쓰기 단위로 나누어 처리하는 배치 유형을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runChunk(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_CHUNK_JOB", requestUser));
    }

    @PostMapping("/retry/run")
    @FpsTransaction(id = "XYZ13EDU0003", name = "XYZBatchRetryRun")
    @Operation(summary = "실패 재처리 Job 실행 샘플", description = "실패 데이터 적재와 재수행 정책을 설명하는 배치 유형을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runRetry(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_RETRY_JOB", requestUser));
    }

    @GetMapping("/retry-policy")
    @FpsTransaction(id = "XYZ13EDU0004", name = "XYZBatchRetryPolicy")
    @Operation(summary = "skip/retry 정책 설명", description = "배치 실패 재처리와 skip/retry 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> retryPolicy() {
        return ResponseEntity.ok(Map.of(
                "when", "외부 API 일시 오류, 잠금 경합, 네트워크 순간 장애처럼 재시도 가치가 있는 오류에 사용합니다.",
                "skip", "데이터 자체가 잘못된 건은 실패 데이터 테이블에 적재하고 skip 대상과 사유를 남깁니다.",
                "retry", "동일 파라미터 재시도 횟수와 backoff를 제한하고 멱등성을 먼저 보장합니다.",
                "adm", "ADM 배치 상세 화면에서는 실패 사유, 재수행 가능 여부, 실패 데이터 위치를 보여줘야 합니다."));
    }

    @GetMapping("/lock-policy")
    @FpsTransaction(id = "XYZ13EDU0005", name = "XYZBatchLockPolicy")
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
    @FpsTransaction(id = "XYZ13EDU0006", name = "XYZBatchCheckpointRestart")
    @Operation(summary = "checkpoint/restart 설명", description = "대용량 배치의 재시작 기준과 checkpoint 저장 원칙을 설명합니다.")
    public ResponseEntity<Map<String, Object>> checkpointRestart() {
        return ResponseEntity.ok(Map.of(
                "checkpoint", "마지막 정상 처리 key, 파일 offset, page 조건을 JobExecutionContext 또는 업무 재처리 테이블에 저장합니다.",
                "restart", "재시작 시 이미 처리한 데이터는 건너뛰고 처리 결과 테이블의 unique key로 멱등성을 보장합니다.",
                "failureData", "처리 불가 데이터는 원문, 오류코드, 오류메시지, 재처리 가능 여부를 별도 테이블에 남깁니다."));
    }

    @GetMapping("/adm-link")
    @FpsTransaction(id = "XYZ13EDU0007", name = "XYZBatchAdmLink")
    @Operation(summary = "ADM 배치 관제 연동 설명", description = "EDU 배치가 ADM 배치 관제와 연결되는 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> admLink() {
        return ResponseEntity.ok(Map.of(
                "metadata", List.of("pfw_batch_job", "pfw_batch_schedule", "pfw_batch_execution", "pfw_batch_step_execution", "pfw_batch_operation_log"),
                "buttons", List.of("조회", "등록", "수동 실행", "실패 재수행", "중지", "스케줄 활성화", "스케줄 비활성화"),
                "audit", "실행, 재수행, 중지, 스케줄 변경은 감사 사유와 before/after diff를 남깁니다.",
                "swagger", "ADM-Batch와 XYZ-EDU Batch tag를 함께 확인하면 개발/운영 양쪽 예제를 볼 수 있습니다."));
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
}
