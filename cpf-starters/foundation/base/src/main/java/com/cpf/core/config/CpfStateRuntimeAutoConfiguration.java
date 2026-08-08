package com.cpf.core.config;

import com.cpf.core.api.state.CpfStateOperations;
import com.cpf.starter.foundation.base.internal.state.InMemoryCpfStateStore;
import com.cpf.core.service.state.DefaultCpfStateOperations;
import com.cpf.core.spi.state.CpfStateAuditSink;
import com.cpf.core.spi.state.CpfStateStore;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Explicit single-JVM state provider. Shared deployments must provide a durable CpfStateStore. */
@AutoConfiguration
@ConditionalOnProperty(prefix = "cpf.state", name = "mode", havingValue = "in-memory")
public class CpfStateRuntimeAutoConfiguration {
    static final int DEFAULT_MAXIMUM_STATES = 10_000;
    static final int DEFAULT_MAXIMUM_OPERATIONS_PER_STATE = 64;
    static final Duration DEFAULT_COMMAND_TTL = Duration.ofHours(24);

    @Bean
    @ConditionalOnMissingBean(CpfStateStore.class)
    InMemoryCpfStateStore cpfStateStore(
            ObjectProvider<Clock> clockProvider,
            Environment environment) {
        Clock clock = clockProvider.getIfAvailable();
        int maximumStates = requiredRange(
                environment.getProperty(
                        "cpf.state.in-memory.maximum-states",
                        Integer.class,
                        DEFAULT_MAXIMUM_STATES),
                1,
                1_000_000,
                "cpf.state.in-memory.maximum-states");
        int maximumOperationsPerState = requiredRange(
                environment.getProperty(
                        "cpf.state.in-memory.maximum-operations-per-state",
                        Integer.class,
                        DEFAULT_MAXIMUM_OPERATIONS_PER_STATE),
                1,
                10_000,
                "cpf.state.in-memory.maximum-operations-per-state");
        Duration commandTtl = requiredDuration(
                parseDuration(environment.getProperty(
                        "cpf.state.in-memory.command-ttl",
                        "24h"),
                        "cpf.state.in-memory.command-ttl"),
                Duration.ofDays(365),
                "cpf.state.in-memory.command-ttl");
        return new InMemoryCpfStateStore(
                maximumStates,
                maximumOperationsPerState,
                commandTtl,
                clock == null ? Clock.systemUTC() : clock);
    }

    @Bean
    @ConditionalOnMissingBean(CpfStateOperations.class)
    CpfStateOperations cpfStateOperations(
            CpfStateStore store,
            ObjectProvider<Clock> clockProvider,
            ObjectProvider<CpfStateAuditSink> auditProvider) {
        Clock clock = clockProvider.getIfAvailable();
        List<CpfStateAuditSink> audits = auditProvider.orderedStream().toList();
        return new DefaultCpfStateOperations(
                store,
                clock == null ? Clock.systemUTC() : clock,
                audits);
    }

    private static int requiredRange(int value, int minimum, int maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    property + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }


    private static Duration parseDuration(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(property + " must not be blank");
        }
        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        try {
            if (normalized.startsWith("p")) return Duration.parse(normalized.toUpperCase(java.util.Locale.ROOT));
            long multiplier;
            String amount;
            if (normalized.endsWith("ms")) { multiplier = 1L; amount = normalized.substring(0, normalized.length() - 2); }
            else if (normalized.endsWith("s")) { multiplier = 1_000L; amount = normalized.substring(0, normalized.length() - 1); }
            else if (normalized.endsWith("m")) { multiplier = 60_000L; amount = normalized.substring(0, normalized.length() - 1); }
            else if (normalized.endsWith("h")) { multiplier = 3_600_000L; amount = normalized.substring(0, normalized.length() - 1); }
            else if (normalized.endsWith("d")) { multiplier = 86_400_000L; amount = normalized.substring(0, normalized.length() - 1); }
            else { multiplier = 1L; amount = normalized; }
            return Duration.ofMillis(Math.multiplyExact(Long.parseLong(amount.trim()), multiplier));
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException(property + " has an invalid duration", invalid);
        }
    }

    private static Duration requiredDuration(
            Duration value,
            Duration maximum,
            String property) {
        if (value == null || value.isZero() || value.isNegative() || value.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(
                    property + " must be positive and <= " + maximum);
        }
        return value;
    }
}
