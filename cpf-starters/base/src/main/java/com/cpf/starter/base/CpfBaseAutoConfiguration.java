package com.cpf.starter.base;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CpfBaseProperties.class)
public class CpfBaseAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfCapabilityBindingRegistry cpfCapabilityBindingRegistry() {
        return new CpfCapabilityBindingRegistry();
    }

    @Bean
    SmartInitializingSingleton cpfBaseStartupValidator(
            CpfBaseProperties properties,
            CpfCapabilityBindingRegistry registry) {
        return () -> {
            properties.validate();
            if (properties.isStrict()) registry.validateAll();
        };
    }

    @Bean("cpfStarterBaseHealthIndicator")
    HealthIndicator cpfStarterBaseHealthIndicator(CpfBaseProperties properties, CpfCapabilityBindingRegistry registry) {
        return () -> Health.up()
                .withDetail("profileId", properties.getProfileId())
                .withDetail("profileVersion", properties.getProfileVersion())
                .withDetail("capabilityCount", registry.snapshot().size())
                .build();
    }
}
