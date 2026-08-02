package com.cpf.batch.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

class SpringBatchWorkerRuntimeStateTest {
    private static final String GROUP_ID = "cpf-batch-remote-workers-v2";

    @Test
    void missingKafkaWorkerListenerIsFailClosed() {
        KafkaListenerEndpointRegistry registry = mock(KafkaListenerEndpointRegistry.class);
        when(registry.getAllListenerContainers()).thenReturn(List.of());
        SpringBatchWorkerRuntimeState runtime = runtime(
                registry, new BatchRuntimePolicy(), new WorkerExecutionTracker(), 4);

        runtime.reconcile();

        assertThat(runtime.ready()).isFalse();
        assertThat(runtime.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(runtime.availableCapacity()).isZero();
        assertThat(runtime.lastErrorCode())
                .isEqualTo(SpringBatchWorkerRuntimeState.LISTENER_UNAVAILABLE);
        assertThat(runtime.dependencyHealth())
                .containsEntry("springBatchKafkaWorker", "NOT_AVAILABLE");
    }

    @Test
    void drainAndResumeControlOnlyTheConfiguredWorkerConsumerGroup() throws Exception {
        KafkaListenerEndpointRegistry registry = mock(KafkaListenerEndpointRegistry.class);
        MessageListenerContainer worker = runningContainer(GROUP_ID);
        MessageListenerContainer unrelated = runningContainer("unrelated-group");
        when(registry.getAllListenerContainers()).thenReturn(List.of(worker, unrelated));
        WorkerExecutionTracker tracker = new WorkerExecutionTracker();
        SpringBatchWorkerRuntimeState runtime = runtime(
                registry, new BatchRuntimePolicy(), tracker, 4);
        runtime.reconcile();
        assertThat(runtime.ready()).isTrue();
        assertThat(runtime.availableCapacity()).isEqualTo(4);

        WorkerExecutionTracker.Scope inFlight = tracker.begin("cpf-101", 101L, 19L);
        runtime.drain();

        verify(worker).pause();
        assertThat(runtime.draining()).isTrue();
        assertThat(runtime.drained()).isFalse();
        assertThat(runtime.actualState()).isEqualTo(ActualState.DRAINING);
        assertThat(runtime.currentExecutions()).containsExactly("cpf-101");
        assertThat(runtime.activeLeases()).isEmpty();
        assertThat(runtime.fencingToken()).isEqualTo(19L);

        inFlight.close();
        assertThat(runtime.drained()).isTrue();

        when(worker.isPauseRequested()).thenReturn(true);
        runtime.resume();
        verify(worker).resume();
        assertThat(runtime.draining()).isFalse();
    }

    @Test
    void runtimePolicyChangesTheActualHandlerAdmissionCapacity() {
        KafkaListenerEndpointRegistry registry = mock(KafkaListenerEndpointRegistry.class);
        MessageListenerContainer worker = runningContainer(GROUP_ID);
        when(registry.getAllListenerContainers()).thenReturn(List.of(worker));
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        WorkerExecutionTracker tracker = new WorkerExecutionTracker();
        SpringBatchWorkerRuntimeState runtime = runtime(registry, policy, tracker, 8);
        runtime.reconcile();

        assertThat(tracker.snapshot().capacityLimit()).isEqualTo(8);
        policy.replaceConcurrency(1L, true, 3);
        runtime.reconcile();
        assertThat(tracker.snapshot().capacityLimit()).isEqualTo(3);
        assertThat(runtime.availableCapacity()).isEqualTo(3);

        policy.replaceConcurrency(2L, false, 3);
        runtime.reconcile();
        verify(worker).pause();
        assertThat(runtime.ready()).isFalse();
        assertThat(runtime.availableCapacity()).isZero();
        assertThat(runtime.actualState()).isEqualTo(ActualState.DRAINING);
    }

    private static SpringBatchWorkerRuntimeState runtime(
            KafkaListenerEndpointRegistry registry,
            BatchRuntimePolicy policy,
            WorkerExecutionTracker tracker,
            int maxConcurrency) {
        return new SpringBatchWorkerRuntimeState(
                registry,
                GROUP_ID,
                policy,
                tracker,
                "worker-1",
                "1.0.0",
                "GENERAL,FILE",
                maxConcurrency);
    }

    private static MessageListenerContainer runningContainer(String groupId) {
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        when(container.getGroupId()).thenReturn(groupId);
        when(container.isRunning()).thenReturn(true);
        when(container.isInExpectedState()).thenReturn(true);
        return container;
    }
}
