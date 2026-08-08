package com.cpf.admin.opr.batch.runtime;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.data.CpfDataRow;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import com.cpf.core.api.util.CpfHeaders;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/** ADM은 Service Registry 기반 Typed ServiceCall로 BAT Control Server Owner API만 호출합니다. */
@Component
public class BatchRuntimeControlClient {
    private static final String SERVICE_ID = "BAT";
    private static final String ENDPOINT_CODE = "SBATCT0001";

    private final CpfServiceCaller caller;
    private final WebClient webClient;
    private final AdmAuthenticatedOperatorContext operatorContext;
    private final String callerInstanceId;

    public BatchRuntimeControlClient(
            CpfServiceCaller caller,
            WebClient.Builder builder,
            AdmAuthenticatedOperatorContext operatorContext,
            @Value("${cpf.framework.instance-id:adm-local-01}") String callerInstanceId) {
        this.caller = caller;
        this.webClient = builder.build();
        this.operatorContext = operatorContext;
        this.callerInstanceId = required(callerInstanceId, "callerInstanceId");
    }

    public List<CpfDataRow> instances(long staleAfterSeconds) {
        long normalized = Math.max(5, staleAfterSeconds);
        return rows(invoke(
                HttpMethod.GET,
                "/api/v1/batch/runtime/instances?staleAfterSeconds=" + normalized,
                null));
    }

    public CpfDataRow view(String view) {
        return row(invoke(HttpMethod.GET, "/api/v1/batch/views/" + encode(view), null));
    }

    public CpfDataRow createPlan(Map<String, Object> request) {
        return row(invoke(HttpMethod.POST, "/api/v1/batch/deployment-plans", request));
    }

    public CpfDataRow commandApproved(Map<String, Object> request, String approvalRequestId, String approvalRequesterId) {
        return row(invoke(HttpMethod.POST, "/api/v1/batch/runtime/commands", request,
                new ApprovalContext(required(approvalRequestId, "approvalRequestId"), required(approvalRequesterId, "approvalRequesterId"))));
    }

    public CpfDataRow commandState(String key) {
        return row(invoke(
                HttpMethod.GET,
                "/api/v1/batch/runtime/commands/" + encode(key),
                null));
    }

    public List<CpfDataRow> jobDefinitions(String jobId, String state, int limit) {
        StringBuilder path = new StringBuilder("/api/v1/batch/job-definitions?limit=")
                .append(Math.max(1, Math.min(limit, 1000)));
        if (text(jobId)) {
            path.append("&jobId=").append(encode(jobId));
        }
        if (text(state)) {
            path.append("&state=").append(encode(state));
        }
        return rows(invoke(HttpMethod.GET, path.toString(), null));
    }

    public CpfDataRow jobDefinitionDetail(String jobId, long version) {
        return row(invoke(
                HttpMethod.GET,
                "/api/v1/batch/job-definitions/" + encode(jobId) + "/versions/" + version,
                null));
    }

    public CpfDataRow validateJobDefinition(Map<String, Object> request) {
        return row(invoke(
                HttpMethod.POST,
                "/api/v1/batch/job-definitions/validate",
                request));
    }

    public CpfDataRow saveJobDefinition(Map<String, Object> request) {
        return row(invoke(
                HttpMethod.POST,
                "/api/v1/batch/job-definitions/drafts",
                request));
    }

    public CpfDataRow transitionJobDefinition(
            String jobId,
            long version,
            Map<String, Object> request) {
        String path = "/api/v1/batch/job-definitions/"
                + encode(jobId)
                + "/versions/"
                + version
                + "/transition";
        return row(invoke(HttpMethod.POST, path, request));
    }

    private Object invoke(HttpMethod method, String path, Object payload) {
        return invoke(method, path, payload, null);
    }

