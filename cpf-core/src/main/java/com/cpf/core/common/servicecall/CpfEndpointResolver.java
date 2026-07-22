package cpf.pfw.common.servicecall;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 서비스 ID와 endpoint code를 실제 호출 대상으로 해석합니다.
 */
public class CpfEndpointResolver {
    private final CpfServiceRegistry serviceRegistry;
    private final CpfEndpointRegistry endpointRegistry;
    private final CpfServiceInstanceRegistry instanceRegistry;
    private final CpfRoutingPolicyResolver routingPolicyResolver;
    private final CpfHealthAwareInstanceSelector instanceSelector;

    public CpfEndpointResolver(
            CpfServiceRegistry serviceRegistry,
            CpfEndpointRegistry endpointRegistry,
            CpfServiceInstanceRegistry instanceRegistry,
            CpfRoutingPolicyResolver routingPolicyResolver,
            CpfHealthAwareInstanceSelector instanceSelector) {
        this.serviceRegistry = serviceRegistry;
        this.endpointRegistry = endpointRegistry;
        this.instanceRegistry = instanceRegistry;
        this.routingPolicyResolver = routingPolicyResolver;
        this.instanceSelector = instanceSelector;
    }

    public ServiceCallResolvedTarget resolve(ServiceCallRequest request) {
        return resolve(request, Set.of());
    }

    public ServiceCallResolvedTarget resolve(ServiceCallRequest request, Set<String> excludedInstanceIds) {
        String serviceId = requireText(request.serviceId(), "serviceId").toUpperCase();
        Map<String, Object> service = serviceRegistry.findService(serviceId)
                .orElseThrow(() -> new IllegalStateException("PFW 서비스 레지스트리에 서비스가 없습니다. serviceId=" + serviceId));
        Map<String, Object> endpoint = endpointRegistry.findEndpoint(serviceId, request.endpointCode())
                .orElseThrow(() -> new IllegalStateException("PFW 서비스 endpoint가 없습니다. serviceId=" + serviceId));
        String endpointCode = String.valueOf(endpoint.get("endpointCode"));
        Map<String, Object> policy = routingPolicyResolver.resolve(serviceId, endpointCode);
        List<Map<String, Object>> instances = instanceRegistry.findInstances(serviceId, endpointCode, null, 100);
        Map<String, Object> instance = instanceSelector.select(instances, request.instanceId(), excludedInstanceIds).orElse(Map.of());
        String baseUrl = firstText(value(instance, "baseUrl"), value(endpoint, "baseUrl"));
        return new ServiceCallResolvedTarget(service, endpoint, instance, policy, baseUrl, value(policy, "routingMode"));
    }

    private String value(Map<String, Object> row, String key) {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String firstText(String first, String fallback) {
        return first != null && !first.isBlank() ? first : fallback;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 값은 필수입니다.");
        }
        return value.trim();
    }
}
