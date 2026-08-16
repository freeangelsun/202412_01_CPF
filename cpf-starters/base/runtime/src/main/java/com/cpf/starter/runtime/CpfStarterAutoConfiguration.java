package com.cpf.starter.runtime;

import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.message.CpfMessageResolver;
import com.cpf.foundation.message.DefaultCpfMessageResolver;
import com.cpf.foundation.version.CpfPlatformVersionLoader;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.id.DefaultCpfTransactionIdGenerator;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.starter.internal.context.CpfStarterContextRuntime;
import com.cpf.core.api.config.CpfConfigCatalog;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.env.Environment;

/**
 * 모든 CPF Application의 경량 공통 Runtime을 조립합니다.
 * DB/Broker/Cache/Object Storage/SSO/OIDC/XA/JTA Provider는 의도적으로 포함하지 않습니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfStarterProperties.class)
public class CpfStarterAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfCapabilityBindingRegistry cpfCapabilityBindingRegistry() { return new CpfCapabilityBindingRegistry(); }

    @Bean @ConditionalOnMissingBean(name = "cpfStarterClock")
    Clock cpfStarterClock() { return Clock.systemUTC(); }

    @Bean @ConditionalOnMissingBean
    CpfGeneratedDomainPolicyRuntimeVerifier cpfGeneratedDomainPolicyRuntimeVerifier(Clock cpfStarterClock, Environment environment) {
        return new CpfGeneratedDomainPolicyRuntimeVerifier(Thread.currentThread().getContextClassLoader(),
                cpfStarterClock, environment::getProperty);
    }

    @Bean
    SmartInitializingSingleton cpfStarterStartupValidator(CpfStarterProperties properties,
            CpfCapabilityBindingRegistry registry, CpfGeneratedDomainPolicyRuntimeVerifier verifier) {
        return () -> { if (properties.isStrict()) registry.validateAll(); verifier.verify(); };
    }

    @Bean @ConditionalOnMissingBean(CpfConfigCatalog.class)
    CpfConfigCatalog cpfConfigCatalog(ApplicationContext context) { return new CpfConfigurationPolicyCatalog(context); }

    @Bean @ConditionalOnMissingBean(CpfTransactionIdGenerator.class)
    DefaultCpfTransactionIdGenerator cpfTransactionIdGenerator(Clock cpfStarterClock, Environment environment) {
        return new DefaultCpfTransactionIdGenerator(environment, cpfStarterClock);
    }

    @Bean @ConditionalOnMissingBean
    CpfExecutionIdGenerator cpfExecutionIdGenerator() {
        return new CpfExecutionIdGenerator() {
            @Override public String newExecutionId() { return "EX-" + UUID.randomUUID(); }
            @Override public String newSegmentId() { return "SG-" + UUID.randomUUID(); }
        };
    }

    @Bean @ConditionalOnMissingBean
    CpfBusinessDateProvider cpfBusinessDateProvider(Clock cpfStarterClock) { return () -> LocalDate.now(cpfStarterClock); }

    @Bean @ConditionalOnMissingBean
    CpfContextExecutionFactory cpfContextExecutionFactory(CpfTransactionIdGenerator transactionIds,
            CpfExecutionIdGenerator executionIds, CpfBusinessDateProvider businessDates, Clock cpfStarterClock) {
        return new CpfContextExecutionFactory(transactionIds, executionIds, businessDates, cpfStarterClock);
    }


    @Bean @ConditionalOnMissingBean
    CpfPlatformVersionLoader cpfPlatformVersionLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = CpfStarterAutoConfiguration.class.getClassLoader();
        return new CpfPlatformVersionLoader(loader);
    }

    @Bean @ConditionalOnMissingBean
    CpfMessageResolver cpfMessageResolver() { return new DefaultCpfMessageResolver(); }

    @Bean @ConditionalOnMissingBean
    CpfLoggingAspect cpfLoggingAspect(CpfStarterProperties properties) { return new CpfLoggingAspect(properties); }

    @Bean @ConditionalOnMissingBean
    CpfPerformanceAspect cpfPerformanceAspect(CpfStarterProperties properties, MeterRegistry meterRegistry) {
        return new CpfPerformanceAspect(properties, meterRegistry);
    }


    @Bean @ConditionalOnMissingBean
    CpfStarterDiagnostics cpfStarterDiagnostics(CpfStarterProperties properties) {
        return CpfStarterDiagnostics.active(properties.isLoggingAnnotationEnabled());
    }

    @Bean(name = "cpfStarterHealthIndicator") @ConditionalOnMissingBean(name = "cpfStarterHealthIndicator")
    HealthIndicator cpfStarterHealthIndicator(CpfStarterProperties properties) {
        return () -> properties.isDiagnostics()
                ? Health.up().withDetail("artifact", "cpf-starter")
                    .withDetail("strict", properties.isStrict())
                    .withDetail("contextRuntime", "BOUNDARY_MANAGED").build()
                : Health.unknown().withDetail("artifact", "cpf-starter")
                    .withDetail("diagnostics", "DISABLED").build();
    }
}
