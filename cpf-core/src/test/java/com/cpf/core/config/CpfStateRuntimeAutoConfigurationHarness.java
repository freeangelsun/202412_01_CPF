package com.cpf.core.config;

import com.cpf.core.api.state.CpfStateOperations;
import com.cpf.core.api.state.CpfStateRuntimeStatus;
import com.cpf.core.internal.state.InMemoryCpfStateStore;
import com.cpf.core.spi.state.CpfStateAuditSink;
import com.cpf.core.spi.state.CpfStateStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;

public final class CpfStateRuntimeAutoConfigurationHarness {
    private CpfStateRuntimeAutoConfigurationHarness() {}

    public static void main(String[] args) {
        CpfStateRuntimeAutoConfiguration configuration = new CpfStateRuntimeAutoConfiguration();
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.state.in-memory.maximum-states", "2")
                .withProperty("cpf.state.in-memory.maximum-operations-per-state", "3")
                .withProperty("cpf.state.in-memory.command-ttl", "30m");
        InMemoryCpfStateStore store = configuration.cpfStateStore(
                provider(List.of(clock)), environment);
        CpfStateRuntimeStatus.RuntimeSnapshot initial = store.stateRuntimeSnapshot();
        require(initial.maximumStates() == 2, "configured state bound");

        CountingAudit first = new CountingAudit();
        CountingAudit second = new CountingAudit();
        CpfStateOperations operations = configuration.cpfStateOperations(
                store,
                provider(List.of(clock)),
                provider(List.of(first, second)));
        require(operations.start("cfg:1", "cfg-op", "worker", "start").applied(),
                "configured state consumer");
        require(first.count == 1 && second.count == 1, "all ordered audit sinks must receive decision");

        expectInvalid(configuration, clock,
                "cpf.state.in-memory.maximum-states", "0");
        expectInvalid(configuration, clock,
                "cpf.state.in-memory.maximum-operations-per-state", "10001");
        expectInvalid(configuration, clock,
                "cpf.state.in-memory.command-ttl", "0s");
        expectInvalid(configuration, clock,
                "cpf.state.in-memory.command-ttl", "not-a-duration");
        System.out.println("CPF_STATE_AUTOCONFIG_HARNESS_PASS");
    }

    private static void expectInvalid(
            CpfStateRuntimeAutoConfiguration configuration,
            Clock clock,
            String key,
            String value) {
        try {
            configuration.cpfStateStore(
                    provider(List.of(clock)),
                    new MockEnvironment().withProperty(key, value));
            throw new AssertionError("invalid state property must fail startup");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static <T> ObjectProvider<T> provider(List<T> values) {
        return new ObjectProvider<>() {
            @Override public T getObject() {
                if (values.isEmpty()) throw new IllegalStateException("no object available");
                return values.getFirst();
            }
            @Override public T getIfAvailable() {
                return values.isEmpty() ? null : values.getFirst();
            }
            @Override public Stream<T> orderedStream() {
                return values.stream();
            }
        };
    }

    private static final class CountingAudit implements CpfStateAuditSink {
        private int count;
        @Override public void record(com.cpf.core.api.state.CpfStateAuditEvent event) {
            count++;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
