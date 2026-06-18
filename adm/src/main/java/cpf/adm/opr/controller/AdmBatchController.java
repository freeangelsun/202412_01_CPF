package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmBatchJobRegisterRequest;
import cpf.adm.opr.dto.AdmBatchOperationRequest;
import cpf.adm.opr.dto.AdmBusinessDayRequest;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmBatchOperationService;
import cpf.pfw.common.logging.FpsTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ADM 배치 관제와 운영 API입니다.
 *
 * <p>운영자는 이 API를 통해 배치 Job, 스케줄, 실행 이력, 인스턴스, 영업일 캘린더를 조회하고
 * 수동 실행, 실패 재수행, 중지, 스케줄 활성/비활성 같은 운영 행위를 수행합니다.</p>
 */
@RestController
@RequestMapping("/adm/api/batch")
@Tag(name = "ADM-Batch", description = "CPF 배치 관제와 운영 API")
public class AdmBatchController {
    private final AdmBatchOperationService batchOperationService;
    private final AdmAuditLogService auditLogService;

    public AdmBatchController(AdmBatchOperationService batchOperationService, AdmAuditLogService auditLogService) {
        this.batchOperationService = batchOperationService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/jobs")
    @FpsTransaction(id = "ADM01BAT0010", name = "ADMBatchJobList")
    @Operation(summary = "배치 Job 목록 조회", description = "배치 Job, 마지막 실행 시간, 성공/실패 건수, 평균 수행 시간을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findJobs() {
        return ResponseEntity.ok(batchOperationService.findJobs());
    }

    @PostMapping("/jobs")
    @FpsTransaction(id = "ADM02BAT0011", name = "ADMBatchJobRegister")
    @Operation(summary = "배치 Job 등록", description = "ADM에서 운영할 배치 Job 메타 정보를 등록하거나 갱신합니다.")
    public ResponseEntity<Map<String, Object>> registerJob(
            @RequestBody AdmBatchJobRegisterRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> result = batchOperationService.registerJob(
                request.jobId(),
                request.jobName(),
                request.jobType(),
                request.description(),
                requestUser(servletRequest, request.requestUser()));
        recordAudit(servletRequest, request.requestUser(), "BATCH_JOB_REGISTER", "pfw_batch_job",
                request.jobId(), reason, null, String.valueOf(result));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schedules")
    @FpsTransaction(id = "ADM01BAT0012", name = "ADMBatchScheduleList")
    @Operation(summary = "배치 스케줄 조회", description = "배치 스케줄, 영업일 전용 여부, 수행 가능 시간, 휴일 정책을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSchedules() {
        return ResponseEntity.ok(batchOperationService.findSchedules());
    }

    @GetMapping("/schedules/{scheduleId}/simulation")
    @FpsTransaction(id = "ADM01BAT0023", name = "ADMBatchScheduleSimulation")
    @Operation(summary = "배치 스케줄 시뮬레이션", description = "영업일 캘린더와 스케줄 정책을 기준으로 수행 가능 후보일을 미리 계산합니다.")
    public ResponseEntity<List<Map<String, Object>>> simulateSchedule(
            @PathVariable String scheduleId,
            @RequestParam(required = false) String baseDate,
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(batchOperationService.simulateSchedule(scheduleId, baseDate, days));
    }

    @GetMapping("/executions")
    @FpsTransaction(id = "ADM01BAT0013", name = "ADMBatchExecutionList")
    @Operation(summary = "배치 실행 이력 조회", description = "배치 실행 상태와 처리 건수를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findExecutions(
            @RequestParam(required = false) String jobId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(batchOperationService.findExecutions(jobId, limit));
    }

    @GetMapping("/executions/{executionId}")
    @FpsTransaction(id = "ADM01BAT0014", name = "ADMBatchExecutionDetail")
    @Operation(summary = "배치 실행 상세 조회", description = "배치 실행 상세와 step 로그, Spring Batch 실행 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findExecutionDetail(@PathVariable long executionId) {
        return ResponseEntity.ok(batchOperationService.findExecutionDetail(executionId));
    }

    @GetMapping("/instances")
    @FpsTransaction(id = "ADM01BAT0015", name = "ADMBatchInstanceList")
    @Operation(summary = "배치 인스턴스 조회", description = "배치 서버 인스턴스와 heartbeat 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findInstances() {
        return ResponseEntity.ok(batchOperationService.findInstances());
    }

    @GetMapping("/relations")
    @FpsTransaction(id = "ADM01BAT0024", name = "ADMBatchRelationList")
    @Operation(summary = "배치 관계 조회", description = "선행 Job, 후행 Job, 트리거 Job 관계를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRelations(@RequestParam(required = false) String jobId) {
        return ResponseEntity.ok(batchOperationService.findRelations(jobId));
    }

    @GetMapping("/execution-targets")
    @FpsTransaction(id = "ADM01BAT0025", name = "ADMBatchExecutionTargetList")
    @Operation(summary = "배치 수행 대상 조회", description = "수행 대기/배정/완료 대상 인스턴스와 예정 수행 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findExecutionTargets(
            @RequestParam(required = false) String jobId,
            @RequestParam(required = false) String dispatchStatus,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(batchOperationService.findExecutionTargets(jobId, dispatchStatus, limit));
    }

    @GetMapping("/calendar")
    @FpsTransaction(id = "ADM01BAT0016", name = "ADMBusinessCalendarList")
    @Operation(summary = "영업일 캘린더 조회", description = "캘린더 ID와 기간 기준으로 영업일/휴일 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findBusinessCalendar(
            @RequestParam(defaultValue = "DEFAULT") String calendarId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(batchOperationService.findBusinessCalendar(calendarId, fromDate, toDate));
    }

    @PostMapping("/calendar")
    @FpsTransaction(id = "ADM02BAT0017", name = "ADMBusinessCalendarSave")
    @Operation(summary = "영업일 캘린더 저장", description = "영업일/휴일 정보를 등록하거나 갱신합니다.")
    public ResponseEntity<Map<String, Object>> saveBusinessDay(
            @RequestBody AdmBusinessDayRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> result = batchOperationService.saveBusinessDay(
                request.calendarId(),
                request.businessDate(),
                request.holidayYn(),
                request.businessDayYn(),
                request.description(),
                requestUser(servletRequest, request.requestUser()));
        recordAudit(servletRequest, request.requestUser(), "BATCH_CALENDAR_SAVE", "pfw_business_day_calendar",
                request.calendarId() + ":" + request.businessDate(), reason, null, String.valueOf(result));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/jobs/{jobId}/run")
    @FpsTransaction(id = "ADM02BAT0018", name = "ADMBatchRun")
    @Operation(summary = "배치 수동 실행", description = "배치 실행을 요청하고 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> runJob(
            @PathVariable String jobId,
            @RequestBody AdmBatchOperationRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> execution = batchOperationService.requestRun(
                jobId, request.jobParameters(), requestUser(servletRequest, request.requestUser()), reason);
        recordAudit(servletRequest, request.requestUser(), "BATCH_RUN", "pfw_batch_execution",
                jobId, reason, null, String.valueOf(execution));
        return ResponseEntity.ok(execution);
    }

    @PostMapping("/executions/{executionId}/retry")
    @FpsTransaction(id = "ADM02BAT0019", name = "ADMBatchRetry")
    @Operation(summary = "배치 실패 재수행", description = "기존 실행 파라미터로 배치 재수행을 요청합니다.")
    public ResponseEntity<Map<String, Object>> retryExecution(
            @PathVariable long executionId,
            @RequestBody AdmBatchOperationRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> execution = batchOperationService.requestRetry(
                executionId, requestUser(servletRequest, request.requestUser()), reason);
        recordAudit(servletRequest, request.requestUser(), "BATCH_RETRY", "pfw_batch_execution",
                String.valueOf(executionId), reason, null, String.valueOf(execution));
        return ResponseEntity.ok(execution);
    }

    @PostMapping("/executions/{executionId}/stop")
    @FpsTransaction(id = "ADM03BAT0020", name = "ADMBatchStop")
    @Operation(summary = "배치 실행 중지", description = "실행 중인 배치에 중지를 요청하고 운영 이력을 남깁니다.")
    public ResponseEntity<Map<String, Object>> stopExecution(
            @PathVariable long executionId,
            @RequestBody AdmBatchOperationRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> execution = batchOperationService.requestStop(
                executionId, requestUser(servletRequest, request.requestUser()), reason);
        recordAudit(servletRequest, request.requestUser(), "BATCH_STOP", "pfw_batch_execution",
                String.valueOf(executionId), reason, null, String.valueOf(execution));
        return ResponseEntity.ok(execution);
    }

    @PostMapping("/schedules/{scheduleId}/enable")
    @FpsTransaction(id = "ADM03BAT0021", name = "ADMBatchScheduleEnable")
    @Operation(summary = "배치 스케줄 활성화", description = "배치 스케줄을 활성화합니다.")
    public ResponseEntity<Map<String, Object>> enableSchedule(
            @PathVariable String scheduleId,
            @RequestBody AdmBatchOperationRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> schedule = batchOperationService.updateScheduleEnabled(
                scheduleId, true, requestUser(servletRequest, request.requestUser()), reason);
        recordAudit(servletRequest, request.requestUser(), "BATCH_SCHEDULE_ENABLE", "pfw_batch_schedule",
                scheduleId, reason, null, String.valueOf(schedule));
        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/schedules/{scheduleId}/disable")
    @FpsTransaction(id = "ADM03BAT0022", name = "ADMBatchScheduleDisable")
    @Operation(summary = "배치 스케줄 비활성화", description = "배치 스케줄을 비활성화합니다.")
    public ResponseEntity<Map<String, Object>> disableSchedule(
            @PathVariable String scheduleId,
            @RequestBody AdmBatchOperationRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> schedule = batchOperationService.updateScheduleEnabled(
                scheduleId, false, requestUser(servletRequest, request.requestUser()), reason);
        recordAudit(servletRequest, request.requestUser(), "BATCH_SCHEDULE_DISABLE", "pfw_batch_schedule",
                scheduleId, reason, null, String.valueOf(schedule));
        return ResponseEntity.ok(schedule);
    }

    private void recordAudit(
            HttpServletRequest servletRequest,
            String requestUser,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            String beforeData,
            String afterData) {
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, requestUser),
                actionType,
                targetType,
                targetId,
                reason,
                beforeData,
                afterData,
                actionType,
                servletRequest.getRemoteAddr());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
