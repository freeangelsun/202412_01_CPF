package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmObservabilityService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class AdmObservabilityController extends cpf.adm.common.base.AdmBaseController {
    private final AdmObservabilityService observabilityService;

    public AdmObservabilityController(AdmObservabilityService observabilityService) {
        this.observabilityService = observabilityService;
    }

    @GetMapping("/transactions/{transactionGlobalId}")
    @CpfOnlineTransaction(id = "OADMOB0010", name = "ADMObservabilityByTransaction")
    @Operation(operationId = "traceAdmByTransactionGlobalId", summary = "거래 글로벌 ID 통합 추적", description = "transactionGlobalId 기준으로 거래 로그, 실패 로그, 일반 감사, 정책 감사, 배치 실행 연결 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> traceByTransactionGlobalId(
            @PathVariable String transactionGlobalId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(observabilityService.traceByTransactionGlobalId(transactionGlobalId, limit));
    }

    @GetMapping("/traces/{traceId}")
    @CpfOnlineTransaction(id = "OADMOB0011", name = "ADMObservabilityByTrace")
    @Operation(operationId = "traceAdmByTraceId", summary = "Trace ID 통합 추적", description = "traceId 기준으로 거래 로그, 실패 로그, 일반 감사, 정책 감사, 배치 실행 연결 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> traceByTraceId(
            @PathVariable String traceId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(observabilityService.traceByTraceId(traceId, limit));
    }

    @GetMapping("/business-transactions/{businessTransactionId}")
    @CpfOnlineTransaction(id = "OADMOB0012", name = "ADMObservabilityByBusinessTransaction")
    @Operation(operationId = "traceAdmByBusinessTransactionId", summary = "업무 거래 ID 통합 추적", description = "businessTransactionId 기준으로 거래 로그, 실패 로그, 일반 감사, 정책 감사, 배치 실행 연결 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> traceByBusinessTransactionId(
            @PathVariable String businessTransactionId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(observabilityService.traceByBusinessTransactionId(businessTransactionId, limit));
    }
}
