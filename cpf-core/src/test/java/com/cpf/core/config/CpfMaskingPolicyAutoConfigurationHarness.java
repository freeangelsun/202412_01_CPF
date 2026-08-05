package com.cpf.core.config;

import com.cpf.core.api.security.CpfMaskingPolicyApproval;
import com.cpf.core.api.security.CpfMaskingPolicyOperations;
import com.cpf.core.api.security.CpfMaskingPolicyResult;
import com.cpf.core.api.security.CpfMaskingPolicyUpdateCommand;
import com.cpf.core.internal.security.InMemoryCpfMaskingPolicyStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

/** Executable bean wiring and fail-fast property validation gate. */
public final class CpfMaskingPolicyAutoConfigurationHarness {
    private CpfMaskingPolicyAutoConfigurationHarness() {
    }

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T07:00:00Z"), ZoneOffset.UTC);
        CpfMaskingPolicyAutoConfiguration configuration = new CpfMaskingPolicyAutoConfiguration();
        MapEnvironment environment = new MapEnvironment(Map.of(
                "cpf.security.masking-policy.in-memory.maximum-history", 8,
                "cpf.security.masking-policy.in-memory.maximum-command-records", 32,
                "cpf.security.masking-policy.in-memory.command-ttl", java.time.Duration.ofHours(1)));
        InMemoryCpfMaskingPolicyStore store = configuration.cpfMaskingPolicyStore(
                environment, new FixedProvider<>(clock));
        CpfMaskingPolicyOperations operations = configuration.cpfMaskingPolicyOperations(
                store, event -> { }, new FixedProvider<>(clock));
        long version = operations.current().version();
        CpfMaskingPolicyUpdateCommand unsigned = new CpfMaskingPolicyUpdateCommand(
                "mask-auto-0001", version, Set.of("password", "autoconfigkey"), 512, true,
                "security-requester", "auto configuration policy change", null);
        CpfMaskingPolicyUpdateCommand command = new CpfMaskingPolicyUpdateCommand(
                unsigned.commandId(), unsigned.expectedVersion(), unsigned.sensitiveKeys(),
                unsigned.maxLength(), unsigned.maskBearerToken(), unsigned.actor(), unsigned.reason(),
                new CpfMaskingPolicyApproval(unsigned.commandHash(), "security-approver",
                        clock.instant().minusSeconds(1), clock.instant().plusSeconds(60)));
        if (operations.update(command).status() != CpfMaskingPolicyResult.Status.APPLIED) {
            throw new AssertionError("auto-configured masking policy manager did not apply update");
        }
        boolean invalid = false;
        try {
            configuration.cpfMaskingPolicyStore(new MapEnvironment(Map.of(
                    "cpf.security.masking-policy.in-memory.maximum-command-records", 1)),
                    new FixedProvider<>(clock));
        } catch (IllegalArgumentException expected) {
            invalid = true;
        }
        if (!invalid) throw new AssertionError("invalid command capacity did not fail fast");
        System.out.println("CPF_MASKING_POLICY_AUTOCONFIG_HARNESS_PASS");
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
