package com.cpf.external.execution.port;

import com.cpf.external.execution.domain.ExternalEndpointPolicy;

import java.util.Map;

/** 기관별 프로토콜 어댑터가 구현하는 대외 송신·결과 조회 경계입니다. */
public interface ExternalEndpointPort {

    Map<String, Object> execute(
            ExternalEndpointPolicy policy,
            String externalRequestId,
            String idempotencyKey,
            Map<String, Object> payload);

    Map<String, Object> inquire(ExternalEndpointPolicy policy, String externalRequestId);
}
