package com.cpf.starter.platform.operations.observability;

import com.cpf.platform.operations.observability.CpfLogPolicyVersionAutoConfiguration;
import com.cpf.platform.operations.observability.spi.logging.CpfLogPolicyVersionStore;
import com.cpf.starter.platform.operations.observability.internal.JdbcCpfLogPolicyVersionStore;
import java.time.Clock;
import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Shared JDBC provider configured before the Core log-policy operations facade. */
@AutoConfiguration(before = CpfLogPolicyVersionAutoConfiguration.class)
@ConditionalOnClass(DataSource.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "cpf.logging.policy-version", name = "mode", havingValue = "jdbc")
public class CpfLogPolicyVersionJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CpfLogPolicyVersionStore.class)
    CpfLogPolicyVersionStore cpfJdbcLogPolicyVersionStore(
            DataSource dataSource,
            Environment environment,
            ObjectProvider<Clock> clockProvider) {
        int maximumTargets = range(environment.getProperty(
                "cpf.logging.policy-version.jdbc.maximum-targets", Integer.class,
                JdbcCpfLogPolicyVersionStore.DEFAULT_MAXIMUM_TARGETS),
                1, 1_000_000, "maximum-targets");
        int maximumHistory = range(environment.getProperty(
                "cpf.logging.policy-version.jdbc.maximum-history-per-target", Integer.class,
                JdbcCpfLogPolicyVersionStore.DEFAULT_MAXIMUM_HISTORY_PER_TARGET),
                2, 4_096, "maximum-history-per-target");
        int maximumCommands = range(environment.getProperty(
                "cpf.logging.policy-version.jdbc.maximum-command-records", Integer.class,
                JdbcCpfLogPolicyVersionStore.DEFAULT_MAXIMUM_COMMAND_RECORDS),
                16, 10_000_000, "maximum-command-records");
        Duration commandTtl = environment.getProperty(
                "cpf.logging.policy-version.jdbc.command-ttl", Duration.class,
                JdbcCpfLogPolicyVersionStore.DEFAULT_COMMAND_TTL);
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("command-ttl must be positive and <= 365d");
        }
        JdbcCpfLogPolicyVersionStore store = new JdbcCpfLogPolicyVersionStore(
                dataSource, maximumTargets, maximumHistory, maximumCommands, commandTtl,
                clockProvider.getIfUnique(Clock::systemUTC));
        store.verifySchema();
        return store;
    }

    private static int range(int value, int minimum, int maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(property + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }
}
