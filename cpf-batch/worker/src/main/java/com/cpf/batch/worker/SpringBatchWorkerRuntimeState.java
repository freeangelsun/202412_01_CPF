package com.cpf.batch.worker;

import com.cpf.foundation.runtime.CpfInstanceIdentity;
import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.RuntimeStateProvider;
import com.cpf.messaging.api.CpfBrokerConsumerControl;
import com.cpf.messaging.api.CpfBrokerConsumerControlPort;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Provider-neutral runtime state and admission control for the Spring Batch worker. */
@Component
public final class SpringBatchWorkerRuntimeState implements RuntimeStateProvider {
    static final String CONTROL_PORT_UNAVAILABLE = "BAT_WORKER_BROKER_CONTROL_PORT_NOT_AVAILABLE";
    static final String CONTROL_APPLY_FAILED = "BAT_WORKER_BROKER_CONTROL_APPLY_FAILED";

    private final List<CpfBrokerConsumerControlPort> controlPorts;
    private final BatchRuntimePolicy runtimePolicy;
    private final WorkerExecutionTracker executions;
    private final String workerId;
    private final String workerVersion;
    private final List<String> capabilities;
    private final int configuredMaxConcurrency;
    private final int configuredPrefetch;
    private final AtomicBoolean manualDrain = new AtomicBoolean();
    private final AtomicReference<String> controlError = new AtomicReference<>();
    private final AtomicLong leaseEpoch = new AtomicLong();
    private final AtomicLong observedFencingToken = new AtomicLong(-1L);

    public SpringBatchWorkerRuntimeState(
            List<CpfBrokerConsumerControlPort> controlPorts,
            BatchRuntimePolicy runtimePolicy,
            WorkerExecutionTracker executions,
            @Value("${cpf.batch.worker.worker-id:${CPF_BAT_WORKER_ID:}}")
            String workerId,
            @Value("${cpf.batch.worker.version:${CPF_BAT_WORKER_VERSION:${CPF_ARTIFACT_VERSION:dev}}}")
            String workerVersion,
            @Value("${cpf.batch.worker.capabilities:${CPF_BAT_WORKER_CAPABILITIES:GENERAL}}")
            String capabilityText,
            @Value("${cpf.batch.worker.max-concurrency:${CPF_BAT_WORKER_MAX_CONCURRENCY:1}}")
            int maxConcurrency,
            @Value("${cpf.batch.worker.broker-prefetch:${CPF_BAT_WORKER_BROKER_PREFETCH:1}}")
            int prefetch) {
        this.controlPorts = List.copyOf(controlPorts);
        this.runtimePolicy = runtimePolicy;
        this.executions = executions;
        this.workerId = requireText(workerId == null || workerId.isBlank() ? CpfInstanceIdentity.instanceId() : workerId, "workerId");
        this.workerVersion = requireText(workerVersion, "workerVersion");
        this.capabilities = capabilities(capabilityText);
        if (maxConcurrency < 1 || maxConcurrency > BatchRuntimePolicy.MAX_CONCURRENCY) {
            throw new IllegalArgumentException("Worker max concurrency is out of range");
        }
        if (prefetch < 1 || prefetch > 100_000) {
            throw new IllegalArgumentException("Worker broker prefetch is out of range");
        }
        this.configuredMaxConcurrency = maxConcurrency;
        this.configuredPrefetch = prefetch;
        this.executions.updateCapacity(effectiveConcurrency());
    }

    @Scheduled(fixedDelayString = "${cpf.batch.worker.runtime-reconcile-ms:500}")
    public synchronized void reconcile() {
        executions.updateCapacity(effectiveConcurrency());
        refreshLeaseEpoch();
        if (controlPorts.isEmpty()) {
            controlError.set(CONTROL_PORT_UNAVAILABLE);
            return;
        }
        CpfBrokerConsumerControl control = new CpfBrokerConsumerControl(
                manualDrain.get() || !runtimePolicy.current().workerEnabled(),
                effectiveConcurrency(),
                configuredPrefetch);
        try {
            for (CpfBrokerConsumerControlPort port : controlPorts) {
                port.apply(control);
            }
            controlError.set(null);
        } catch (RuntimeException failure) {
            controlError.set(CONTROL_APPLY_FAILED + "_" + failure.getClass().getSimpleName()
                    .toUpperCase(Locale.ROOT));
        }
    }

