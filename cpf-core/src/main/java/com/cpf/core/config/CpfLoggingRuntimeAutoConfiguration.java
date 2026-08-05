package com.cpf.core.config;

import com.cpf.core.api.logging.CpfDynamicLogLevelOperations;
import com.cpf.core.api.observability.CpfTelemetry;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations;
import com.cpf.core.api.security.CpfMaskingPolicyOperations;
import com.cpf.core.api.security.CpfMaskingPolicySnapshot;
import com.cpf.core.common.logging.CpfTraceSamplingPolicy;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.DefaultCpfDynamicLogLevelOperations;
import com.cpf.core.common.logging.audit.CpfFileSensitiveDataAccessAuditSink;
import com.cpf.core.common.logging.audit.CpfFileMaskingPolicyAuditSink;
import com.cpf.core.common.logging.audit.CpfFileStateAuditSink;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.internal.security.InMemoryCpfSensitiveDataAccessStore;
import com.cpf.core.internal.security.InMemoryCpfMaskingPolicyStore;
import com.cpf.core.service.security.DefaultCpfSensitiveDataAccessManager;
import com.cpf.core.service.security.DefaultCpfMaskingPolicyManager;
import com.cpf.core.spi.security.CpfSensitiveDataAccessAuditSink;
import com.cpf.core.spi.security.CpfMaskingPolicyAuditSink;
import com.cpf.core.spi.security.CpfMaskingPolicyStore;
import com.cpf.core.spi.security.CpfSensitiveDataAccessStore;
import com.cpf.core.spi.state.CpfStateAuditSink;
import com.cpf.core.internal.observability.DefaultCpfTelemetry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.time.Duration;

