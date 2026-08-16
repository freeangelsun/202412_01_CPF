package com.cpf.security;

import com.cpf.security.api.CpfMaskingPolicyOperations;
import com.cpf.security.api.CpfMaskingPolicySnapshot;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.security.internal.masking.InMemoryCpfMaskingPolicyStore;
import com.cpf.security.internal.masking.DefaultCpfMaskingPolicyManager;
import com.cpf.security.spi.CpfMaskingPolicyAuditSink;
import com.cpf.security.spi.CpfMaskingPolicyStore;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Explicit masking-policy control plane. Shared deployments replace the in-memory store. */
@AutoConfiguration
public class CpfMaskingPolicyAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "cpf.security.masking-policy", name = "mode", havingValue = "in-memory")
    @ConditionalOnMissingBean(CpfMaskingPolicyStore.class)
    InMemoryCpfMaskingPolicyStore cpfMaskingPolicyStore(
            Environment environment,
            ObjectProvider<Clock> clockProvider) {
        Clock clock = clockProvider.getIfUnique(Clock::systemUTC);
        int maximumHistory = requiredRange(environment.getProperty(
                "cpf.security.masking-policy.in-memory.maximum-history",
                Integer.class,
                InMemoryCpfMaskingPolicyStore.DEFAULT_MAXIMUM_HISTORY), 2, 4_096, "maximum-history");
        int maximumCommands = requiredRange(environment.getProperty(
                "cpf.security.masking-policy.in-memory.maximum-command-records",
                Integer.class,
                InMemoryCpfMaskingPolicyStore.DEFAULT_MAXIMUM_COMMAND_RECORDS), 16, 65_536, "maximum-command-records");
        Duration commandTtl = environment.getProperty(
                "cpf.security.masking-policy.in-memory.command-ttl",
                Duration.class,
                InMemoryCpfMaskingPolicyStore.DEFAULT_COMMAND_TTL);
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("command-ttl must be positive and <= 365d");
        }
        CpfMaskingRuntime.MaskingPolicy current = CpfMaskingRuntime.currentPolicy();
        CpfMaskingPolicySnapshot initial = new CpfMaskingPolicySnapshot(
                current.version(), current.sensitiveKeys(), current.maxLength(), current.maskBearerToken(),
                clock.instant(), "CPF_SYSTEM", "initial masking policy loaded from runtime");
        return new InMemoryCpfMaskingPolicyStore(
                initial, maximumHistory, maximumCommands, commandTtl, clock);
    }


    @Bean
    @ConditionalOnBean({CpfMaskingPolicyStore.class, CpfMaskingPolicyAuditSink.class})
    @ConditionalOnMissingBean(CpfMaskingPolicyOperations.class)
    CpfMaskingPolicyOperations cpfMaskingPolicyOperations(
            CpfMaskingPolicyStore store,
            CpfMaskingPolicyAuditSink auditSink,
            ObjectProvider<Clock> clockProvider) {
        return new DefaultCpfMaskingPolicyManager(
                store, auditSink, clockProvider.getIfUnique(Clock::systemUTC));
    }

    private static int requiredRange(int value, int minimum, int maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(property + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }
}
