package com.cpf.gateway.context;

/** Gateway Owner가 보유하는 route/target 메타데이터입니다. Core Context generic component가 아닙니다. */
public record CpfGatewayContext(
        String ingressType,
        String routeId,
        String routeVersion,
        String targetService,
        String gatewayInstanceId,
        String trustLevel,
        String authenticatedSubject,
        String tenantId) {
}
