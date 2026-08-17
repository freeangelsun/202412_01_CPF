package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmObservabilityService;
import com.cpf.platform.operations.observability.api.logging.CpfFileLogRuntimeStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ADM 통합 운영 추적 API입니다.
 *
 * <p>운영자는 장애나 문의를 하나의 거래 ID, trace ID, 업무 거래 ID 중 하나로 시작하는 경우가 많습니다.
 * 이 API는 같은 기준으로 거래 로그, 실패 로그, 일반 감사, 로그 정책 감사, 배치 실행 연결 정보를 묶어 반환합니다.</p>
 */
@RestController
@RequestMapping("/adm/api/observability")
@Tag(name = "ADM-Observability", description = "ADM 거래, 오류, 감사 통합 추적 API")
public class AdmObservabilityController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmObservabilityService observabilityService;
    private final ObjectProvider<CpfFileLogRuntimeStatus> fileLogRuntimeStatusProvider;

    public AdmObservabilityController(
            AdmObservabilityService observabilityService,
            ObjectProvider<CpfFileLogRuntimeStatus> fileLogRuntimeStatusProvider) {
        this.observabilityService = observabilityService;
        this.fileLogRuntimeStatusProvider = fileLogRuntimeStatusProvider;
    }

    @GetMapping("/file-log-recovery")    @Operation(operationId = "getAdmFileLogRecoveryStatus", summary = "파일 로그 내구 복구 상태", description = "파일 로그 직접 쓰기 실패, durable spool pending/replay/quarantine/terminal-loss 상태를 조회합니다. terminalLoss 또는 quarantine은 운영자 확인이 필요하며 pending은 재전송 대기 상태입니다.")
    public ResponseEntity<Map<String, Object>> fileLogRecoveryStatus() {
        CpfFileLogRuntimeStatus runtime = fileLogRuntimeStatusProvider.getIfAvailable();
        if (runtime == null) {
            return ResponseEntity.ok(Map.of(
                    "available", false,
                    "health", "UNKNOWN",
                    "alertState", "UNAVAILABLE"));
        }
        CpfFileLogRuntimeStatus.FileWriteDiagnostics write = runtime.fileWriteDiagnostics();
        CpfFileLogRuntimeStatus.FileRecoveryDiagnostics recovery = runtime.fileRecoveryDiagnostics();
        CpfFileLogRuntimeStatus.FileLogRuntimeSnapshot retention = runtime.fileLogRuntimeSnapshot();
        boolean terminal = recovery.terminalLoss() > 0L;
        boolean quarantined = recovery.quarantined() > 0L;
        boolean pending = recovery.pending() > 0L;
        String health = terminal ? "DOWN" : (quarantined || pending || write.writeFailureCount() > 0L ? "DEGRADED" : retention.health());
        String alertState = terminal ? "TERMINAL_LOSS" : (quarantined ? "QUARANTINED" : (pending ? "RETRY_PENDING" : "CLEAR"));
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("available", true);
        result.put("health", health);
        result.put("alertState", alertState);
        result.put("write", write);
        result.put("recovery", recovery);
        result.put("retention", retention);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/transactions/{transactionId}")    @Operation(operationId = "traceAdmByTransactionId", summary = "거래 글로벌 ID 통합 추적", description = "transactionId 기준으로 거래 로그, 실패 로그, 일반 감사, 정책 감사, 배치 실행 연결 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> traceByTransactionId(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(observabilityService.traceByTransactionId(transactionId, limit));
    }

    @GetMapping("/traces/{traceId}")    @Operation(operationId = "traceAdmByTraceId", summary = "Trace ID 통합 추적", description = "traceId 기준으로 거래 로그, 실패 로그, 일반 감사, 정책 감사, 배치 실행 연결 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> traceByTraceId(
            @PathVariable String traceId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(observabilityService.traceByTraceId(traceId, limit));
    }

    @GetMapping("/business-transactions/{businessTransactionId}")    @Operation(operationId = "traceAdmByBusinessTransactionId", summary = "업무 거래 ID 통합 추적", description = "businessTransactionId 기준으로 거래 로그, 실패 로그, 일반 감사, 정책 감사, 배치 실행 연결 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> traceByBusinessTransactionId(
            @PathVariable String businessTransactionId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(observabilityService.traceByBusinessTransactionId(businessTransactionId, limit));
    }
}
