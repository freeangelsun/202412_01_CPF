package com.cpf.integration.graphql;

import com.cpf.core.api.context.CpfContext;

/** GraphQL operation/field authorization을 업무 권한 Provider와 연결하는 계약. */
public interface CpfGraphqlAuthorizationPolicy {
    boolean authorizeOperation(CpfContext context, String operationName, String documentSha256);
    boolean authorizeField(CpfContext context, String fieldPath);

    static CpfGraphqlAuthorizationPolicy authenticatedTenantPolicy() {
        return new CpfGraphqlAuthorizationPolicy() {
            @Override
            public boolean authorizeOperation(CpfContext context, String operationName, String documentSha256) {
                return context != null && context.subjectId() != null && !context.subjectId().isBlank()
                        && context.tenantId() != null && !context.tenantId().isBlank();
            }
            @Override
            public boolean authorizeField(CpfContext context, String fieldPath) {
                return authorizeOperation(context, null, null);
            }
        };
    }
}