    public void drain() {
        manualDrain.set(true);
        reconcile();
    }

    public void resume() {
        manualDrain.set(false);
        reconcile();
    }

    public boolean drained() {
        return draining() && executions.snapshot().inFlightInvocations() == 0;
    }

    public String workerId() {
        return workerId;
    }

    public String workerVersion() {
        return workerVersion;
    }

    public List<String> capabilities() {
        return capabilities;
    }

    public int configuredMaxConcurrency() {
        return configuredMaxConcurrency;
    }

    public long leaseEpoch() {
        refreshLeaseEpoch();
        return leaseEpoch.get();
    }

    public Long currentJobExecutionId() {
        return executions.snapshot().currentJobExecutionId();
    }

    @Override
    public ActualState actualState() {
        WorkerExecutionTracker.Snapshot snapshot = executions.snapshot();
        if (controlError.get() != null) {
            return ActualState.DEGRADED;
        }
        if (draining()) {
            return ActualState.DRAINING;
        }
        if (snapshot.inFlightInvocations() > 0) {
            return ActualState.BUSY;
        }
        return ready() ? ActualState.READY : ActualState.STARTING;
    }

    @Override
    public boolean ready() {
        return !draining() && !controlPorts.isEmpty() && controlError.get() == null;
    }

    @Override
    public List<String> currentExecutions() {
        return executions.snapshot().executionIds();
    }

    @Override
    public List<String> activeLeases() {
        long token = fencingToken();
        return token > 0 ? List.of("lease-epoch=" + leaseEpoch() + ";fencing-token=" + token) : List.of();
    }

    @Override
    public int availableCapacity() {
        if (!ready()) {
            return 0;
        }
        WorkerExecutionTracker.Snapshot snapshot = executions.snapshot();
        return Math.max(0, effectiveConcurrency() - snapshot.activeInvocations());
    }

    @Override
    public long queueDepth() {
        return executions.snapshot().pendingInvocations();
    }

    @Override
    public boolean draining() {
        return manualDrain.get() || !runtimePolicy.current().workerEnabled();
    }

    @Override
    public Map<String, String> dependencyHealth() {
        return Map.of("brokerConsumerControl", controlPorts.isEmpty()
                ? "NOT_AVAILABLE"
                : controlError.get() == null ? "UP" : "DOWN");
    }

    @Override
    public String lastErrorCode() {
        return controlError.get();
    }

    @Override
    public Map<String, Number> metrics() {
        WorkerExecutionTracker.Snapshot snapshot = executions.snapshot();
        LinkedHashMap<String, Number> values = new LinkedHashMap<>();
        values.put("worker.configuredMaxConcurrency", configuredMaxConcurrency);
        values.put("worker.effectiveConcurrency", effectiveConcurrency());
        values.put("worker.activeInvocations", snapshot.activeInvocations());
        values.put("worker.pendingInvocations", snapshot.pendingInvocations());
        values.put("worker.brokerControlPorts", controlPorts.size());
        values.put("worker.leaseEpoch", leaseEpoch());
        values.put("worker.fencingToken", fencingToken());
        return Map.copyOf(values);
    }

    @Override
    public long fencingToken() {
        return Math.max(0L, executions.snapshot().fencingToken());
    }

    private void refreshLeaseEpoch() {
        long token = fencingToken();
        long previous = observedFencingToken.getAndSet(token);
        if (previous >= 0 && previous != token) {
            leaseEpoch.incrementAndGet();
        }
    }

    private int effectiveConcurrency() {
        return Math.min(configuredMaxConcurrency, runtimePolicy.current().workerConcurrencyLimit());
    }

    private static List<String> capabilities(String text) {
        List<String> values = Arrays.stream(text == null ? new String[0] : text.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .sorted()
                .toList();
        return values.isEmpty() ? List.of("GENERAL") : values;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}
