package com.cpf.web.runtime;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import com.cpf.web.api.CpfOnlineTransactionPolicyEvaluator;

/** @CpfController의 구조/Context 정책을 자동 구성합니다. */
@AutoConfiguration
@ConditionalOnClass(HandlerInterceptor.class)
@EnableConfigurationProperties({CpfControllerPolicyProperties.class, CpfDtoValidationProperties.class})
public class CpfControllerPolicyAutoConfiguration {
    @Bean
    CpfOnlineTransactionBeanPostProcessor cpfOnlineTransactionBeanPostProcessor() {
        return new CpfOnlineTransactionBeanPostProcessor();
    }

    @Bean
    CpfOnlineTransactionAspect cpfOnlineTransactionAspect(ObjectProvider<CpfOnlineTransactionPolicyEvaluator> evaluators) {
        return new CpfOnlineTransactionAspect(evaluators.orderedStream().toList());
    }

    @Bean
    CpfControllerPolicyBeanPostProcessor cpfControllerPolicyBeanPostProcessor(CpfControllerPolicyProperties properties) {
        return new CpfControllerPolicyBeanPostProcessor(properties);
    }

    @Bean
    CpfControllerContextInterceptor cpfControllerContextInterceptor(CpfControllerPolicyProperties properties) {
        return new CpfControllerContextInterceptor(properties);
    }

    @Bean
    CpfControllerPolicyWebMvcConfigurer cpfControllerPolicyWebMvcConfigurer(CpfControllerContextInterceptor interceptor) {
        return new CpfControllerPolicyWebMvcConfigurer(interceptor);
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnBean(jakarta.validation.Validator.class)
    CpfDtoValidationAspect cpfDtoValidationAspect(jakarta.validation.Validator validator, CpfDtoValidationProperties properties) {
        return new CpfDtoValidationAspect(validator, properties);
    }
}
