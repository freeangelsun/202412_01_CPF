package com.cpf.core.common.gateway;

import com.cpf.core.common.execution.CpfExecutionDefinition;

/** CPF Gateway가 immutable snapshot에 보관하는 경량 route입니다. */
public record CpfGatewayRoute(
        String standardExecutionId,
        String serviceId,
        String httpMethod,
        String endpoint,
        String operationId,
        String requiredPermission,
        boolean auditReasonRequired,
        String routeVersion) {

    public static CpfGatewayRoute from(CpfExecutionDefinition definition) {
        return new CpfGatewayRoute(
                definition.standardExecutionId(),
                definition.sourceModule(),
                definition.httpMethod(),
                definition.endpoint(),
                definition.operationId(),
                definition.requiredPermission(),
                definition.auditReasonRequired(),
                definition.sourceVersion());
    }
}
