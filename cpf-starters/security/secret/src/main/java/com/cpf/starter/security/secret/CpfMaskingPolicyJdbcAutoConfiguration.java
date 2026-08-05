package com.cpf.starter.security.secret;

import com.cpf.core.api.security.CpfMaskingPolicySnapshot;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.config.CpfMaskingPolicyAutoConfiguration;
import com.cpf.core.spi.security.CpfMaskingPolicyStore;
import com.cpf.starter.security.secret.internal.JdbcCpfMaskingPolicyStore;
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

/** Shared JDBC masking-policy provider configured before the Core operations facade. */
@AutoConfiguration(before = CpfMaskingPolicyAutoConfiguration.class)
@ConditionalOnClass(DataSource.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "cpf.security.masking-policy", name = "mode", havingValue = "jdbc")
public class CpfMaskingPolicyJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CpfMaskingPolicyStore.class)
    JdbcCpfMaskingPolicyStore cpfJdbcMaskingPolicyStore(
            DataSource dataSource,
            Environment environment,
            ObjectProvider<Clock> clockProvider) {
        Clock clock = clockProvider.getIfUnique(Clock::systemUTC);
        int maximumHistory = requiredRange(environment.getProperty(
                "cpf.security.masking-policy.jdbc.maximum-history",
                Integer.class,
                JdbcCpfMaskingPolicyStore.DEFAULT_MAXIMUM_HISTORY), 2, 4_096, "maximum-history");
        int maximumCommands = requiredRange(environment.getProperty(
                "cpf.security.masking-policy.jdbc.maximum-command-records",
                Integer.class,
                JdbcCpfMaskingPolicyStore.DEFAULT_MAXIMUM_COMMAND_RECORDS),
                16, 65_536, "maximum-command-records");
        Duration commandTtl = environment.getProperty(
                "cpf.security.masking-policy.jdbc.command-ttl",
                Duration.class,
                JdbcCpfMaskingPolicyStore.DEFAULT_COMMAND_TTL);
        if (commandTtl == null || commandTtl.isZero() || commandTtl.isNegative()
                || commandTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("command-ttl must be positive and <= 365d");
        }
        SensitiveDataMasker.MaskingPolicy current = SensitiveDataMasker.currentPolicy();
        CpfMaskingPolicySnapshot initial = new CpfMaskingPolicySnapshot(
                current.version(), current.sensitiveKeys(), current.maxLength(),
                current.maskBearerToken(), current.updatedAt(), "CPF_SYSTEM",
                "initial masking policy loaded from runtime");
        return new JdbcCpfMaskingPolicyStore(
                dataSource, initial, maximumHistory, maximumCommands, commandTtl, clock);
    }

    private static int requiredRange(int value, int minimum, int maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    property + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }
}
