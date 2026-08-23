package com.cpf.starter.runtime;

import com.cpf.starter.async.operation.CpfAsyncOperationAutoConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Provides the Jackson 2 mapper contract consumed by CPF runtime components on Spring Boot 4. */
@AutoConfiguration(before = CpfAsyncOperationAutoConfiguration.class)
@ConditionalOnClass(ObjectMapper.class)
public class CpfJackson2AutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper cpfJackson2ObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
