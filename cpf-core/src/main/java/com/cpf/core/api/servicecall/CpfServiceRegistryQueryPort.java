package com.cpf.core.api.servicecall;

import java.util.List;
import java.util.Map;

/**
 * ADM·Gateway가 내부 저장구조를 모르고 Service Registry를 조회하는 공개 Port입니다.
 *
 * <p>기존 Map 메서드는 내부 Adapter 호환을 위해 유지하되, Public Controller와 신규 Consumer는
 * 반드시 Typed View 메서드를 사용합니다.</p>
 */
public interface CpfServiceRegistryQueryPort {
    List<Map<String, Object>> findServices(String serviceId, String useYn, int limit);
    List<Map<String, Object>> findEndpoints(String serviceId, String endpointCode, String useYn, int limit);
    List<Map<String, Object>> findInstances(String serviceId, String endpointCode, String status, int limit);
    List<Map<String, Object>> findHealth(String serviceId, String endpointCode, int limit);
    List<Map<String, Object>> findRoutingPolicies(String serviceId, String endpointCode, String activeYn, int limit);
    List<Map<String, Object>> findCircuitStates(String serviceId, String endpointCode, int limit);
    List<Map<String, Object>> findCallHistory(String serviceId, String transactionId, int limit);

    default List<CpfServiceRegistryView.Service> services(String serviceId, String useYn, int limit) {
        return findServices(serviceId, useYn, normalizeLimit(limit)).stream().map(CpfServiceRegistryView.Service::from).toList();
    }
    default List<CpfServiceRegistryView.Endpoint> endpoints(String serviceId, String endpointCode, String useYn, int limit) {
        return findEndpoints(serviceId, endpointCode, useYn, normalizeLimit(limit)).stream().map(CpfServiceRegistryView.Endpoint::from).toList();
    }
    default List<CpfServiceRegistryView.Instance> instances(String serviceId, String endpointCode, String status, int limit) {
        return findInstances(serviceId, endpointCode, status, normalizeLimit(limit)).stream().map(CpfServiceRegistryView.Instance::from).toList();
    }
    default List<CpfServiceRegistryView.Health> health(String serviceId, String endpointCode, int limit) {
        return findHealth(serviceId, endpointCode, normalizeLimit(limit)).stream().map(CpfServiceRegistryView.Health::from).toList();
    }
    default List<CpfServiceRegistryView.RoutingPolicy> routingPolicies(String serviceId, String endpointCode, String activeYn, int limit) {
        return findRoutingPolicies(serviceId, endpointCode, activeYn, normalizeLimit(limit)).stream().map(CpfServiceRegistryView.RoutingPolicy::from).toList();
    }
    default List<CpfServiceRegistryView.CircuitState> circuitStates(String serviceId, String endpointCode, int limit) {
        return findCircuitStates(serviceId, endpointCode, normalizeLimit(limit)).stream().map(CpfServiceRegistryView.CircuitState::from).toList();
    }
    default List<CpfServiceRegistryView.CallHistory> callHistory(String serviceId, String transactionId, int limit) {
        return findCallHistory(serviceId, transactionId, normalizeLimit(limit)).stream().map(CpfServiceRegistryView.CallHistory::from).toList();
    }

    private static int normalizeLimit(int limit) {
        if (limit <= 0) return 100;
        return Math.min(limit, 1_000);
    }
}
