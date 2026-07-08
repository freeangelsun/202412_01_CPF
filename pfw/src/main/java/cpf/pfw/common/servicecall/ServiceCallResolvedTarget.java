package cpf.pfw.common.servicecall;

import java.util.Map;

/**
 * 서비스 레지스트리 기준으로 선택된 실제 호출 대상입니다.
 */
public record ServiceCallResolvedTarget(
        Map<String, Object> service,
        Map<String, Object> endpoint,
        Map<String, Object> instance,
        Map<String, Object> routingPolicy,
        String baseUrl,
        String routingMode) {

    public String serviceId() {
        return value(service, "serviceId");
    }

    public String endpointCode() {
        return value(endpoint, "endpointCode");
    }

    public String instanceId() {
        return value(instance, "instanceId");
    }

    public boolean failoverEnabled() {
        return "Y".equalsIgnoreCase(value(routingPolicy, "failoverEnabledYn"));
    }

    private String value(Map<String, Object> row, String key) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        Object found = row.get(key);
        return found == null ? null : String.valueOf(found);
    }
}
