package com.cpf.integration.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;

/** Resolver 예외 원문/stack/secret을 Client에 노출하지 않는 기본 masking resolver. */
public final class CpfGraphqlExceptionResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable exception, DataFetchingEnvironment environment) {
        return GraphqlErrorBuilder.newError(environment)
                .message("GraphQL request failed")
                .errorType(ErrorType.INTERNAL_ERROR)
                .build();
    }
}
