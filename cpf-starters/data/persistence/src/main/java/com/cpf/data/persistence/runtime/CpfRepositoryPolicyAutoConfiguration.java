package com.cpf.data.persistence.runtime;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@EnableConfigurationProperties(CpfRepositoryPolicyProperties.class)
public class CpfRepositoryPolicyAutoConfiguration {
    @Bean CpfRepositoryPolicyBeanPostProcessor cpfRepositoryPolicyBeanPostProcessor(CpfRepositoryPolicyProperties p){return new CpfRepositoryPolicyBeanPostProcessor(p);}
}
