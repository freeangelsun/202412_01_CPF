package com.cpf.external.execution.adapter;

import com.cpf.core.common.exception.CpfExternalServiceException;
import com.cpf.core.common.header.CpfHeaderNames;
import com.cpf.core.common.http.CpfWebClient;
import com.cpf.core.common.servicecall.CpfServiceCallException;
import com.cpf.core.common.servicecall.ServiceCallRequest;
import com.cpf.core.common.servicecall.ServiceCallResult;
import com.cpf.external.execution.domain.ExternalEndpointPolicy;
import com.cpf.external.execution.port.ExternalEndpointPort;
import com.cpf.external.execution.port.ExternalUnknownResultException;
import org.springframework.stereotype.Component;

import java.util.Map;

/** CPF Service Call Engine을 통해 endpoint 정책과 거래 헤더를 적용하는 REST 어댑터입니다. */
@Component
@SuppressWarnings("unchecked")
public class CpfExternalEndpointAdapter implements ExternalEndpointPort {

    private final CpfWebClient webClient;

    public CpfExternalEndpointAdapter(CpfWebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Map<String, Object> execute(
            ExternalEndpointPolicy policy,
            String externalRequestId,
            String idempotencyKey,
            Map<String, Object> payload) {
        ServiceCallRequest request = baseRequest(policy, policy.requestPath())
                .httpMethod("POST")
                .header("Idempotency-Key", idempotencyKey)
                .header("X-External-Request-Id", externalRequestId)
                .build();
        try {
            return webClient.post(request, payload, Map.class);
        } catch (CpfServiceCallException exception) {
            throw translate(exception);
        } catch (RuntimeException exception) {
            throw new ExternalUnknownResultException("EXS-TRANSPORT-UNKNOWN", "대외 호출 결과를 확정하지 못했습니다.", exception);
        }
    }

    @Override
    public Map<String, Object> inquire(ExternalEndpointPolicy policy, String externalRequestId) {
        if (policy.resultQueryPath() == null || policy.resultQueryPath().isBlank()) {
            throw new IllegalStateException("결과 조회 경로가 등록되지 않은 endpoint입니다.");
        }
        ServiceCallRequest request = baseRequest(
                policy,
                policy.resultQueryPath().replace("{externalRequestId}", externalRequestId))
                .httpMethod("GET")
                .header("X-External-Request-Id", externalRequestId)
                .build();
        return webClient.get(request, Map.class);
    }

    private ServiceCallRequest.Builder baseRequest(ExternalEndpointPolicy policy, String path) {
        return ServiceCallRequest.builder(policy.serviceId())
                .endpointCode(policy.endpointCode())
                .requestPath(path)
                .timeoutMillis(policy.timeoutMillis())
                .retryCount(policy.retryCount())
                .header(CpfHeaderNames.CALLER_SERVICE, "EXS")
                .attribute("institutionCode", policy.institutionCode());
    }

    private RuntimeException translate(CpfServiceCallException exception) {
        ServiceCallResult<?> result = exception.getResult();
        if (result != null && "UNKNOWN".equals(result.status())) {
            return new ExternalUnknownResultException(result.failureCode(), result.failureMessage(), exception);
        }
        return new CpfExternalServiceException("대외기관 호출이 실패했습니다.", exception);
    }
}
