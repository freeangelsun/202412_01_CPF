package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeFenceException;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInstanceLease;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInstanceRegistration;
import java.sql.SQLTransactionRollbackException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfRuntimeControlAgentRegistrationRetryTest {
    @Test
    void retriesTheWholePortCallAfterTransientDeadlockAndThenStarts() {
        CpfRuntimeAgentPort port = mock(CpfRuntimeAgentPort.class);
        CpfRuntimeInstanceRegistration registration = registration();
        CpfRuntimeInstanceLease lease = lease();
        when(port.register(registration))
                .thenThrow(deadlock())
                .thenReturn(lease);
        CpfRuntimeInstanceInboxStore inbox = mock(CpfRuntimeInstanceInboxStore.class);
        when(inbox.latestAppliedStates()).thenReturn(List.of());
        List<Long> delays = new ArrayList<>();
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                port, registration, List.of(), inbox, 3, 25L, delays::add);

        agent.start();

        verify(port, times(2)).register(registration);
        assertThat(delays).containsExactly(25L);
    }

    @Test
    void exhaustsTheStrictBoundAndRethrowsTheTransientFailure() {
        CpfRuntimeAgentPort port = mock(CpfRuntimeAgentPort.class);
        CpfRuntimeInstanceRegistration registration = registration();
        when(port.register(registration)).thenThrow(deadlock());
        CpfRuntimeInstanceInboxStore inbox = mock(CpfRuntimeInstanceInboxStore.class);
        List<Long> delays = new ArrayList<>();
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                port, registration, List.of(), inbox, 3, 20L, delays::add);

        assertThatThrownBy(agent::start).isInstanceOf(CannotAcquireLockException.class);
        verify(port, times(3)).register(registration);
        assertThat(delays).containsExactly(20L, 40L);
    }

    @Test
    void neverRetriesFencingOrPermanentFailures() {
        CpfRuntimeAgentPort port = mock(CpfRuntimeAgentPort.class);
        CpfRuntimeInstanceRegistration registration = registration();
        when(port.register(registration)).thenThrow(new CpfRuntimeFenceException("identity conflict"));
        CpfRuntimeInstanceInboxStore inbox = mock(CpfRuntimeInstanceInboxStore.class);
        List<Long> delays = new ArrayList<>();
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                port, registration, List.of(), inbox, 5, 20L, delays::add);

        assertThatThrownBy(agent::start).isInstanceOf(CpfRuntimeFenceException.class);
        verify(port).register(registration);
        assertThat(delays).isEmpty();
    }

    private static CannotAcquireLockException deadlock() {
        return new CannotAcquireLockException(
                "transaction arbitration lost",
                new SQLTransactionRollbackException("deadlock", "40001"));
    }

    private static CpfRuntimeInstanceRegistration registration() {
        Instant now = Instant.parse("2026-08-24T02:00:00Z");
        return new CpfRuntimeInstanceRegistration(
                "worker-2", "BAT", "BAT_API", "local", null, null, "http://127.0.0.1:8283",
                "1", "commit", "WORKER", "AUTO_CONFIGURATION", "1", "hash",
                Map.of(), Map.of(), null, null, "host", "BAT", "worker", "WORKER",
                22L, "25", "1", "1", now, now, 60);
    }

    private static CpfRuntimeInstanceLease lease() {
        return new CpfRuntimeInstanceLease(
                "worker-2", 1L, 0L, 0L, null, null, "IN_SYNC", Instant.parse("2026-08-24T02:01:00Z"));
    }
}
