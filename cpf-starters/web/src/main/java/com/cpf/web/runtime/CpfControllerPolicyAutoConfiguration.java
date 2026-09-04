package com.cpf.web.runtime;

import com.cpf.web.context.CpfOperationIdResolver;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.context.CpfRequestOperationResolver;
import com.cpf.web.context.CpfOperationOwnerResolver;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.core.env.Environment;

/** @CpfRestController structural/context/protocol policy. */
@AutoConfiguration
@ConditionalOnClass(HandlerInterceptor.class)
@EnableConfigurationProperties({CpfControllerPolicyProperties.class, CpfDtoValidationProperties.class})
@Import(CpfControllerPolicyWebMvcConfigurer.class)
public class CpfControllerPolicyAutoConfiguration {
    @Bean CpfOnlineTransactionBeanPostProcessor cpfOnlineTransactionBeanPostProcessor() { return new CpfOnlineTransactionBeanPostProcessor(); }
    @Bean CpfOnlineTransactionAspect cpfOnlineTransactionAspect() { return new CpfOnlineTransactionAspect(); }
    @Bean CpfControllerPolicyBeanPostProcessor cpfControllerPolicyBeanPostProcessor(CpfControllerPolicyProperties properties) {
        return new CpfControllerPolicyBeanPostProcessor(properties);
    }
    @Bean CpfOperationIdResolver cpfOperationIdResolver() { return new CpfOperationIdResolver(); }
    @Bean CpfOperationOwnerResolver cpfOperationOwnerResolver() { return new CpfClasspathOperationOwnerResolver(); }
    @Bean CpfControllerContextInterceptor cpfControllerContextInterceptor(CpfControllerPolicyProperties properties,
            CpfOperationIdResolver operationIds, CpfRuntimeIdentity runtime, ObjectProvider<CpfOperationAccessPolicy> accessPolicies,
            ObjectProvider<CpfRequestOperationResolver> requestOperationResolvers,
            ObjectProvider<CpfOperationOwnerResolver> operationOwnerResolvers) {
        return new CpfControllerContextInterceptor(properties, operationIds, runtime, accessPolicies.orderedStream().toList(),
                requestOperationResolvers.orderedStream().toList(), operationOwnerResolvers.orderedStream().toList());
    }

    @Bean CpfOperationCatalogBootstrap cpfOperationCatalogBootstrap(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings, CpfRuntimeIdentity runtime,
            Environment environment, ObjectProvider<CpfOperationCatalogRegistry> registries,
            CpfOperationOwnerResolver operationOwners) {
        return new CpfOperationCatalogBootstrap(mappings, runtime, environment, registries.orderedStream().toList(),
                new CpfBusinessOperationManifestVerifier(), operationOwners);
    }

    @Bean @ConditionalOnBean(jakarta.validation.Validator.class)
    CpfDtoValidationAspect cpfDtoValidationAspect(jakarta.validation.Validator validator, CpfDtoValidationProperties properties) {
        return new CpfDtoValidationAspect(validator, properties);
    }
}
