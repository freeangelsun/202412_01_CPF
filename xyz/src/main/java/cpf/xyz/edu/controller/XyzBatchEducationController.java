package cpf.xyz.edu.controller;

import cpf.pfw.common.batch.CpfBatchExecutionRequest;
import cpf.pfw.common.batch.CpfBatchExecutionResult;
import cpf.pfw.common.batch.CpfBatchLauncher;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * <p>이 컨트롤러의 핵심은 개발자가 Spring Batch의 Job/Step 작성법은 배우되,
 * 실행, 중복 실행 방지, 이력 기록, ADM 관제 연동은 반드시 PFW 공통 {@link CpfBatchLauncher}를
 * 통과해야 한다는 기준을 익히게 하는 것입니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu/batch")
@Tag(name = "XYZ-EDU 13. Batch", description = "PFW 공통 배치 API 기반 Spring Batch 유형별 개발 샘플")
public class XyzBatchEducationController {
    private final CpfBatchLauncher batchLauncher;

    public XyzBatchEducationController(CpfBatchLauncher batchLauncher) {
        this.batchLauncher = batchLauncher;
    }

    @PostMapping("/tasklet/run")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0001", name = "XYZBatchTaskletRun")
    @Operation(operationId = "xyzBatchEducationRunTasklet", summary = "Tasklet Job 실행 샘플", description = "PFW 공통 배치 API를 통해 단건 처리 Job을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runTasklet(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_TASKLET_JOB", requestUser, "Tasklet Job 교육 실행"));
    }

    @PostMapping("/chunk/run")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0002", name = "XYZBatchChunkRun")
    @Operation(operationId = "xyzBatchEducationRunChunk", summary = "Chunk Job 실행 샘플", description = "PFW 공통 배치 API를 통해 대용량 chunk 처리 Job을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runChunk(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_CHUNK_JOB", requestUser, "Chunk Job 교육 실행"));
    }

    @PostMapping("/retry/run")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0003", name = "XYZBatchRetryRun")
    @Operation(operationId = "xyzBatchEducationRunRetry", summary = "실패 재처리 Job 실행 샘플", description = "PFW 공통 배치 API를 통해 실패 재처리 Job을 실행합니다.")
    public ResponseEntity<Map<String, Object>> runRetry(@RequestParam(defaultValue = "XYZ_EDU") String requestUser) {
        return ResponseEntity.ok(runJob("CPF_EDU_RETRY_JOB", requestUser, "Retry Job 교육 실행"));
    }

    @GetMapping("/retry-policy")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0004", name = "XYZBatchRetryPolicy")
    @Operation(operationId = "xyzBatchEducationRetryPolicy", summary = "skip/retry 정책 설명", description = "배치 실패 재처리와 skip/retry 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> retryPolicy() {
        return ResponseEntity.ok(Map.of(
                "when", "외부 API 일시 오류, 잠금 경합, 네트워크 순간 장애처럼 재시도 가치가 있는 오류에 사용합니다.",
                "skip", "데이터 자체가 잘못된 건은 실패 데이터 테이블에 적재하고 skip 대상과 사유를 남깁니다.",
                "retry", "동일 파라미터 재시도 횟수와 backoff를 제한하고 멱등성을 먼저 보장합니다.",
                "adm", "ADM 배치 상세 화면에서는 실패 사유, 재수행 가능 여부, 실패 데이터 위치를 보여줘야 합니다."));
    }

    @GetMapping("/lock-policy")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0005", name = "XYZBatchLockPolicy")
    @Operation(operationId = "xyzBatchEducationLockPolicy", summary = "중복 실행 방지 lock 설명", description = "동일 job/parameter 중복 실행을 막는 운영 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> lockPolicy() {
        return ResponseEntity.ok(Map.of(
                "lockTable", "pfw_batch_lock",
                "key", "job_id + job_parameters_hash",
                "owner", "serverInstanceId 또는 batch_instance_id",
                "expire", "비정상 종료에 대비해 lock 만료 시간을 반드시 둡니다.",
                "guide", "신규 업무 배치는 JobLauncher를 직접 호출하지 말고 CpfBatchLauncher를 통해 실행해야 합니다."));
    }

    @GetMapping("/checkpoint-restart")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0006", name = "XYZBatchCheckpointRestart")
    @Operation(operationId = "xyzBatchEducationCheckpointRestart", summary = "checkpoint/restart 설명", description = "대용량 배치의 재시작 기준과 checkpoint 저장 원칙을 설명합니다.")
    public ResponseEntity<Map<String, Object>> checkpointRestart() {
        return ResponseEntity.ok(Map.of(
                "checkpoint", "마지막 정상 처리 key, 파일 offset, page 조건을 JobExecutionContext 또는 업무 재처리 테이블에 저장합니다.",
                "restart", "재시작 시 이미 처리한 데이터는 건너뛰고 처리 결과 테이블의 unique key로 멱등성을 보장합니다.",
                "failureData", "처리 불가 데이터는 원문, 오류코드, 오류메시지, 재처리 가능 여부를 별도 테이블에 남깁니다."));
    }

    @GetMapping("/adm-link")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0007", name = "XYZBatchAdmLink")
    @Operation(operationId = "xyzBatchEducationAdmLink", summary = "ADM 배치 관제 연동 설명", description = "EDU 배치가 ADM 배치 관제와 연결되는 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> admLink() {
        return ResponseEntity.ok(Map.of(
                "metadata", List.of("BATCH_*", "pfw_batch_job", "pfw_batch_schedule", "pfw_batch_job_relation", "pfw_batch_execution_target", "pfw_batch_execution", "pfw_batch_step_execution", "pfw_batch_operation_log"),
                "buttons", List.of("조회", "등록", "수동 실행", "실패 재수행", "중지", "스케줄 활성화", "스케줄 비활성화", "수행 시뮬레이션", "관계 조회", "수행 대상 조회"),
                "audit", "실행, 재수행, 중지, 스케줄 변경은 감사 사유와 before/after diff를 남깁니다.",
                "facade", "업무 배치는 CpfBatchLauncher 실행 결과의 pfwExecutionId와 Spring Batch executionId를 함께 추적합니다."));
    }

    @GetMapping("/schedule-policy")
    @CpfOnlineTransaction(id = "OXYZ-EDU-13-0008", name = "XYZBatchSchedulePolicy")
    @Operation(operationId = "xyzBatchEducationSchedulePolicy", summary = "배치 스케줄 정책 설명", description = "영업일 전용 수행, 수행 가능 시간, 선행/트리거 관계, 수행 대상 인스턴스 기준을 설명합니다.")
    public ResponseEntity<Map<String, Object>> schedulePolicy() {
        return ResponseEntity.ok(Map.of(
                "businessDayOnly", "pfw_batch_schedule.business_day_only_yn='Y'이면 pfw_business_day_calendar 기준 영업일만 수행 후보가 됩니다.",
                "availableTime", "available_start_time과 available_end_time은 운영자가 허용 시간대를 확인하고 시뮬레이션할 때 사용합니다.",
                "simulation", "GET /adm/api/batch/schedules/{scheduleId}/simulation은 기준일과 조회 일수로 수행 가능 후보일을 반환합니다.",
                "relation", "pfw_batch_job_relation은 PREDECESSOR, SUCCESSOR, TRIGGER 관계와 필요 상태를 관리합니다.",
                "target", "pfw_batch_execution_target은 수행 대기/배정/완료 대상 인스턴스와 업무 기준일을 관리합니다.",
                "notification", "pfw_notification_rule과 pfw_notification_delivery_log는 배치 실패나 보안 이벤트 알림 기준을 관리합니다."));
    }

    private Map<String, Object> runJob(String jobId, String requestUser, String reason) {
        Map<String, Object> response = new LinkedHashMap<>();

        // 1. 업무 개발자는 Spring Batch Job bean만 등록합니다.
        //    실행 요청은 직접 JobLauncher를 호출하지 않고 PFW Facade에 맡깁니다.
        CpfBatchExecutionRequest request = CpfBatchExecutionRequest.run(
                jobId,
                "{\"edu\":true,\"jobId\":\"" + jobId + "\"}",
                requestUser,
                reason);

        // 2. PFW Facade는 transactionGlobalId, 중복 실행 lock, pfw_batch_execution 기록,
        //    Spring Batch executionId 연결, fallback 이벤트 발행을 한 번에 처리합니다.
        CpfBatchExecutionResult result = batchLauncher.run(request);

        // 3. EDU 응답은 학습자가 ADM 배치 관제에서 어떤 값을 따라가야 하는지 함께 보여줍니다.
        response.put("enabled", result.executed());
        response.put("jobId", result.jobId());
        response.put("pfwExecutionId", result.pfwExecutionId());
        response.put("springBatchExecutionId", result.springBatchExecutionId());
        response.put("status", result.status());
        response.put("message", result.message());
        response.put("detail", result.detail());
        response.put("guide", "ADM 배치 관제에서는 pfwExecutionId로 CPF 운영 메타를, springBatchExecutionId로 Spring Batch 표준 메타를 함께 조회합니다.");
        return response;
    }
}
