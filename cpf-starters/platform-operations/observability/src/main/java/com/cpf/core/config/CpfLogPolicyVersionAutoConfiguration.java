package com.cpf.core.config;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionOperations;
import com.cpf.core.common.logging.audit.CpfFileLogPolicyVersionAuditSink;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.common.logging.policy.CpfLogPolicyCacheVersionApplier;
import com.cpf.core.common.logging.policy.LogPolicyCache;
import com.cpf.starter.platform.operations.observability.internal.logging.InMemoryCpfLogPolicyVersionStore;
import com.cpf.core.service.logging.DefaultCpfLogPolicyVersionManager;
import com.cpf.core.spi.logging.CpfLogPolicyVersionApplier;
import com.cpf.core.spi.logging.CpfLogPolicyVersionAuditSink;
import com.cpf.core.spi.logging.CpfLogPolicyVersionStore;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Explicit control-plane wiring. Disabled by default; in-memory mode is single-JVM only. */
@AutoConfiguration
public class CpfLogPolicyVersionAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "cpf.logging.policy-version", name = "mode", havingValue = "in-memory")
    @ConditionalOnMissingBean(CpfLogPolicyVersionStore.class)
    InMemoryCpfLogPolicyVersionStore cpfLogPolicyVersionStore(Environment environment,
            ObjectProvider<Clock> clocks) {
        int targets = environment.getProperty("cpf.logging.policy-version.in-memory.maximum-targets",
                Integer.class, InMemoryCpfLogPolicyVersionStore.DEFAULT_MAXIMUM_TARGETS);
        int history = environment.getProperty("cpf.logging.policy-version.in-memory.maximum-history-per-target",
                Integer.class, InMemoryCpfLogPolicyVersionStore.DEFAULT_MAXIMUM_HISTORY_PER_TARGET);
        int commands = environment.getProperty("cpf.logging.policy-version.in-memory.maximum-command-records",
                Integer.class, InMemoryCpfLogPolicyVersionStore.DEFAULT_MAXIMUM_COMMAND_RECORDS);
        Duration ttl = environment.getProperty("cpf.logging.policy-version.in-memory.command-ttl",
                Duration.class, InMemoryCpfLogPolicyVersionStore.DEFAULT_COMMAND_TTL);
        return new InMemoryCpfLogPolicyVersionStore(targets, history, commands, ttl,
                clocks.getIfUnique(Clock::systemUTC));
    }

    @Bean @ConditionalOnBean(LogPolicyCache.class)
    @ConditionalOnMissingBean(CpfLogPolicyVersionApplier.class)
    CpfLogPolicyVersionApplier cpfLogPolicyVersionApplier(LogPolicyCache cache) {
        return new CpfLogPolicyCacheVersionApplier(cache);
    }

    @Bean @ConditionalOnBean(CpfFileLogWriter.class)
    @ConditionalOnMissingBean(CpfLogPolicyVersionAuditSink.class)
    CpfLogPolicyVersionAuditSink cpfLogPolicyVersionAuditSink(CpfFileLogWriter writer) {
        return new CpfFileLogPolicyVersionAuditSink(writer);
    }

    @Bean @ConditionalOnBean({CpfLogPolicyVersionStore.class, CpfLogPolicyVersionAuditSink.class,
            CpfLogPolicyVersionApplier.class})
    @ConditionalOnMissingBean(CpfLogPolicyVersionOperations.class)
    CpfLogPolicyVersionOperations cpfLogPolicyVersionOperations(CpfLogPolicyVersionStore store,
            CpfLogPolicyVersionAuditSink audit, CpfLogPolicyVersionApplier applier,
            ObjectProvider<Clock> clocks) {
        return new DefaultCpfLogPolicyVersionManager(store, audit, applier,
                clocks.getIfUnique(Clock::systemUTC));
    }
}
