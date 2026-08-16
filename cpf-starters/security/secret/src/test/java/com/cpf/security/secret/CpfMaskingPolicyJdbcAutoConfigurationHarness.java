package com.cpf.security.secret;

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
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/** Executable fail-fast wiring contract for the shared masking-policy provider. */
public final class CpfMaskingPolicyJdbcAutoConfigurationHarness {
    private CpfMaskingPolicyJdbcAutoConfigurationHarness() { }

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T08:00:00Z"), ZoneOffset.UTC);
        CpfMaskingPolicyJdbcAutoConfiguration configuration =
                new CpfMaskingPolicyJdbcAutoConfiguration();
        AtomicInteger connections = new AtomicInteger();
        DataSource unavailable = (DataSource) Proxy.newProxyInstance(
                CpfMaskingPolicyJdbcAutoConfigurationHarness.class.getClassLoader(),
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
            configuration.cpfJdbcMaskingPolicyStore(
                    unavailable,
                    new MapEnvironment(Map.of(
                            "cpf.security.masking-policy.jdbc.maximum-history", 1)),
                    new FixedProvider<>(clock));
        } catch (IllegalArgumentException expected) {
            invalid = true;
        }
        require(invalid, "invalid JDBC history capacity must fail fast");
        require(connections.get() == 0,
                "invalid configuration must fail before acquiring a database connection");

        boolean schemaFailure = false;
        try {
            configuration.cpfJdbcMaskingPolicyStore(
                    unavailable,
                    new MapEnvironment(Map.of(
                            "cpf.security.masking-policy.jdbc.maximum-history", 32,
                            "cpf.security.masking-policy.jdbc.maximum-command-records", 128,
                            "cpf.security.masking-policy.jdbc.command-ttl", Duration.ofHours(2))),
                    new FixedProvider<>(clock));
        } catch (IllegalStateException expected) {
            schemaFailure = expected.getMessage().contains("schema");
        }
        require(schemaFailure, "missing JDBC schema must fail startup explicitly");
        require(connections.get() == 1, "valid configuration must attempt one schema transaction");

        System.out.println("CPF_MASKING_POLICY_JDBC_AUTOCONFIG_HARNESS_PASS");
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
        @Override public boolean acceptsProfiles(Profiles profiles) { return profiles.matches(profile -> false); }
        @Override public <T> T getProperty(String key, Class<T> type, T defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : type.cast(value);
        }
    }
}
