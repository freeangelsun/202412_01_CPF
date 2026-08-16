package com.cpf.integration.graphql;

/** GraphQL Owner 내부의 안전한 operation 메타데이터입니다. Core Context 확장값이 아닙니다. */
public record CpfGraphqlContext(
        String operationId,
        String operationName,
        String operationType,
        String documentSha256,
        int attempt) {
    public CpfGraphqlContext {
        if (attempt < 1) throw new IllegalArgumentException("attempt");
    }
}
