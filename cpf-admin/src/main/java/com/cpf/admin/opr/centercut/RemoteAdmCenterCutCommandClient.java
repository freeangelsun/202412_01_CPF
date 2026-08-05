package com.cpf.admin.opr.centercut;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.data.CpfDataRow;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import com.cpf.core.api.util.CpfHeaders;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
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
            AdmAuthenticatedOperatorContext operatorContext,
            @Value("${cpf.framework.instance-id:adm-local-01}") String callerInstanceId) {
        this.caller = Objects.requireNonNull(caller, "caller");
        this.webClient = Objects.requireNonNull(webClientBuilder, "webClientBuilder").build();
        this.operatorContext = Objects.requireNonNull(operatorContext, "operatorContext");
        this.callerInstanceId = required(callerInstanceId, "callerInstanceId");
    }

    @Override
    public Map<String, Object> reprocessFailed(String executionId, CpfBatchRiskCommand command) {
        return invoke(executionId, "reprocess-failed", command);
    }

    @Override
    public Map<String, Object> reconcileUnknown(String executionId, CpfBatchRiskCommand command) {
        return invoke(executionId, "reconcile-unknown", command);
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
                .header(CpfHeaders.callerService(), CALLER_SERVICE)
                .header(CpfHeaders.callerInstanceId(), callerInstanceId)
                .header(CpfHeaders.operatorId(), approvedBy)
                .header(CpfHeaders.idempotencyKey(), command.idempotencyKey())
                .header(CpfHeaders.approvalRequestId(), command.approvalRequestId())
                .header(CpfHeaders.approvalRequesterId(), command.requestUser())
                .attribute("ownerDomain", "BAT")
                .attribute("callerDomain", "ADM")
                .build();

        CpfServiceResult<Object> result = caller.invoke(request, target -> webClient.post()
                .uri(join(target.baseUrl(), path))
                .headers(headers -> {
                    headers.set(CpfHeaders.callerService(), CALLER_SERVICE);
                    headers.set(CpfHeaders.callerInstanceId(), callerInstanceId);
                    headers.set(CpfHeaders.operatorId(), approvedBy);
                    headers.set(CpfHeaders.idempotencyKey(), command.idempotencyKey());
                    headers.set(CpfHeaders.approvalRequestId(), command.approvalRequestId());
                    headers.set(CpfHeaders.approvalRequesterId(), command.requestUser());
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
