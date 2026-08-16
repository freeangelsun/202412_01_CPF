package com.cpf.integration.graphql;

import org.springframework.graphql.execution.BatchLoaderRegistry;

/** N+1 방지를 위한 DataLoader를 capability consumer가 등록하는 확장 계약. */
@FunctionalInterface
public interface CpfGraphqlBatchLoaderRegistrar {
    void register(BatchLoaderRegistry registry);
}
