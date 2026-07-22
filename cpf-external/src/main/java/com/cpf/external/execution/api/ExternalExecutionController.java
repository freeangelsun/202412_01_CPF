package com.cpf.external.execution.api;

import com.cpf.external.common.base.ExternalBaseController;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.external.execution.application.ExternalExecutionService;
import com.cpf.external.execution.domain.ExternalExecution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 대외 호출 접수, 상태 조회와 결과 불명 복구 API를 제공합니다. */
@RestController
@RequestMapping("/api/v1/external/executions")
@Tag(name = "EXS 대외연계", description = "멱등 대외 호출과 결과 불명 조회·복구")
public class ExternalExecutionController extends ExternalBaseController {

    private final ExternalExecutionService service;

    public ExternalExecutionController(ExternalExecutionService service) {
        this.service = service;
    }

    @PostMapping
    @CpfOnlineTransaction(id = "OEXSEX0001", name = "EXS대외호출", ownerDomain = "EXS")
    @Operation(operationId = "executeExternalRequest", summary = "멱등 대외 요청 실행")
    public ResponseEntity<ExternalExecution> execute(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody ExecuteRequest request) {
        return ResponseEntity.ok(service.execute(new ExternalExecutionService.ExecuteCommand(
                request.institutionCode(),
                request.endpointCode(),
                request.externalRequestId(),
                idempotencyKey,
                request.payload())));
    }

    @GetMapping("/{executionId}")
    @CpfOnlineTransaction(id = "OEXSEX0002", name = "EXS대외호출조회", ownerDomain = "EXS")
    @Operation(operationId = "findExternalExecution", summary = "대외 요청 상태 조회")
    public ResponseEntity<ExternalExecution> find(@PathVariable String executionId) {
        return ResponseEntity.ok(service.find(executionId));
    }

    @PostMapping("/{executionId}/inquiries")
    @CpfOnlineTransaction(id = "OEXSEX0003", name = "EXS결과재조회", ownerDomain = "EXS",
            requiredPermission = "EXS_RECONCILE", auditReasonRequired = true)
    @Operation(operationId = "inquireExternalExecution", summary = "결과 불명 기관 재조회")
    public ResponseEntity<ExternalExecution> inquire(@PathVariable String executionId) {
        return ResponseEntity.ok(service.inquire(executionId));
    }

    @PostMapping("/{executionId}/reconciliation")
    @CpfOnlineTransaction(id = "OEXSEX0004", name = "EXS결과수동확정", ownerDomain = "EXS",
            requiredPermission = "EXS_RECONCILE", auditReasonRequired = true)
    @Operation(operationId = "reconcileExternalExecution", summary = "결과 불명 수동 확정")
    public ResponseEntity<ExternalExecution> reconcile(
            @PathVariable String executionId,
            @Valid @RequestBody ReconcileRequest request) {
        return ResponseEntity.ok(service.reconcile(executionId, new ExternalExecutionService.ReconcileCommand(
                request.outcome(), request.operatorId(), request.reason())));
    }

    public record ExecuteRequest(
            @NotBlank String institutionCode,
            @NotBlank String endpointCode,
            String externalRequestId,
            @NotNull Map<String, Object> payload) {
    }

    public record ReconcileRequest(
            @NotBlank String outcome,
            @NotBlank String operatorId,
            @NotBlank String reason) {
    }
}
