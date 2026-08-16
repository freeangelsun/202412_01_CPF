package com.cpf.education.batch.support.controller;
import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.education.batch.support.EducationAdmBatchLogQueryEducationSample;
import com.cpf.education.batch.support.EducationBatchPolicyEducationSample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * BAT Owner의 공개 Operations Port를 사용하는 배치 EDU API입니다.
 * EDU가 Core 내부 Batch Runtime이나 CPF Platform DB를 직접 소유하지 않습니다.
 */
@RestController
@RequestMapping({"/api/education/batch", "/education/edu/batch"})
@Tag(name = "EDU Education 13. Batch", description = "CPF BAT Owner 계약 기반 배치 개발/운영 예제")
public class EducationBatchEducationController extends com.cpf.education.base.EducationBaseController {
    private final CpfBatchOperationsPort batchOperations;
    private final EducationBatchPolicyEducationSample policyEducation;
    private final EducationAdmBatchLogQueryEducationSample admLogEducation;

    /** EducationBatchEducationController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationBatchEducationController(
            ObjectProvider<CpfBatchOperationsPort> provider,
            EducationBatchPolicyEducationSample policyEducation,
            EducationAdmBatchLogQueryEducationSample admLogEducation) {
        this.batchOperations = provider.getIfAvailable();
        this.policyEducation = policyEducation;
        this.admLogEducation = admLogEducation;
    }

    @PostMapping("/tasklet/run")
    @CpfOnlineTransaction(id = "OEDUAA0047", name = "EDUBatchTaskletRun", ownerDomain = "EDU")
    @Operation(operationId = "refBatchEducationRunTasklet", summary = "Tasklet Job 실행 요청", description = "동일 JVM 또는 Remote Adapter로 연결된 BAT Owner에 실행을 요청합니다.")
    /** runTasklet 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> runTasklet(@RequestParam(defaultValue = "EDU_EDU") String requestUser) {
        return ResponseEntity.ok(requestRun("CPF_EDU_TASKLET_JOB", requestUser, "Tasklet Job 교육 실행"));
    }

    @PostMapping("/chunk/run")
    @CpfOnlineTransaction(id = "OEDUAA0048", name = "EDUBatchChunkRun", ownerDomain = "EDU")
    @Operation(operationId = "refBatchEducationRunChunk", summary = "Chunk Job 실행 요청")
    /** runChunk 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> runChunk(@RequestParam(defaultValue = "EDU_EDU") String requestUser) {
        return ResponseEntity.ok(requestRun("CPF_EDU_CHUNK_JOB", requestUser, "Chunk Job 교육 실행"));
    }

    @PostMapping("/retry/run")
    @CpfOnlineTransaction(id = "OEDUAA0049", name = "EDUBatchRetryRun", ownerDomain = "EDU")
    @Operation(operationId = "refBatchEducationRunRetry", summary = "재처리 Job 실행 요청")
    /** runRetry 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> runRetry(@RequestParam(defaultValue = "EDU_EDU") String requestUser) {
        return ResponseEntity.ok(requestRun("CPF_EDU_RETRY_JOB", requestUser, "Retry Job 교육 실행"));
    }

    @GetMapping("/retry-policy")
    @CpfOnlineTransaction(id = "OEDUAA0050", name = "EDUBatchRetryPolicy", ownerDomain = "EDU")
    @Operation(
            operationId = "refBatchEducationRetryPolicy",
            summary = "Batch 재시도·결과불명 정책",
            description = "skip/retry/unknown-result를 구분하고 멱등성·재확인 원칙을 설명합니다.")
    /** retryPolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> retryPolicy(){ return ResponseEntity.ok(Map.of(
            "skip", "데이터 오류는 실패 데이터와 사유를 남기고 skip 여부를 정책으로 관리합니다.",
            "retry", "일시 오류만 제한 횟수/backoff로 재시도하고 멱등성을 먼저 보장합니다.",
            "unknown", "결과불명은 성공/실패로 추정하지 않고 재확인/복구 대상으로 유지합니다.")); }

    @GetMapping("/lock-policy")
    @CpfOnlineTransaction(id = "OEDUAA0058", name = "EDUBatchLockPolicy", ownerDomain = "EDU")
    @Operation(
            operationId = "refBatchEducationLockPolicy",
            summary = "Batch lease·fencing 정책",
            description = "EDU가 BAT lock 구현을 복제하지 않고 BAT Owner의 lease, fencing 및 승인된 강제 해제 원칙을 설명합니다.")
    /** lockPolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<EducationBatchPolicyEducationSample.LockPolicy> lockPolicy() {
        return ResponseEntity.ok(policyEducation.lockPolicy());
    }

    @GetMapping("/checkpoint-restart")
    @CpfOnlineTransaction(id = "OEDUAA0052", name = "EDUBatchCheckpointRestart", ownerDomain = "EDU")
    @Operation(
            operationId = "refBatchEducationCheckpointRestart",
            summary = "Checkpoint restart·rerun 구분",
            description = "동일 JobInstance restart와 새 JobInstance rerun, UNKNOWN_RESULT 대사 순서를 설명합니다.")
    /** checkpointRestart 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<EducationBatchPolicyEducationSample.RestartPolicy> checkpointRestart() {
        return ResponseEntity.ok(policyEducation.restartPolicy());
    }

    @GetMapping("/adm-link")
    @CpfOnlineTransaction(id = "OEDUAA0053", name = "EDUBatchAdmLogLink", ownerDomain = "EDU")
    @Operation(
            operationId = "refBatchEducationAdmLink",
            summary = "ADM Batch JobInstance 로그 조회 링크",
            description = "공개 CPF 로그 경로 계약으로 입력을 검증하고 ADM 관제 API의 목록·상세 URL을 구성합니다.")
    /** admLink 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, String>> admLink(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate,
            @RequestParam(defaultValue = "CPF_EDU_TASKLET_JOB") String jobName,
            @RequestParam(defaultValue = "1") long jobInstanceId,
            @RequestParam String serverInstanceId) {
        LocalDate effectiveDate = businessDate == null ? LocalDate.now() : businessDate;
        return ResponseEntity.ok(admLogEducation.queryUrls(
                effectiveDate,
                jobName,
                jobInstanceId,
                serverInstanceId));
    }

    @GetMapping("/ownership")
    @CpfOnlineTransaction(id = "OEDUAA0054", name = "EDUBatchOwnership", ownerDomain = "EDU")
    @Operation(
            operationId = "refBatchEducationOwnership",
            summary = "BAT Runtime Ownership",
            description = "Job/Step 업무 로직과 BAT Runtime/Operations Port의 소유권 및 local/remote topology 사용 기준을 설명합니다.")
    /** ownership 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> ownership(){ return ResponseEntity.ok(Map.of(
            "owner", "cpf-batch",
            "domain", "업무 Domain은 Job/Step 또는 Handler 같은 업무 로직만 제공합니다.",
            "operations", List.of("requestRun","requestRetry","requestStop","schedule","worker/instance 조회"),
            "topology", "동일 JVM은 local port, 분리 WAS는 remote adapter를 사용하고 Gateway를 재경유하지 않습니다.")); }

    @GetMapping("/schedule-policy")
    @CpfOnlineTransaction(id = "OEDUAA0055", name = "EDUBatchSchedulePolicy", ownerDomain = "EDU")
    @Operation(
            operationId = "refBatchEducationSchedulePolicy",
            summary = "Scheduler leader·중복 방지 정책",
            description = "독립 Scheduler Runtime의 leader fencing, trigger 멱등성 및 CMN Calendar 경계를 설명합니다.")
    /** schedulePolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<EducationBatchPolicyEducationSample.SchedulePolicy> schedulePolicy() {
        return ResponseEntity.ok(policyEducation.schedulePolicy());
    }

    @GetMapping("/lifecycle-policy")
    @CpfOnlineTransaction(id = "OEDUAA0056", name = "EDUBatchLifecyclePolicy", ownerDomain = "EDU")
    @Operation(
            operationId = "refBatchEducationLifecyclePolicy",
            summary = "Chunk transaction·중복·결과불명·대사 정책",
            description = "과거 BAT EDU의 transaction/idempotency/reconciliation 개념을 EDU에서 BAT Runtime 복제 없이 설명합니다.")
    /** lifecyclePolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<EducationBatchPolicyEducationSample.LifecyclePolicy> lifecyclePolicy() {
        return ResponseEntity.ok(policyEducation.lifecyclePolicy());
    }

    private Map<String,Object> requestRun(String jobId,String user,String reason){
        if(batchOperations==null){
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "CpfBatchOperationsPort adapter가 없습니다. 동일 JVM local adapter 또는 BAT remote adapter를 구성하세요.");
        }
        return batchOperations.requestRun(jobId, "{}", user, reason);
    }
}
