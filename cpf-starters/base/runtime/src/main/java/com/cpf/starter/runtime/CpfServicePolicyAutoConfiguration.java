package com.cpf.starter.runtime;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** @CpfService 구조/Context 정책을 자동 구성합니다. */
@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@EnableConfigurationProperties(CpfServicePolicyProperties.class)
public class CpfServicePolicyAutoConfiguration {
    @Bean
    CpfServicePolicyBeanPostProcessor cpfServicePolicyBeanPostProcessor(CpfServicePolicyProperties properties) {
        return new CpfServicePolicyBeanPostProcessor(properties);
    }

    @Bean
    CpfServicePolicyAspect cpfServicePolicyAspect(CpfServicePolicyProperties properties) {
        return new CpfServicePolicyAspect(properties);
    }

}
