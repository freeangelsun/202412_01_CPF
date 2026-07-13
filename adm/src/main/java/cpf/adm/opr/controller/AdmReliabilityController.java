package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmReliabilityActionRequest;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmBatchJobLogService;
import cpf.adm.opr.service.AdmReliabilityService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
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
@Tag(name = "ADM-OPR Reliability", description = "PFW reliability capability 운영 조회와 수동 처리 API")
public class AdmReliabilityController {
    private final AdmReliabilityService reliabilityService;
    private final AdmBatchJobLogService batchJobLogService;
    private final AdmAuditLogService auditLogService;

    public AdmReliabilityController(
            AdmReliabilityService reliabilityService,
            AdmBatchJobLogService batchJobLogService,
            AdmAuditLogService auditLogService) {
        this.reliabilityService = reliabilityService;
        this.batchJobLogService = batchJobLogService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/idempotency")
    @CpfTransaction(id = "ADM01REL0001", name = "ADMReliabilityIdempotencyList")
    @Operation(operationId = "findAdmIdempotencyRecords", summary = "멱등 처리 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> idempotency(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String key,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findIdempotency(scope, status, key, limit));
    }

    @GetMapping("/broker/outbox")
    @CpfTransaction(id = "ADM01REL0002", name = "ADMReliabilityOutboxList")
    @Operation(operationId = "findAdmBrokerOutbox", summary = "Broker outbox 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> outbox(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findOutbox(status, transactionGlobalId, topic, limit));
    }

    @GetMapping("/broker/inbox")
    @CpfTransaction(id = "ADM01REL0003", name = "ADMReliabilityInboxList")
    @Operation(operationId = "findAdmBrokerInbox", summary = "Broker inbox 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> inbox(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String key,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findInbox(status, key, limit));
    }

    @GetMapping("/broker/dlq")
    @CpfTransaction(id = "ADM01REL0004", name = "ADMReliabilityDlqList")
    @Operation(operationId = "findAdmBrokerDlq", summary = "Broker DLQ 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> dlq(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findDlq(status, transactionGlobalId, topic, limit));
    }

    @PostMapping("/broker/dlq/{messageId}/replay")
    @CpfTransaction(id = "ADM05REL0005", name = "ADMReliabilityDlqReplay")
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
        recordAudit(servletRequest, operatorId, "BROKER_DLQ_REPLAY", "pfw_broker_dlq", messageId, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/file-transfers")
    @CpfTransaction(id = "ADM01REL0006", name = "ADMReliabilityFileTransferList")
    @Operation(operationId = "findAdmFileTransferHistory", summary = "파일전송 이력 조회")
    public ResponseEntity<List<Map<String, Object>>> fileTransfers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findFileTransfers(status, transactionGlobalId, endpointCode, limit));
    }

    @GetMapping("/unknown-results")
    @CpfTransaction(id = "ADM01REL0007", name = "ADMReliabilityUnknownResultList")
    @Operation(operationId = "findAdmUnknownResults", summary = "결과 미확정 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> unknownResults(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reliabilityService.findUnknownResults(type, status, transactionGlobalId, limit));
    }

    @GetMapping("/batch-job-logs")
    @CpfTransaction(id = "ADM01REL0009", name = "ADMReliabilityBatchJobLogList")
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
    @CpfTransaction(id = "ADM01REL0010", name = "ADMReliabilityBatchJobLogDetail")
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

    @PostMapping("/unknown-results/{unknownId}/resolve")
    @CpfTransaction(id = "ADM05REL0008", name = "ADMReliabilityUnknownResultResolve")
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
        recordAudit(servletRequest, operatorId, "UNKNOWN_RESULT_RESOLVE", "pfw_unknown_result", unknownId, result);
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