/** Runtime logging policy wiring that remains replaceable by customer-specific distributed providers. */
@Configuration(proxyBeanMethods = false)
public class CpfLoggingRuntimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CpfDynamicLogLevelOperations.class)
    @ConditionalOnProperty(prefix = "cpf.logging.dynamic-level", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public DefaultCpfDynamicLogLevelOperations cpfDynamicLogLevelOperations(
            Environment environment, ObjectProvider<Clock> clockProvider) {
        Duration maxTtl = environment.getProperty(
                "cpf.logging.dynamic-level.max-ttl",
                Duration.class,
                Duration.ofHours(24));
        int maxActiveRules = environment.getProperty(
                "cpf.logging.dynamic-level.max-active-rules",
                Integer.class,
                2_048);
        int maxAuditRecords = environment.getProperty(
                "cpf.logging.dynamic-level.max-audit-records",
                Integer.class,
                DefaultCpfDynamicLogLevelOperations.DEFAULT_MAX_AUDIT_RECORDS);
        Clock clock = clockProvider.getIfUnique(Clock::systemUTC);
        return new DefaultCpfDynamicLogLevelOperations(
                clock, maxTtl, maxActiveRules, maxAuditRecords);
    }

    @Bean
    @ConditionalOnMissingBean(CpfTelemetry.class)
    public CpfTelemetry cpfTelemetry(Environment environment, ObjectProvider<Clock> clockProvider) {
        int maxActiveSpans = environment.getProperty(
                "cpf.tracing.max-active-spans", Integer.class, 16_384);
        Clock clock = clockProvider.getIfUnique(Clock::systemUTC);
        return new DefaultCpfTelemetry(clock, maxActiveSpans);
    }


    @Bean
    @ConditionalOnBean(CpfFileLogWriter.class)
    @ConditionalOnMissingBean(CpfStateAuditSink.class)
    public CpfStateAuditSink cpfStateAuditSink(CpfFileLogWriter fileLogWriter) {
        return new CpfFileStateAuditSink(fileLogWriter);
    }

    @Bean
    @ConditionalOnBean(CpfFileLogWriter.class)
    @ConditionalOnMissingBean(CpfSensitiveDataAccessAuditSink.class)
    public CpfSensitiveDataAccessAuditSink cpfSensitiveDataAccessAuditSink(
            CpfFileLogWriter fileLogWriter) {
        return new CpfFileSensitiveDataAccessAuditSink(fileLogWriter);
    }

    @Bean
    @ConditionalOnBean(CpfFileLogWriter.class)
    @ConditionalOnMissingBean(CpfMaskingPolicyAuditSink.class)
    public CpfMaskingPolicyAuditSink cpfMaskingPolicyAuditSink(CpfFileLogWriter fileLogWriter) {
        return new CpfFileMaskingPolicyAuditSink(fileLogWriter);
    }

    @Bean
    @ConditionalOnMissingBean(CpfMaskingPolicyStore.class)
    @ConditionalOnProperty(prefix = "cpf.security.masking-policy", name = "mode",
            havingValue = "in-memory")
    public InMemoryCpfMaskingPolicyStore cpfMaskingPolicyStore(
            Environment environment, ObjectProvider<Clock> clockProvider) {
        int maximumHistory = environment.getProperty(
                "cpf.security.masking-policy.in-memory.maximum-history", Integer.class,
                InMemoryCpfMaskingPolicyStore.DEFAULT_MAXIMUM_HISTORY);
        int maximumCommandRecords = environment.getProperty(
                "cpf.security.masking-policy.in-memory.maximum-command-records", Integer.class,
                InMemoryCpfMaskingPolicyStore.DEFAULT_MAXIMUM_COMMAND_RECORDS);
        Duration commandTtl = environment.getProperty(
                "cpf.security.masking-policy.in-memory.command-ttl", Duration.class,
                InMemoryCpfMaskingPolicyStore.DEFAULT_COMMAND_TTL);
        Clock clock = clockProvider.getIfUnique(Clock::systemUTC);
        SensitiveDataMasker.MaskingPolicy active = SensitiveDataMasker.currentPolicy();
        CpfMaskingPolicySnapshot initial = new CpfMaskingPolicySnapshot(
                active.version(), active.sensitiveKeys(), active.maxLength(), active.maskBearerToken(),
                active.updatedAt(), "SYSTEM", "runtime masking policy initialization");
        return new InMemoryCpfMaskingPolicyStore(
                initial, maximumHistory, maximumCommandRecords, commandTtl, clock);
    }

    @Bean
    @ConditionalOnBean({CpfMaskingPolicyStore.class, CpfMaskingPolicyAuditSink.class})
    @ConditionalOnMissingBean(CpfMaskingPolicyOperations.class)
    public DefaultCpfMaskingPolicyManager cpfMaskingPolicyOperations(
            CpfMaskingPolicyStore store,
            CpfMaskingPolicyAuditSink auditSink,
            ObjectProvider<Clock> clockProvider) {
        return new DefaultCpfMaskingPolicyManager(
                store, auditSink, clockProvider.getIfUnique(Clock::systemUTC));
    }

    @Bean
    @ConditionalOnMissingBean(CpfSensitiveDataAccessStore.class)
    @ConditionalOnProperty(prefix = "cpf.security.sensitive-access", name = "mode",
            havingValue = "in-memory")
    public InMemoryCpfSensitiveDataAccessStore cpfSensitiveDataAccessStore(
            Environment environment, ObjectProvider<Clock> clockProvider) {
        int maximumGrants = environment.getProperty(
                "cpf.security.sensitive-access.in-memory.maximum-grants",
                Integer.class,
                InMemoryCpfSensitiveDataAccessStore.DEFAULT_MAXIMUM_GRANTS);
        Duration terminalRetention = environment.getProperty(
                "cpf.security.sensitive-access.in-memory.terminal-retention",
                Duration.class,
                InMemoryCpfSensitiveDataAccessStore.DEFAULT_TERMINAL_RETENTION);
        Clock clock = clockProvider.getIfUnique(Clock::systemUTC);
        return new InMemoryCpfSensitiveDataAccessStore(maximumGrants, terminalRetention, clock);
    }

    @Bean
    @ConditionalOnBean({CpfSensitiveDataAccessStore.class, CpfSensitiveDataAccessAuditSink.class})
    @ConditionalOnMissingBean(CpfSensitiveDataAccessOperations.class)
    public DefaultCpfSensitiveDataAccessManager cpfSensitiveDataAccessOperations(
            CpfSensitiveDataAccessStore store,
            CpfSensitiveDataAccessAuditSink auditSink,
            ObjectProvider<Clock> clockProvider) {
        return new DefaultCpfSensitiveDataAccessManager(
                store, auditSink, clockProvider.getIfUnique(Clock::systemUTC));
    }

    @Bean
    @ConditionalOnMissingBean(CpfTraceSamplingPolicy.class)
    public CpfTraceSamplingPolicy cpfTraceSamplingPolicy(
            ObjectProvider<CpfDynamicLogLevelOperations> dynamicLogLevelOperations,
            Environment environment) {
        int maximumOverrideEntries = environment.getProperty(
                "cpf.tracing.sampling.maximum-override-entries",
                Integer.class,
                CpfTraceSamplingPolicy.DEFAULT_MAXIMUM_OVERRIDE_ENTRIES);
        return new CpfTraceSamplingPolicy(
                dynamicLogLevelOperations.getIfAvailable(), maximumOverrideEntries);
    }

    /** Compatibility helper for direct non-Spring construction. */
    public CpfTraceSamplingPolicy cpfTraceSamplingPolicy(
            ObjectProvider<CpfDynamicLogLevelOperations> dynamicLogLevelOperations) {
        return new CpfTraceSamplingPolicy(dynamicLogLevelOperations.getIfAvailable());
    }
}
