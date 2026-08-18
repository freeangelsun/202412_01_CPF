package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.domain.CpfDomainBindingResolver;
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.integration.api.domaincall.CpfDomainOperationRegistry;
import com.cpf.integration.api.domaincall.CpfDomainRemoteTransport;
import com.cpf.integration.http.internal.CpfWebClient;
import com.cpf.web.context.CpfHttpOutboundContextAdapter;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Typed Domain Client의 Local/Remote Runtime 경로를 실제 Bean으로 연결합니다. */
@AutoConfiguration(afterName = "com.cpf.integration.http.internal.servicecall.CpfServiceCallAutoConfiguration")
@EnableConfigurationProperties(CpfDomainCallProperties.class)
public class CpfDomainCallAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfDomainBindingResolver cpfDomainBindingResolver(CpfDomainCallProperties properties) {
        return new CpfConfiguredDomainBindingResolver(properties);
    }
    @Bean @ConditionalOnMissingBean(CpfDomainOperationRegistry.class)
    CpfDefaultDomainOperationRegistry cpfDomainOperationRegistry(List<CpfDomainOperation<?, ?>> operations) {
        return new CpfDefaultDomainOperationRegistry(operations);
    }
    @Bean @ConditionalOnMissingBean(CpfDomainRemoteTransport.class)
    CpfDomainRemoteTransport cpfDomainRemoteTransport(CpfWebClient webClient, ObjectMapper objectMapper,
                                                       CpfHttpOutboundContextAdapter outboundHeaders) {
        return new CpfHttpDomainRemoteTransport(webClient, objectMapper, outboundHeaders);
    }
    @Bean @ConditionalOnMissingBean(CpfDomainClientRouter.class)
    CpfDomainClientRouter cpfDomainClientRouter(
            CpfDomainBindingResolver resolver, CpfDomainOperationRegistry registry, CpfDomainRemoteTransport remoteTransport) {
        return new CpfDomainClientRouter(resolver, registry, remoteTransport);
    }
    @Bean @ConditionalOnMissingBean
    CpfDomainInvocationGuard cpfDomainInvocationGuard(List<CpfOperationAccessPolicy> policies, CpfRuntimeIdentity runtime) {
        return new CpfDomainInvocationGuard(policies, runtime);
    }
    @Bean @ConditionalOnMissingBean(CpfDomainCallController.class)
    CpfDomainCallController cpfDomainCallController(CpfDefaultDomainOperationRegistry registry, ObjectMapper objectMapper,
            CpfDomainInvocationGuard invocationGuard) {
        return new CpfDomainCallController(registry, objectMapper, invocationGuard);
    }
}