    private Object invoke(HttpMethod method, String path, Object payload, ApprovalContext approval) {
        String actor = required(operatorContext.currentOperatorId(), "authenticated operator");
        var requestBuilder = CpfServiceRequest.builder(SERVICE_ID)
                .endpointCode(ENDPOINT_CODE)
                .httpMethod(method.name())
                .requestPath(path)
                .header(CpfHeaders.callerService(), "ADM")
                .header(CpfHeaders.callerInstanceId(), callerInstanceId)
                .header(CpfHeaders.operatorId(), actor)
                .attribute("ownerDomain", "BAT")
                .attribute("callerDomain", "ADM");
        if (approval != null) {
            requestBuilder.header(CpfHeaders.approvalRequestId(), approval.approvalRequestId());
            requestBuilder.header(CpfHeaders.approvalRequesterId(), approval.approvalRequesterId());
        }
        CpfServiceRequest request = requestBuilder.build();

        CpfServiceResult<Object> result = caller.invoke(request, target -> {
            try {
                WebClient.RequestBodySpec call = webClient.method(method)
                        .uri(join(target.baseUrl(), path))
                        .headers(headers -> {
                            headers.set(CpfHeaders.callerService(), "ADM");
                            headers.set(CpfHeaders.callerInstanceId(), callerInstanceId);
                            headers.set(CpfHeaders.operatorId(), actor);
                            if (approval != null) {
                                headers.set(CpfHeaders.approvalRequestId(), approval.approvalRequestId());
                                headers.set(CpfHeaders.approvalRequesterId(), approval.approvalRequesterId());
                            }
                        });
                return (payload == null ? call : call.bodyValue(payload))
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block();
            } catch (WebClientResponseException exception) {
                throw ownerHttp(exception);
            }
        });

        if (result.unknown()) {
            throw new BatchControlClientException(
                    BatchControlClientException.Category.UNKNOWN_RESULT,
                    value(result.failureCode(), "BAT_CONTROL_UNKNOWN"),
                    "BAT Owner 호출 결과를 확정할 수 없습니다.",
                    null,
                    null);
        }
        if (!result.success()) {
            throw new BatchControlClientException(
                    category(result.failureCode()),
                    value(result.failureCode(), "BAT_CONTROL_FAILED"),
                    value(result.failureMessage(), "BAT Owner 호출 실패"),
                    null,
                    null);
        }
        return result.responseBody();
    }

    private BatchControlClientException ownerHttp(WebClientResponseException exception) {
        int status = exception.getStatusCode().value();
        BatchControlClientException.Category category;
        if (status == 400) {
            category = BatchControlClientException.Category.VALIDATION;
        } else if (status == 403) {
            category = BatchControlClientException.Category.PERMISSION;
        } else if (status == 404) {
            category = BatchControlClientException.Category.NOT_FOUND;
        } else if (status == 409) {
            category = BatchControlClientException.Category.CONFLICT;
        } else if (status >= 500) {
            category = BatchControlClientException.Category.UNAVAILABLE;
        } else {
            category = BatchControlClientException.Category.OWNER_ERROR;
        }
        return new BatchControlClientException(
                category,
                "BAT_OWNER_HTTP_" + status,
                "BAT Owner HTTP 오류: " + status,
                null,
                exception);
    }

    private static BatchControlClientException.Category category(String code) {
        String normalized = value(code, "").toUpperCase();
        if (normalized.contains("VALID")) {
            return BatchControlClientException.Category.VALIDATION;
        }
        if (normalized.contains("CONFLICT") || normalized.contains("VERSION")) {
            return BatchControlClientException.Category.CONFLICT;
        }
        if (normalized.contains("PERMISSION") || normalized.contains("AUTH")) {
            return BatchControlClientException.Category.PERMISSION;
        }
        if (normalized.contains("NOT_FOUND")) {
            return BatchControlClientException.Category.NOT_FOUND;
        }
        if (normalized.contains("UNAVAILABLE")
                || normalized.contains("TIMEOUT")
                || normalized.contains("TARGET_DOWN")) {
            return BatchControlClientException.Category.UNAVAILABLE;
        }
        return BatchControlClientException.Category.OWNER_ERROR;
    }

    private static String join(String base, String path) {
        if (!text(base)) {
            throw new BatchControlClientException(
                    BatchControlClientException.Category.UNAVAILABLE,
                    "BAT_SERVICE_UNAVAILABLE",
                    "BAT Service Registry baseUrl이 없습니다.",
                    null,
                    null);
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) + path : base + path;
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(
                BatchRuntimeControlClient.value(value, ""),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    private static boolean text(String value) {
        return value != null && !value.isBlank();
    }

    private static String required(String value, String field) {
        if (!text(value)) {
            throw new IllegalStateException(field + " is required");
        }
        return value.trim();
    }

    private static String value(String value, String defaultValue) {
        return text(value) ? value.trim() : defaultValue;
    }

    private static List<CpfDataRow> rows(Object value) {
        if (value == null) {
            throw new BatchControlClientException(
                    BatchControlClientException.Category.OWNER_ERROR,
                    "BAT_OWNER_EMPTY_RESPONSE",
                    "BAT Owner가 목록 응답 본문을 반환하지 않았습니다.",
                    null,
                    null);
        }
        return CpfDataRow.copyRows(value);
    }

    private record ApprovalContext(String approvalRequestId, String approvalRequesterId) { }

    private static CpfDataRow row(Object value) {
        if (value == null) {
            throw new BatchControlClientException(
                    BatchControlClientException.Category.OWNER_ERROR,
                    "BAT_OWNER_EMPTY_RESPONSE",
                    "BAT Owner가 상세 응답 본문을 반환하지 않았습니다.",
                    null,
                    null);
        }
        return CpfDataRow.copyOf(value);
    }
}
