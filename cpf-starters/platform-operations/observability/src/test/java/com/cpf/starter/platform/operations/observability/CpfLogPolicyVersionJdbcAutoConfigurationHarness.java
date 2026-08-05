package com.cpf.starter.platform.operations.observability;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;

/** Executable fail-fast wiring contract for the shared log-policy JDBC provider. */
public final class CpfLogPolicyVersionJdbcAutoConfigurationHarness {
    private CpfLogPolicyVersionJdbcAutoConfigurationHarness() { }

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        CpfLogPolicyVersionJdbcAutoConfiguration configuration =
                new CpfLogPolicyVersionJdbcAutoConfiguration();
        AtomicInteger connections = new AtomicInteger();
        DataSource unavailable = (DataSource) Proxy.newProxyInstance(
                CpfLogPolicyVersionJdbcAutoConfigurationHarness.class.getClassLoader(),
                new Class<?>[] {DataSource.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getConnection")) {
                        connections.incrementAndGet();
                        throw new SQLException("database unavailable");
                    }
                    if (method.getName().equals("isWrapperFor")) return false;
                    if (method.getName().equals("unwrap")) throw new SQLException("not a wrapper");
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(int.class)) return 0;
                    return null;
                });

        boolean invalid = false;
        try {
            configuration.cpfJdbcLogPolicyVersionStore(
                    unavailable,
                    new MapEnvironment(Map.of(
                            "cpf.logging.policy-version.jdbc.maximum-history-per-target", 1)),
                    new FixedProvider<>(clock));
        } catch (IllegalArgumentException expected) {
            invalid = true;
        }
        require(invalid, "invalid history capacity must fail fast");
        require(connections.get() == 0,
                "invalid configuration must fail before acquiring a database connection");

        boolean ttlInvalid = false;
        try {
            configuration.cpfJdbcLogPolicyVersionStore(
                    unavailable,
                    new MapEnvironment(Map.of(
                            "cpf.logging.policy-version.jdbc.command-ttl", Duration.ZERO)),
                    new FixedProvider<>(clock));
        } catch (IllegalArgumentException expected) {
            ttlInvalid = true;
        }
        require(ttlInvalid, "invalid command TTL must fail fast");
        require(connections.get() == 0,
                "invalid TTL must fail before acquiring a database connection");

        boolean schemaFailure = false;
        try {
            configuration.cpfJdbcLogPolicyVersionStore(
                    unavailable,
                    new MapEnvironment(Map.of(
                            "cpf.logging.policy-version.jdbc.maximum-targets", 32,
                            "cpf.logging.policy-version.jdbc.maximum-history-per-target", 8,
                            "cpf.logging.policy-version.jdbc.maximum-command-records", 128,
                            "cpf.logging.policy-version.jdbc.command-ttl", Duration.ofHours(2))),
                    new FixedProvider<>(clock));
        } catch (IllegalStateException expected) {
            schemaFailure = expected.getMessage().contains("schema");
        }
        require(schemaFailure, "missing JDBC schema must fail startup explicitly");
        require(connections.get() == 1, "valid configuration must attempt one schema transaction");

        ConditionalOnProperty property = CpfLogPolicyVersionJdbcAutoConfiguration.class
                .getAnnotation(ConditionalOnProperty.class);
        require(property != null && "jdbc".equals(property.havingValue()) && !property.matchIfMissing(),
                "JDBC provider must be explicit and disabled by default");

        System.out.println("CPF_LOG_POLICY_VERSION_JDBC_AUTOCONFIG_HARNESS_PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private record FixedProvider<T>(T value) implements ObjectProvider<T> {
        @Override public T getIfUnique(Supplier<T> defaultSupplier) {
            return value == null ? defaultSupplier.get() : value;
        }
    }

    private static final class MapEnvironment implements Environment {
        private final Map<String, Object> values = new HashMap<>();
        private MapEnvironment(Map<String, Object> values) { this.values.putAll(values); }
        @Override public <T> T getProperty(String key, Class<T> type, T defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : type.cast(value);
        }
    }
}
