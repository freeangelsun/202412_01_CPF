package com.cpf.core.api.servicecall;

import java.util.List;
import java.util.Map;

/**
 * ADM 등 운영 모듈이 CPF 서비스 레지스트리 내부 저장구조를 모르고 조회하기 위한 공개 포트입니다.
 */
public interface CpfServiceRegistryQueryPort {
    List<Map<String, Object>> findServices(String serviceId, String useYn, int limit);

    List<Map<String, Object>> findEndpoints(String serviceId, String endpointCode, String useYn, int limit);

    List<Map<String, Object>> findInstances(String serviceId, String endpointCode, String status, int limit);

    List<Map<String, Object>> findHealth(String serviceId, String endpointCode, int limit);

    List<Map<String, Object>> findRoutingPolicies(String serviceId, String endpointCode, String activeYn, int limit);

    List<Map<String, Object>> findCircuitStates(String serviceId, String endpointCode, int limit);

    List<Map<String, Object>> findCallHistory(String serviceId, String transactionGlobalId, int limit);
}
