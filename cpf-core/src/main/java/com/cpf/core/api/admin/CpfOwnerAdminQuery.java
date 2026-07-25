package com.cpf.core.api.admin;

import java.util.Map;

/**
 * ADM이 업무 Owner에게 전달하는 topology-independent 조회 계약입니다.
 *
 * <p>ADM은 Owner DB schema나 Mapper를 알지 않으며, resource/operation/criteria만 전달합니다.
 * 동일 JVM에서는 Port 구현 Bean을, 분리 WAS에서는 동일 계약의 Remote Adapter를 사용합니다.</p>
 */
public record CpfOwnerAdminQuery(
        String resource,
        String operation,
        String resourceId,
        Map<String, Object> criteria) {
    public CpfOwnerAdminQuery {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource는 필수입니다.");
        }
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("operation은 필수입니다.");
        }
        criteria = criteria == null ? Map.of() : Map.copyOf(criteria);
    }
}
