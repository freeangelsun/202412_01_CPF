package com.cpf.starter.integration.graphql;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.server.WebGraphQlInterceptor;

@AutoConfiguration
@EnableConfigurationProperties(CpfGraphqlProperties.class)
@ConditionalOnClass(WebGraphQlInterceptor.class)
@ConditionalOnProperty(prefix = "cpf.integration.graphql", name = "enabled", havingValue = "true")
public class CpfGraphqlAutoConfiguration {
    @Bean
    CpfGraphqlGuardInterceptor cpfGraphqlGuardInterceptor(CpfGraphqlProperties properties) {
        return new CpfGraphqlGuardInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean(Instrumentation.class)
    Instrumentation cpfGraphqlSafetyInstrumentation(CpfGraphqlProperties properties) {
        return new ChainedInstrumentation(List.of(
                new MaxQueryDepthInstrumentation(properties.getMaxDepth()),
                new MaxQueryComplexityInstrumentation(properties.getMaxComplexity())));
    }
}
