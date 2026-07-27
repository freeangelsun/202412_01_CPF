package com.cpf.core.api.gateway;

/** CPF Gateway가 사용하는 topology-independent 공개 route 계약입니다. */
public record CpfGatewayRoute(
        String standardExecutionId,
        String serviceId,
        String httpMethod,
        String endpoint,
        String operationId,
        String requiredPermission,
        boolean auditReasonRequired,
        String routeVersion) {
}
