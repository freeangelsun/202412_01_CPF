package com.cpf.data.persistence.runtime;
import com.cpf.data.persistence.api.transaction.CpfTransactionOperations;
import com.cpf.data.persistence.internal.transaction.SpringCpfTransactionOperations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@EnableConfigurationProperties(CpfRepositoryPolicyProperties.class)
public class CpfRepositoryPolicyAutoConfiguration {
    @Bean CpfRepositoryPolicyBeanPostProcessor cpfRepositoryPolicyBeanPostProcessor(CpfRepositoryPolicyProperties p){return new CpfRepositoryPolicyBeanPostProcessor(p);}

    @Bean
    @ConditionalOnMissingBean(CpfTransactionOperations.class)
    CpfTransactionOperations cpfTransactionOperations() {
        return new SpringCpfTransactionOperations();
    }
}
