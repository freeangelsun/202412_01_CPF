package com.cpf.core.common.runtime;

/**
 * runtime health 조회 조건입니다.
 */
public record CpfRuntimeHealthQuery(
        String componentId,
        String componentType,
        String status) {
}
