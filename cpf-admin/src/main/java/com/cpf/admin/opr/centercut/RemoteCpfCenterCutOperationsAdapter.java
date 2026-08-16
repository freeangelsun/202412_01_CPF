package com.cpf.admin.opr.centercut;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.batch.api.CpfBatchOwnerUnknownResultException;
import com.cpf.batch.api.CpfCenterCutOperationsPort;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import com.cpf.integration.api.servicecall.CpfServiceRequest;
import com.cpf.integration.api.servicecall.CpfServiceResult;
import com.cpf.web.api.CpfHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 분리 WAS topology에서 ADM이 BAT Center-Cut Owner 조회 계약을 호출합니다. */
public class RemoteCpfCenterCutOperationsAdapter implements CpfCenterCutOperationsPort {
    private static final String SERVICE_ID = "BAT";
    private static final String ENDPOINT_CODE = "SBATCT0001";
    private static final String CALLER_SERVICE = "ADM";

    private final CpfServiceCaller serviceCaller;
    private final WebClient webClient;
    private final AdmAuthenticatedOperatorContext operatorContext;
    private final String callerInstanceId;

    public RemoteCpfCenterCutOperationsAdapter(
            CpfServiceCaller serviceCaller,
            WebClient.Builder webClientBuilder,
            AdmAuthenticatedOperatorContext operatorContext,
            String callerInstanceId) {
        this.serviceCaller = serviceCaller;
        this.webClient = webClientBuilder.build();
        this.operatorContext = operatorContext;
        this.callerInstanceId = requireText(callerInstanceId, "callerInstanceId");
    }

    private Object call(String operation, Map<String, Object> payload) {
        String operatorId = requireText(operatorContext.currentOperatorId(), "operatorId");
        String path = "/bat/internal/center-cut/" + operation;
        CpfServiceRequest request = CpfServiceRequest.builder(SERVICE_ID)
                .endpointCode(ENDPOINT_CODE)
                .httpMethod("POST")
                .requestPath(path)
                .header(CpfHeaders.callerService(), CALLER_SERVICE)
                .header(CpfHeaders.callerInstanceId(), callerInstanceId)
                .header(CpfHeaders.operatorId(), operatorId)
                .attribute("ownerDomain", "BAT")
                .attribute("callerDomain", "ADM")
                .build();
        CpfServiceResult<Object> result = serviceCaller.invoke(
                request,
                target -> webClient.post()
                        .uri(join(target.baseUrl(), path))
                        .headers(headers -> {
                            headers.set(CpfHeaders.callerService(), CALLER_SERVICE);
                            headers.set(CpfHeaders.callerInstanceId(), callerInstanceId);
                            headers.set(CpfHeaders.operatorId(), operatorId);
                        })
                        .bodyValue(payload == null ? Map.of() : payload)
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block());
        if (result.unknown()) {
            throw new CpfBatchOwnerUnknownResultException(
                    result.failureCode(),
                    "BAT Center-Cut 결과불명: " + result.failureMessage());
        }
        if (!"SUCCESS".equals(result.status())) {
            throw new IllegalStateException("BAT Center-Cut 호출 실패: " + result.failureMessage());
        }
        return result.responseBody();
    }

    private static String join(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("BAT baseUrl missing");
        }
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1) + path
                : baseUrl + path;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(field + " is required");
        }
        return value.trim();
    }

    private static Map<String, Object> parameters(Object... pairs) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            if (pairs[index + 1] != null) {
                values.put(String.valueOf(pairs[index]), pairs[index + 1]);
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(Object value) {
        return value == null ? List.of() : (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value == null ? Map.of() : (Map<String, Object>) value;
    }

    @Override
    public List<Map<String, Object>> findJobs() {
        return list(call("findJobs", Map.of()));
    }

    @Override
    public Map<String, Object> findJobDetail(String centerCutJobId) {
        return map(call("findJobDetail", parameters("centerCutJobId", centerCutJobId)));
    }

    @Override
    public List<Map<String, Object>> findParameters(String centerCutJobId) {
        return list(call("findParameters", parameters("centerCutJobId", centerCutJobId)));
    }

    @Override
    public Map<String, Object> findSummary(String centerCutJobId) {
        return map(call("findSummary", parameters("centerCutJobId", centerCutJobId)));
    }

    @Override
    public List<Map<String, Object>> findTargets(String centerCutJobId, String statusCode, int limit) {
        return list(call(
                "findTargets",
                parameters("centerCutJobId", centerCutJobId, "statusCode", statusCode, "limit", limit)));
    }

    @Override
    public List<Map<String, Object>> findResults(String centerCutJobId, String resultStatus, int limit) {
        return list(call(
                "findResults",
                parameters("centerCutJobId", centerCutJobId, "resultStatus", resultStatus, "limit", limit)));
    }

    @Override
    public Map<String, Object> findResultDetail(String resultId) {
        return map(call("findResultDetail", parameters("resultId", resultId)));
    }
}
