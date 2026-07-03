package cpf.exs.operation.controller;

import cpf.exs.operation.service.ExsOperationService;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.segment.TransactionSegmentDirection;
import cpf.pfw.common.logging.segment.TransactionSegmentRole;
import cpf.pfw.common.logging.segment.TransactionSegmentScope;
import cpf.pfw.common.logging.segment.TransactionSegmentService;
import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EXS 외부기관 mock 처리 구간을 기록하는 교육 API입니다.
 */
@RestController
@RequestMapping("/api/exs/edu")
@Tag(name = "EXS-EDU Composite Transaction", description = "EXS 외부연계 segment 기록 교육 API")
public class ExsCompositeEducationController {
    private final TransactionSegmentService segmentService;
    private final ExsOperationService operationService;

    public ExsCompositeEducationController(
            TransactionSegmentService segmentService,
            ExsOperationService operationService) {
        this.segmentService = segmentService;
        this.operationService = operationService;
    }

    @PostMapping("/external-transfer")
    @CpfTransaction(id = "EXS09EDU0001", name = "EXSCompositeExternalTransfer")
    @Operation(summary = "EXS 외부기관 mock 호출", description = "전달된 transactionGlobalId를 유지하고 EXTERNAL segment와 EXS 송신 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> externalTransfer(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = request == null ? Map.of() : request;
        try (TransactionSegmentScope scope = segmentService.start(
                TransactionSegmentRole.EXTERNAL,
                TransactionSegmentDirection.INBOUND,
                "EXS",
                "ACC",
                "EXS",
                "/api/exs/edu/external-transfer",
                "EXS 외부기관 mock 처리")) {
            try {
                scope.record().setExternalInstitutionCode(text(body, "institutionCode", "BANK01"));
                scope.record().setExternalTransactionId(text(body, "externalTransactionId", "N/A"));
                Map<String, Object> exchange = operationService.sendOutbound(TransactionContext.getOrCreateTransactionId(), body);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
                response.put("rootTransactionGlobalId", TransactionContext.originalTransactionId());
                response.put("transactionSegmentId", scope.transactionSegmentId());
                response.put("moduleCode", "EXS");
                response.put("transactionRole", scope.record().getTransactionRole());
                response.put("externalInstitutionCode", text(body, "institutionCode", "BANK01"));
                response.put("externalTransactionId", SensitiveDataMasker.mask(text(body, "externalTransactionId", "N/A")));
                response.put("exchangeLog", exchange);
                scope.success();
                return ResponseEntity.ok(response);
            } catch (RuntimeException ex) {
                scope.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    private String text(Map<String, Object> body, String key, String fallback) {
        Object value = body.get(key);
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
    }
}
