package com.cpf.batch.worker;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.RuntimeStateProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runtime state and admission control for the Spring Batch/Kafka worker.
 *
 * <p>The worker never polls {@code bat_execution} or owns a parallel CPF lease lifecycle. Drain
 * and resume are applied to the Kafka listener container, while in-flight work is observed at the
 * Spring Batch {@link SpringBatchWorkerStepHandler} boundary.</p>
 */
@Component
public final class SpringBatchWorkerRuntimeState implements RuntimeStateProvider {
    static final String LISTENER_UNAVAILABLE = "BAT_WORKER_KAFKA_LISTENER_NOT_AVAILABLE";
    static final String LISTENER_STOPPED = "BAT_WORKER_KAFKA_LISTENER_STOPPED";
    static final String LISTENER_CONTROL_FAILED = "BAT_WORKER_KAFKA_LISTENER_CONTROL_FAILED";

    private final KafkaListenerEndpointRegistry listenerRegistry;
    private final String workerGroupId;
    private final BatchRuntimePolicy runtimePolicy;
    private final WorkerExecutionTracker executions;
    private final String workerId;
    private final String workerVersion;
    private final List<String> capabilities;
    private final int configuredMaxConcurrency;
    private final AtomicBoolean manualDrain = new AtomicBoolean();
    private final AtomicReference<String> listenerError = new AtomicReference<>();

    public SpringBatchWorkerRuntimeState(
            KafkaListenerEndpointRegistry listenerRegistry,
            @Qualifier("cpfBatchWorkerGroupId") String workerGroupId,
            BatchRuntimePolicy runtimePolicy,
            WorkerExecutionTracker executions,
            @Value("${cpf.batch.worker.worker-id:${CPF_BAT_WORKER_ID:${CPF_INSTANCE_ID:worker-local-01}}}")
            String workerId,
            @Value("${cpf.batch.worker.version:${CPF_BAT_WORKER_VERSION:${CPF_ARTIFACT_VERSION:dev}}}")
            String workerVersion,
            @Value("${cpf.batch.worker.capabilities:${CPF_BAT_WORKER_CAPABILITIES:GENERAL}}")
            String capabilityText,
            @Value("${cpf.batch.worker.max-concurrency:${CPF_BAT_WORKER_MAX_CONCURRENCY:1}}")
            int maxConcurrency) {
        this.listenerRegistry = listenerRegistry;
        this.workerGroupId = requireText(workerGroupId, "workerGroupId");
        this.runtimePolicy = runtimePolicy;
        this.executions = executions;
        this.workerId = requireText(workerId, "workerId");
        this.workerVersion = requireText(workerVersion, "workerVersion");
        this.capabilities = capabilities(capabilityText);
        if (maxConcurrency < 1 || maxConcurrency > BatchRuntimePolicy.MAX_CONCURRENCY) {
            throw new IllegalArgumentException("Worker max concurrency is out of range");
        }
        this.configuredMaxConcurrency = maxConcurrency;
        this.executions.updateCapacity(effectiveConcurrency());
    }

    @Scheduled(fixedDelayString = "${cpf.batch.worker.runtime-reconcile-ms:500}")
    public synchronized void reconcile() {
        executions.updateCapacity(effectiveConcurrency());
        List<MessageListenerContainer> containers = workerContainers();
        if (containers.isEmpty()) {
            listenerError.set(LISTENER_UNAVAILABLE);
            return;
        }
        try {
            boolean pause = manualDrain.get() || !runtimePolicy.current().workerEnabled();
            for (MessageListenerContainer container : containers) {
                if (pause) {
                    if (!container.isPauseRequested()) {
                        container.pause();
                    }
                } else if (container.isPauseRequested() || container.isContainerPaused()) {
                    container.resume();
                }
            }
            listenerError.set(containers.stream().allMatch(this::running)
                    ? null
                    : LISTENER_STOPPED);
        } catch (RuntimeException failure) {
            listenerError.set(LISTENER_CONTROL_FAILED + "_" + failure.getClass().getSimpleName()
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

    public Long currentJobExecutionId() {
        return executions.snapshot().currentJobExecutionId();
    }

    @Override
    public ActualState actualState() {
        WorkerExecutionTracker.Snapshot snapshot = executions.snapshot();
        if (listenerError.get() != null) {
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
        if (draining() || listenerError.get() != null) {
            return false;
        }
        List<MessageListenerContainer> containers = workerContainers();
        return !containers.isEmpty() && containers.stream().allMatch(this::accepting);
    }

    @Override
    public List<String> currentExecutions() {
        return executions.snapshot().executionIds();
    }

    @Override
    public List<String> activeLeases() {
        return List.of();
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
        List<MessageListenerContainer> containers = workerContainers();
        String status;
        if (containers.isEmpty()) {
            status = "NOT_AVAILABLE";
        } else if (containers.stream().anyMatch(container -> !running(container))) {
            status = "DOWN";
        } else if (draining() || containers.stream().anyMatch(MessageListenerContainer::isPauseRequested)) {
            status = "PAUSED";
        } else {
            status = "UP";
        }
        return Map.of("springBatchKafkaWorker", status);
    }

    @Override
    public String lastErrorCode() {
        return listenerError.get();
    }

    @Override
    public Map<String, Number> metrics() {
        WorkerExecutionTracker.Snapshot snapshot = executions.snapshot();
        LinkedHashMap<String, Number> values = new LinkedHashMap<>();
        values.put("worker.configuredMaxConcurrency", configuredMaxConcurrency);
        values.put("worker.effectiveConcurrency", effectiveConcurrency());
        values.put("worker.activeInvocations", snapshot.activeInvocations());
        values.put("worker.pendingInvocations", snapshot.pendingInvocations());
        values.put("worker.kafkaListenerContainers", workerContainers().size());
        return Map.copyOf(values);
    }

    @Override
    public long fencingToken() {
        return executions.snapshot().fencingToken();
    }

    private int effectiveConcurrency() {
        return Math.min(configuredMaxConcurrency, runtimePolicy.current().workerConcurrencyLimit());
    }

    private List<MessageListenerContainer> workerContainers() {
        Collection<MessageListenerContainer> containers = listenerRegistry.getAllListenerContainers();
        if (containers == null || containers.isEmpty()) {
            return List.of();
        }
        return containers.stream()
                .filter(container -> workerGroupId.equals(container.getGroupId()))
                .toList();
    }

    private boolean accepting(MessageListenerContainer container) {
        return running(container) && !container.isPauseRequested() && !container.isContainerPaused();
    }

    private boolean running(MessageListenerContainer container) {
        return container.isRunning() && container.isInExpectedState();
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
