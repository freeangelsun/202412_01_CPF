package com.cpf.starter.runtime;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** {@code @CpfService}의 3단 Base 구조 검증만 자동 구성합니다. 거래 Context는 실행 Boundary가 소유합니다. */
@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@EnableConfigurationProperties(CpfServicePolicyProperties.class)
public class CpfServicePolicyAutoConfiguration {
    @Bean
    CpfServicePolicyBeanPostProcessor cpfServicePolicyBeanPostProcessor(CpfServicePolicyProperties properties) {
        return new CpfServicePolicyBeanPostProcessor(properties);
    }

}
