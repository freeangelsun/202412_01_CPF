package com.cpf.starter.foundation.base;

import java.time.Clock;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@EnableConfigurationProperties(CpfBaseProperties.class)
public class CpfBaseAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfCapabilityBindingRegistry cpfCapabilityBindingRegistry() {
        return new CpfCapabilityBindingRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(name = "cpfGeneratedDomainPolicyClock")
    Clock cpfGeneratedDomainPolicyClock() {
        return Clock.systemUTC();
    }

    @Bean
    CpfGeneratedDomainPolicyRuntimeVerifier cpfGeneratedDomainPolicyRuntimeVerifier(
            Clock cpfGeneratedDomainPolicyClock,
            Environment environment) {
        return new CpfGeneratedDomainPolicyRuntimeVerifier(
                Thread.currentThread().getContextClassLoader(),
                cpfGeneratedDomainPolicyClock,
                environment::getProperty);
    }

    @Bean
    SmartInitializingSingleton cpfBaseStartupValidator(
            CpfBaseProperties properties,
            CpfCapabilityBindingRegistry registry,
            CpfGeneratedDomainPolicyRuntimeVerifier generatedDomainPolicyRuntimeVerifier) {
        return () -> {
            properties.validate();
            if (properties.isStrict()) {
                registry.validateAll();
            }
            generatedDomainPolicyRuntimeVerifier.verify();
        };
    }

    @Bean("cpfStarterBaseHealthIndicator")
    HealthIndicator cpfStarterBaseHealthIndicator(
            CpfBaseProperties properties,
            CpfCapabilityBindingRegistry registry,
            CpfGeneratedDomainPolicyRuntimeVerifier generatedDomainPolicyRuntimeVerifier) {
        return () -> {
            CpfGeneratedDomainPolicyRuntimeVerifier.VerificationResult policy =
                    generatedDomainPolicyRuntimeVerifier.lastResult();
            Health.Builder builder = "UP".equals(policy.status()) || "NOT_APPLICABLE".equals(policy.status())
                    ? Health.up() : Health.down();
            return builder
                    .withDetail("profileId", properties.getProfileId())
                    .withDetail("profileVersion", properties.getProfileVersion())
                    .withDetail("capabilityCount", registry.snapshot().size())
                    .withDetail("generatedDomain", policy.generatedDomain())
                    .withDetail("generatedDomainProfile", policy.profile())
                    .withDetail("generatedDomainCapabilities", policy.capabilities())
                    .withDetail("approvedExternalExceptionCount", policy.approvedExceptionCount())
                    .withDetail("approvedExternalExceptionIds", policy.approvedExceptionIds())
                    .withDetail("generatedDomainEnvironment", policy.activeEnvironment())
                    .build();
        };
    }
}
