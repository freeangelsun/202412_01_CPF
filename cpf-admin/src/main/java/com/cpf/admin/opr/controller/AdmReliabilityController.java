package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmReliabilityActionRequest;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmBatchJobLogService;
import com.cpf.admin.opr.service.AdmReliabilityService;
import com.cpf.core.api.logging.CpfTraceRecoveryPort;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * 멱등, Broker, 파일전송, 결과 미확정 상태를 조회하고 수동 처리하는 ADM API입니다.
 */
@RestController
@RequestMapping("/adm/api/reliability")
@Tag(name = "ADM-OPR Reliability", description = "CPF reliability capability 운영 조회와 수동 처리 API")
public class AdmReliabilityController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmReliabilityService reliabilityService;
    private final AdmBatchJobLogService batchJobLogService;
    private final AdmAuditLogService auditLogService;
    private final CpfTraceRecoveryPort traceRecoveryPort;

    public AdmReliabilityController(
            AdmReliabilityService reliabilityService,
            AdmBatchJobLogService batchJobLogService,
            AdmAuditLogService auditLogService,
            CpfTraceRecoveryPort traceRecoveryPort) {
        this.reliabilityService = reliabilityService;
        this.batchJobLogService = batchJobLogService;
        this.auditLogService = auditLogService;
        this.traceRecoveryPort = traceRecoveryPort;
    }

    @GetMapping("/idempotency")
    @CpfOnlineTransaction(id = "OADMRE0001", name = "ADMReliabilityIdempotencyList")
    @Operation(operationId = "findAdmIdempotencyRecords", summary = "멱등 처리 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> idempotency(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String key,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findIdempotency(scope, status, key, limit));
    }

    @GetMapping("/broker/outbox")
    @CpfOnlineTransaction(id = "OADMRE0002", name = "ADMReliabilityOutboxList")
    @Operation(operationId = "findAdmBrokerOutbox", summary = "Broker outbox 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> outbox(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findOutbox(status, transactionGlobalId, topic, limit));
    }

    @GetMapping("/broker/inbox")
    @CpfOnlineTransaction(id = "OADMRE0003", name = "ADMReliabilityInboxList")
    @Operation(operationId = "findAdmBrokerInbox", summary = "Broker inbox 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> inbox(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String key,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findInbox(status, key, limit));
    }

    @GetMapping("/broker/dlq")
    @CpfOnlineTransaction(id = "OADMRE0004", name = "ADMReliabilityDlqList")
    @Operation(operationId = "findAdmBrokerDlq", summary = "Broker DLQ 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> dlq(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findDlq(status, transactionGlobalId, topic, limit));
    }

    @PostMapping("/broker/dlq/{messageId}/replay")
    @CpfOnlineTransaction(id = "OADMRE0005", name = "ADMReliabilityDlqReplay")
    @Operation(
            operationId = "requestAdmBrokerDlqReplay",
            summary = "Broker DLQ 재처리 요청",
            description = "서버 권한검사와 감사 사유 확인 후 중복되지 않게 재처리를 요청합니다.")
    public ResponseEntity<AdmReliabilityService.ChangeResult> replay(
            @Parameter(description = "Broker message ID", required = true)
            @PathVariable String messageId,
            @RequestBody AdmReliabilityActionRequest request,
            HttpServletRequest servletRequest) {
        String operatorId = requestUser(servletRequest, request.requestUser());
        String reason = auditLogService.requireReason(request.reason());
        AdmReliabilityService.ChangeResult result = reliabilityService.requestDlqReplay(
                messageId,
                operatorId,
                reason);
        recordAudit(servletRequest, operatorId, "BROKER_DLQ_REPLAY", "cpf_broker_dlq", messageId, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/file-transfers")
    @CpfOnlineTransaction(id = "OADMRE0006", name = "ADMReliabilityFileTransferList")
    @Operation(operationId = "findAdmFileTransferHistory", summary = "파일전송 이력 조회")
    public ResponseEntity<List<Map<String, Object>>> fileTransfers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findFileTransfers(status, transactionGlobalId, endpointCode, limit));
    }

    @GetMapping("/unknown-results")
    @CpfOnlineTransaction(id = "OADMRE0007", name = "ADMReliabilityUnknownResultList")
    @Operation(operationId = "findAdmUnknownResults", summary = "결과 미확정 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> unknownResults(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findUnknownResults(type, status, transactionGlobalId, limit));
    }

    @GetMapping("/batch-job-logs")
    @CpfOnlineTransaction(id = "OADMRE0009", name = "ADMReliabilityBatchJobLogList")
    @Operation(
            operationId = "findAdmBatchJobInstanceLogs",
            summary = "BAT JobInstance 로그 목록 조회",
            description = "CPF_LOG_ROOT 아래 정규화된 BAT JobInstance 로그 메타데이터만 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> batchJobLogs(
            @RequestParam(required = false) String businessDate,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) Long jobInstanceId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(batchJobLogService.findLogs(businessDate, jobName, jobInstanceId, limit));
    }

    @GetMapping("/batch-job-logs/{businessDate}/{jobName}/{jobInstanceId}")
    @CpfOnlineTransaction(id = "OADMRE0010", name = "ADMReliabilityBatchJobLogDetail")
    @Operation(
            operationId = "getAdmBatchJobInstanceLog",
            summary = "BAT JobInstance 로그 상세 조회",
            description = "경로 이탈과 심볼릭 링크를 차단하고 마지막 JSON Lines 레코드를 구조화해 반환합니다.")
    public ResponseEntity<Map<String, Object>> batchJobLogDetail(
            @PathVariable String businessDate,
            @PathVariable String jobName,
            @PathVariable long jobInstanceId,
            @RequestParam(defaultValue = "200") int maxRecords) {
        return ResponseEntity.ok(batchJobLogService.findDetail(
                businessDate,
                jobName,
                jobInstanceId,
                maxRecords));
    }

    @GetMapping("/transaction-log-recovery")
    @CpfOnlineTransaction(id = "OADMRE0011", name = "ADMTransactionLogRecoveryStatus")
    @Operation(
            operationId = "getAdmTransactionLogRecoveryStatus",
            summary = "DB 거래 로그 복구 상태 조회",
            description = "CPF durable journal의 pending, processing, poison, 용량과 복구 worker 상태를 조회합니다.")
    public ResponseEntity<CpfTraceRecoveryPort.TraceRecoveryStatus> transactionLogRecoveryStatus() {
        return ResponseEntity.ok(traceRecoveryPort.status());
    }

    @PostMapping("/transaction-log-recovery/run")
    @CpfOnlineTransaction(id = "OADMRE0012", name = "ADMTransactionLogRecoveryRun")
    @Operation(
            operationId = "runAdmTransactionLogRecovery",
            summary = "DB 거래 로그 복구 즉시 실행",
            description = "감사 사유를 확인하고 현재 재시도 가능한 durable journal을 즉시 재적재합니다.")
    public ResponseEntity<CpfTraceRecoveryPort.TraceRecoveryRunResult> runTransactionLogRecovery(
            @RequestBody AdmReliabilityActionRequest request,
            HttpServletRequest servletRequest) {
        String operatorId = requestUser(servletRequest, request.requestUser());
        String reason = auditLogService.requireReason(request.reason());
        CpfTraceRecoveryPort.TraceRecoveryStatus before = traceRecoveryPort.status();
        CpfTraceRecoveryPort.TraceRecoveryRunResult result = traceRecoveryPort.recoverReadyEvents();
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                operatorId,
                "TRANSACTION_LOG_RECOVERY_RUN",
                "cpf_transaction_log_recovery",
                "PENDING",
                reason,
                String.valueOf(before),
                String.valueOf(traceRecoveryPort.status()),
                "DB 거래·구간 로그 durable journal 즉시 복구",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/transaction-log-recovery/poison/{target}/{recoveryEventId}/retry")
    @CpfOnlineTransaction(id = "OADMRE0013", name = "ADMTraceRecoveryPoisonRetry")
    @Operation(
            operationId = "retryAdmTraceRecoveryPoison",
            summary = "추적 로그 poison 재시도 승인",
            description = "감사 사유와 운영 권한을 확인한 뒤 거래 또는 구간 poison 이벤트를 pending 대기열로 이동합니다.")
    public ResponseEntity<CpfTraceRecoveryPort.PoisonRetryResult> retryRecoveryPoison(
            @PathVariable CpfTraceRecoveryPort.RecoveryTarget target,
            @PathVariable String recoveryEventId,
            @RequestBody AdmReliabilityActionRequest request,
            HttpServletRequest servletRequest) {
        String operatorId = requestUser(servletRequest, request.requestUser());
        String reason = auditLogService.requireReason(request.reason());
        CpfTraceRecoveryPort.TraceRecoveryStatus before = traceRecoveryPort.status();
        CpfTraceRecoveryPort.PoisonRetryResult result = traceRecoveryPort.retryPoison(target, recoveryEventId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                operatorId,
                "TRACE_RECOVERY_POISON_RETRY",
                "CPF_TRACE_RECOVERY",
                recoveryEventId,
                reason,
                String.valueOf(before),
                String.valueOf(traceRecoveryPort.status()),
                "추적 로그 poison 재시도 승인",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/unknown-results/{unknownId}/resolve")
    @CpfOnlineTransaction(id = "OADMRE0008", name = "ADMReliabilityUnknownResultResolve")
    @Operation(
            operationId = "resolveAdmUnknownResult",
            summary = "결과 미확정 수동 처리",
            description = "감사 사유와 변경 전후 상태를 기록하고 이미 끝난 건의 중복 처리를 차단합니다.")
    public ResponseEntity<AdmReliabilityService.ChangeResult> resolveUnknown(
            @PathVariable String unknownId,
            @RequestBody AdmReliabilityActionRequest request,
            HttpServletRequest servletRequest) {
        String operatorId = requestUser(servletRequest, request.requestUser());
        String reason = auditLogService.requireReason(request.reason());
        AdmReliabilityService.ChangeResult result = reliabilityService.resolveUnknown(
                unknownId,
                request.targetStatus(),
                operatorId,
                reason);
        recordAudit(servletRequest, operatorId, "UNKNOWN_RESULT_RESOLVE", "cpf_unknown_result", unknownId, result);
        return ResponseEntity.ok(result);
    }

    private void recordAudit(
            HttpServletRequest servletRequest,
            String operatorId,
            String action,
            String targetType,
            String targetId,
            AdmReliabilityService.ChangeResult result) {
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                operatorId,
                action,
                targetType,
                targetId,
                result.reason(),
                String.valueOf(result.before()),
                String.valueOf(result.after()),
                "상태 변경",
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
