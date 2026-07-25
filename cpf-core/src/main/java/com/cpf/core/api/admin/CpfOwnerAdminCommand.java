package com.cpf.core.api.admin;

import java.util.Map;

/**
 * ADM이 업무 Owner에게 전달하는 변경 명령 계약입니다.
 *
 * <p>requestUser/reason을 계약에 포함해 Owner가 변경 이력과 감사 연계를 보존할 수 있게 합니다.</p>
 */
public record CpfOwnerAdminCommand(
        String resource,
        String operation,
        String resourceId,
        Map<String, Object> payload,
        String requestUser,
        String reason) {
    public CpfOwnerAdminCommand {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource는 필수입니다.");
        }
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("operation은 필수입니다.");
        }
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
