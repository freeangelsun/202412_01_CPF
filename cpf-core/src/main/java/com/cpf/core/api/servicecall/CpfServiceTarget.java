package com.cpf.core.api.servicecall;

import java.util.Map;

/** 서비스 레지스트리에서 선택된 호출 대상의 공개 불변 View입니다. */
public record CpfServiceTarget(
        Map<String,Object> service,
        Map<String,Object> endpoint,
        Map<String,Object> instance,
        Map<String,Object> routingPolicy,
        String baseUrl,
        String routingMode) {
    public CpfServiceTarget {
        service = service == null ? Map.of() : Map.copyOf(service);
        endpoint = endpoint == null ? Map.of() : Map.copyOf(endpoint);
        instance = instance == null ? Map.of() : Map.copyOf(instance);
        routingPolicy = routingPolicy == null ? Map.of() : Map.copyOf(routingPolicy);
    }
    public String serviceId() { return value(service, "serviceId"); }
    public String endpointCode() { return value(endpoint, "endpointCode"); }
    public String instanceId() { return value(instance, "instanceId"); }
    public boolean failoverEnabled() { return "Y".equalsIgnoreCase(value(routingPolicy, "failoverEnabledYn")); }
    private static String value(Map<String,Object> source, String key) {
        Object value = source.get(key); return value == null ? null : String.valueOf(value);
    }
}
