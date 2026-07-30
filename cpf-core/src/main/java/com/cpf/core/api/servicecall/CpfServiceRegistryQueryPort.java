package com.cpf.core.api.servicecall;

import java.util.List;

/**
 * Service Registry의 topology-independent Typed Query Port입니다.
 *
 * <p>DB 컬럼 Map은 Owner 내부 Repository 경계 밖으로 노출하지 않습니다.
 * 필수 컬럼 누락과 잘못된 Code는 조회 성공으로 보정하지 않고 계약 오류로 실패합니다.</p>
 */
public interface CpfServiceRegistryQueryPort {
    List<CpfServiceRegistryView.Service> services(String serviceId, String useYn, int limit);
    List<CpfServiceRegistryView.Endpoint> endpoints(String serviceId, String endpointCode, String useYn, int limit);
    List<CpfServiceRegistryView.Instance> instances(String serviceId, String endpointCode, String status, int limit);
    List<CpfServiceRegistryView.Health> health(String serviceId, String endpointCode, int limit);
    List<CpfServiceRegistryView.RoutingPolicy> routingPolicies(
            String serviceId, String endpointCode, String activeYn, int limit);
    List<CpfServiceRegistryView.CircuitState> circuitStates(String serviceId, String endpointCode, int limit);
    List<CpfServiceRegistryView.CallHistory> callHistory(String serviceId, String transactionId, int limit);

    static int normalizeLimit(int limit) {
        if (limit <= 0) return 100;
        return Math.min(limit, 1_000);
    }
}
