package com.cpf.core.common.servicecall;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 서비스 호출 대상의 현재 상태를 표준 형태로 요약합니다.
 *
 * <p>실제 HTTP health check 스케줄링은 후속 단계에서 worker와 연결하고,
 * 현재 단계에서는 레지스트리에 저장된 상태를 호출 엔진/ADM이 같은 의미로 해석하게 합니다.</p>
 */
public class CpfServiceHealthChecker {

    public Map<String, Object> summarize(ServiceCallResolvedTarget target) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serviceId", value(target.service(), "serviceId"));
        result.put("endpointCode", value(target.endpoint(), "endpointCode"));
        result.put("instanceId", value(target.instance(), "instanceId"));
        result.put("instanceStatus", value(target.instance(), "instanceStatus"));
        result.put("routingMode", target.routingMode());
        result.put("baseUrl", target.baseUrl());
        return result;
    }

    private String value(Map<String, Object> row, String key) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
