package com.cpf.core.common.servicecall;

import com.cpf.core.api.servicecall.CpfServiceRegistryQueryPort;

import java.util.List;
import java.util.Map;

/**
 * CPF 서비스 레지스트리 저장소를 공개 운영 조회 포트로 노출하는 파사드입니다.
 */
public class CpfServiceRegistryQueryFacade implements CpfServiceRegistryQueryPort {
    private final CpfServiceRegistryRepository repository;

    public CpfServiceRegistryQueryFacade(CpfServiceRegistryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> findServices(String serviceId, String useYn, int limit) {
        return repository.findServices(serviceId, useYn, limit);
    }

    @Override
    public List<Map<String, Object>> findEndpoints(String serviceId, String endpointCode, String useYn, int limit) {
        return repository.findEndpoints(serviceId, endpointCode, useYn, limit);
    }

    @Override
    public List<Map<String, Object>> findInstances(String serviceId, String endpointCode, String status, int limit) {
        return repository.findInstances(serviceId, endpointCode, status, limit);
    }

    @Override
    public List<Map<String, Object>> findHealth(String serviceId, String endpointCode, int limit) {
        return repository.findHealthStatuses(serviceId, endpointCode, limit);
    }

    @Override
    public List<Map<String, Object>> findRoutingPolicies(String serviceId, String endpointCode, String activeYn, int limit) {
        return repository.findRoutingPolicies(serviceId, endpointCode, activeYn, limit);
    }

    @Override
    public List<Map<String, Object>> findCircuitStates(String serviceId, String endpointCode, int limit) {
        return repository.findCircuitStates(serviceId, endpointCode, limit);
    }

    @Override
    public List<Map<String, Object>> findCallHistory(String serviceId, String transactionGlobalId, int limit) {
        return repository.findCallHistory(serviceId, transactionGlobalId, limit);
    }
}
