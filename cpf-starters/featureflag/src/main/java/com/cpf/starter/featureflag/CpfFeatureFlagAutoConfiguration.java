package com.cpf.starter.featureflag;

import dev.openfeature.sdk.OpenFeatureAPI;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OpenFeatureAPI.class)
public class CpfFeatureFlagAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfFeatureFlagService cpfFeatureFlagService() { return new CpfFeatureFlagService(OpenFeatureAPI.getInstance().getClient("cpf")); }
}
