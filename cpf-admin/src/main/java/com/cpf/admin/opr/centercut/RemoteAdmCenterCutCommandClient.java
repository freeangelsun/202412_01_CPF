package com.cpf.admin.opr.centercut;

import com.cpf.foundation.runtime.CpfInstanceIdentity;

import com.cpf.batch.api.BatControlHeaders;
import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.batch.api.CpfBatchOwnerUnknownResultException;
import com.cpf.batch.api.CpfBatchRiskCommand;
import com.cpf.data.api.CpfDataRow;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import com.cpf.integration.api.servicecall.CpfServiceRequest;
import com.cpf.integration.api.servicecall.CpfServiceResult;
import com.cpf.web.api.CpfHttpHeaders;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/** 분리 WAS topology에서 ADM이 BAT Center-Cut execution-scope API를 호출합니다. */
@Component
public final class RemoteAdmCenterCutCommandClient implements AdmCenterCutCommandClient {
    private static final String SERVICE_ID = "BAT";
    private static final String ENDPOINT_CODE = "SBATOP0001";
    private static final String CALLER_SERVICE = "ADM";

    private final CpfServiceCaller caller;
    private final WebClient webClient;
    private final AdmAuthenticatedOperatorContext operatorContext;
    private final String callerInstanceId;

    public RemoteAdmCenterCutCommandClient(
            CpfServiceCaller caller,
            WebClient.Builder webClientBuilder,
            AdmAuthenticatedOperatorContext operatorContext) {
        this.caller = Objects.requireNonNull(caller, "caller");
        this.webClient = Objects.requireNonNull(webClientBuilder, "webClientBuilder").build();
        this.operatorContext = Objects.requireNonNull(operatorContext, "operatorContext");
        this.callerInstanceId = CpfInstanceIdentity.current().instanceId();
    }

    @Override
    public Map<String, Object> reprocessFailed(String executionId, CpfBatchRiskCommand command) {
        return invoke(executionId, "reprocess-failed", command);
    }

    @Override
    public Map<String, Object> reconcileUnknown(String executionId, CpfBatchRiskCommand command) {
        return invoke(executionId, "reconcile-unknown", command);
    }

    @Override
    public Map<String, Object> observe(String executionId) {
        String safeExecutionId = requiredIdentifier(executionId);
        String actor = required(operatorContext.currentOperatorId(), "operatorId");
        String path = "/api/v1/batch/center-cut/executions/" + safeExecutionId + "/reconciliation-status";
        CpfServiceRequest request = CpfServiceRequest.builder(SERVICE_ID)
                .endpointCode(ENDPOINT_CODE).httpMethod("GET").requestPath(path)
                .header(BatControlHeaders.CALLER_SERVICE, CALLER_SERVICE)
                .header(BatControlHeaders.CALLER_INSTANCE_ID, callerInstanceId)
                .header(BatControlHeaders.OPERATOR_ID, actor)
                .attribute("ownerDomain", "BAT").attribute("callerDomain", "ADM").build();
        CpfServiceResult<Object> result = caller.invoke(request, target -> webClient.get()
                .uri(join(target.baseUrl(), path))
                .headers(headers -> {
                    headers.set(BatControlHeaders.CALLER_SERVICE, CALLER_SERVICE);
                    headers.set(BatControlHeaders.CALLER_INSTANCE_ID, callerInstanceId);
                    headers.set(BatControlHeaders.OPERATOR_ID, actor);
                }).retrieve().bodyToMono(Object.class).block());
        if (result.unknown() || !result.success() || result.responseBody() == null) return Map.of();
        return new LinkedHashMap<>(CpfDataRow.copyOf(result.responseBody()));
    }

    private Map<String, Object> invoke(
            String executionId, String operationPath, CpfBatchRiskCommand command) {
        String safeExecutionId = requiredIdentifier(executionId);
        Objects.requireNonNull(command, "command");
        String approvedBy = required(operatorContext.currentOperatorId(), "approvedBy");
        String path = "/api/v1/batch/center-cut/executions/" + safeExecutionId + "/" + operationPath;
        Map<String, Object> body = Map.of(
                "requestedBy", command.requestUser(),
                "approvedBy", approvedBy,
                "reason", command.reason());

        CpfServiceRequest request = CpfServiceRequest.builder(SERVICE_ID)
                .endpointCode(ENDPOINT_CODE)
                .httpMethod("POST")
                .requestPath(path)
                .header(BatControlHeaders.CALLER_SERVICE, CALLER_SERVICE)
                .header(BatControlHeaders.CALLER_INSTANCE_ID, callerInstanceId)
                .header(BatControlHeaders.OPERATOR_ID, approvedBy)
                .header(CpfHttpHeaders.idempotencyKey(), command.idempotencyKey())
                .header(BatControlHeaders.APPROVAL_REQUEST_ID, command.approvalRequestId())
                .header(BatControlHeaders.APPROVAL_REQUESTER_ID, command.requestUser())
                .attribute("ownerDomain", "BAT")
                .attribute("callerDomain", "ADM")
                .build();

        CpfServiceResult<Object> result = caller.invoke(request, target -> webClient.post()
                .uri(join(target.baseUrl(), path))
                .headers(headers -> {
                    headers.set(BatControlHeaders.CALLER_SERVICE, CALLER_SERVICE);
                    headers.set(BatControlHeaders.CALLER_INSTANCE_ID, callerInstanceId);
                    headers.set(BatControlHeaders.OPERATOR_ID, approvedBy);
                    headers.set(CpfHttpHeaders.idempotencyKey(), command.idempotencyKey());
                    headers.set(BatControlHeaders.APPROVAL_REQUEST_ID, command.approvalRequestId());
                    headers.set(BatControlHeaders.APPROVAL_REQUESTER_ID, command.requestUser());
                })
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Object.class)
                .block());
        if (result.unknown()) {
            throw new CpfBatchOwnerUnknownResultException(
                    result.failureCode(),
                    "BAT Center-Cut 결과를 확정할 수 없습니다. reconciliation 필요. code="
                            + result.failureCode() + ", message=" + result.failureMessage());
        }
        if (!result.success()) {
            throw new IllegalStateException(
                    "BAT Center-Cut 호출 실패 status=" + result.status()
                            + ", code=" + result.failureCode()
                            + ", message=" + result.failureMessage());
        }
        CpfDataRow row = CpfDataRow.copyOf(result.responseBody());
        return new LinkedHashMap<>(row);
    }

    private static String requiredIdentifier(String value) {
        String normalized = required(value, "executionId");
        if (!normalized.matches("[A-Za-z0-9._:-]{1,128}")) {
            throw new IllegalArgumentException("executionId contains unsafe path characters");
        }
        return normalized;
    }

    private static String join(String baseUrl, String path) {
        String base = required(baseUrl, "BAT baseUrl");
        return base.endsWith("/") ? base.substring(0, base.length() - 1) + path : base + path;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
