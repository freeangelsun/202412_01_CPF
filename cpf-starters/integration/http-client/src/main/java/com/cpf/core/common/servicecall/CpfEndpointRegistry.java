package com.cpf.core.common.servicecall;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CPF 서비스 endpoint 정보를 조회하는 레지스트리입니다.
 */
public class CpfEndpointRegistry {
    private final CpfServiceRegistryRepository repository;

    public CpfEndpointRegistry(CpfServiceRegistryRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> findEndpoints(String serviceId, String endpointCode, String useYn, int limit) {
        return repository.findEndpoints(serviceId, endpointCode, useYn, limit);
    }

    public Optional<Map<String, Object>> findEndpoint(String serviceId, String endpointCode) {
        return repository.findEndpoints(serviceId, endpointCode, "Y", 1).stream().findFirst();
    }
}
