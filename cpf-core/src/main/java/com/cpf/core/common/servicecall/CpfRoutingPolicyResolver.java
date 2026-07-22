package cpf.pfw.common.servicecall;

import java.util.List;
import java.util.Map;

/**
 * 서비스별 라우팅 정책을 결정합니다.
 */
public class CpfRoutingPolicyResolver {
    private final CpfServiceRegistryRepository repository;

    public CpfRoutingPolicyResolver(CpfServiceRegistryRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> resolve(String serviceId, String endpointCode) {
        List<Map<String, Object>> policies = repository.findRoutingPolicies(serviceId, endpointCode, "Y", 1);
        return policies.isEmpty() ? Map.of("routingMode", "PRIMARY", "loadBalanceType", "WEIGHT") : policies.get(0);
    }
}
