package com.cpf.core.service.feature;

import com.cpf.core.api.feature.CpfFeatureFlags;
import com.cpf.core.spi.feature.CpfFeatureFlagProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** 고객 Provider가 없을 때만 CPF safe-default Provider를 구성합니다. */
@Configuration
public class CpfFeatureFlagConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfFeatureFlagProvider.class)
    CpfFeatureFlagProvider cpfPropertyFeatureFlagProvider(Environment environment, ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new CpfPropertyFeatureFlagProvider(environment, objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean(CpfFeatureFlags.class)
    CpfFeatureFlags cpfFeatureFlags(CpfFeatureFlagProvider provider) {
        return new CpfFeatureFlags(provider);
    }
}
