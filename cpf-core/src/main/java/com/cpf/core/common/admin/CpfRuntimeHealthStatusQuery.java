package com.cpf.core.common.admin;

/**
 * ADM runtime health 관제 후보 조회 조건입니다.
 */
public record CpfRuntimeHealthStatusQuery(
        String componentId,
        String componentType,
        String status) {
}
