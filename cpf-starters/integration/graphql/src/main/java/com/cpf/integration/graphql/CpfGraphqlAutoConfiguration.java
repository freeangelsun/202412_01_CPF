package com.cpf.integration.graphql;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.server.WebGraphQlInterceptor;

/** Spring for GraphQL 안전 기본값과 CPF authorization/audit/telemetry/DataLoader hook을 연결한다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfGraphqlProperties.class)
@ConditionalOnClass(WebGraphQlInterceptor.class)
@ConditionalOnProperty(prefix="cpf.integration.graphql",name="enabled",havingValue="true")
public class CpfGraphqlAutoConfiguration {
    @Bean @ConditionalOnMissingBean CpfGraphqlRateLimiter cpfGraphqlRateLimiter(){return new CpfGraphqlRateLimiter();}
    @Bean @ConditionalOnMissingBean CpfGraphqlAuthorizationPolicy cpfGraphqlAuthorizationPolicy(){return CpfGraphqlAuthorizationPolicy.authenticatedTenantPolicy();}
    @Bean @ConditionalOnMissingBean CpfGraphqlAuditSink cpfGraphqlAuditSink(){return CpfGraphqlAuditSink.noop();}
    @Bean @ConditionalOnMissingBean CpfGraphqlTelemetry cpfGraphqlTelemetry(){return CpfGraphqlTelemetry.noop();}
    @Bean @ConditionalOnMissingBean CpfGraphqlExceptionResolver cpfGraphqlExceptionResolver(){return new CpfGraphqlExceptionResolver();}
    @Bean CpfGraphqlGuardInterceptor cpfGraphqlGuardInterceptor(CpfGraphqlProperties p,CpfGraphqlRateLimiter r,CpfGraphqlAuthorizationPolicy a,CpfGraphqlAuditSink audit,CpfGraphqlTelemetry telemetry){return new CpfGraphqlGuardInterceptor(p,r,a,audit,telemetry);}
    @Bean @ConditionalOnMissingBean(Instrumentation.class) Instrumentation cpfGraphqlSafetyInstrumentation(CpfGraphqlProperties p){return new ChainedInstrumentation(List.of(new MaxQueryDepthInstrumentation(p.getMaxDepth()),new MaxQueryComplexityInstrumentation(p.getMaxComplexity())));}
    @Bean Object cpfGraphqlBatchLoaders(BatchLoaderRegistry registry,ObjectProvider<CpfGraphqlBatchLoaderRegistrar> registrars){registrars.orderedStream().forEach(r->r.register(registry));return new Object();}
}
