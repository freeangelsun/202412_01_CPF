package com.cpf.batch.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.messaging.api.CpfBrokerConsumerControl;
import com.cpf.messaging.api.CpfBrokerConsumerControlPort;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpringBatchWorkerRuntimeStateTest {
    private static final int PREFETCH = 7;

    @Test
    void missingBrokerControlPortIsFailClosed() {
        SpringBatchWorkerRuntimeState runtime = runtime(
                List.of(), new BatchRuntimePolicy(), new WorkerExecutionTracker(), 4);

        runtime.reconcile();

        assertThat(runtime.ready()).isFalse();
        assertThat(runtime.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(runtime.availableCapacity()).isZero();
        assertThat(runtime.lastErrorCode())
                .isEqualTo(SpringBatchWorkerRuntimeState.CONTROL_PORT_UNAVAILABLE);
        assertThat(runtime.dependencyHealth())
                .containsEntry("brokerConsumerControl", "NOT_AVAILABLE");
    }

    @Test
    void drainAndResumeApplyProviderNeutralBrokerControl() throws Exception {
        CpfBrokerConsumerControlPort controlPort = mock(CpfBrokerConsumerControlPort.class);
        WorkerExecutionTracker tracker = new WorkerExecutionTracker();
        SpringBatchWorkerRuntimeState runtime = runtime(
                List.of(controlPort), new BatchRuntimePolicy(), tracker, 4);
        runtime.reconcile();
        verify(controlPort).apply(new CpfBrokerConsumerControl(false, 4, PREFETCH));
        assertThat(runtime.ready()).isTrue();
        assertThat(runtime.availableCapacity()).isEqualTo(4);

        WorkerExecutionTracker.Scope inFlight = tracker.begin("cpf-101", 101L, 19L);
        runtime.drain();

        verify(controlPort).apply(new CpfBrokerConsumerControl(true, 4, PREFETCH));
        assertThat(runtime.draining()).isTrue();
        assertThat(runtime.drained()).isFalse();
        assertThat(runtime.actualState()).isEqualTo(ActualState.DRAINING);
        assertThat(runtime.currentExecutions()).containsExactly("cpf-101");
        assertThat(runtime.activeLeases())
                .containsExactly("lease-epoch=1;fencing-token=19");
        assertThat(runtime.fencingToken()).isEqualTo(19L);

        inFlight.close();
        assertThat(runtime.drained()).isTrue();

        runtime.resume();
        verify(controlPort, times(2)).apply(new CpfBrokerConsumerControl(false, 4, PREFETCH));
        assertThat(runtime.draining()).isFalse();
    }

    @Test
    void runtimePolicyChangesTheActualHandlerAdmissionCapacity() {
        CpfBrokerConsumerControlPort controlPort = mock(CpfBrokerConsumerControlPort.class);
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        WorkerExecutionTracker tracker = new WorkerExecutionTracker();
        SpringBatchWorkerRuntimeState runtime = runtime(List.of(controlPort), policy, tracker, 8);
        runtime.reconcile();

        assertThat(tracker.snapshot().capacityLimit()).isEqualTo(8);
        policy.replaceConcurrency(1L, true, 3);
        runtime.reconcile();
        verify(controlPort).apply(new CpfBrokerConsumerControl(false, 3, PREFETCH));
        assertThat(tracker.snapshot().capacityLimit()).isEqualTo(3);
        assertThat(runtime.availableCapacity()).isEqualTo(3);

        policy.replaceConcurrency(2L, false, 3);
        runtime.reconcile();
        verify(controlPort).apply(new CpfBrokerConsumerControl(true, 3, PREFETCH));
        assertThat(runtime.ready()).isFalse();
        assertThat(runtime.availableCapacity()).isZero();
        assertThat(runtime.actualState()).isEqualTo(ActualState.DRAINING);
    }

    private static SpringBatchWorkerRuntimeState runtime(
            List<CpfBrokerConsumerControlPort> controlPorts,
            BatchRuntimePolicy policy,
            WorkerExecutionTracker tracker,
            int maxConcurrency) {
        return new SpringBatchWorkerRuntimeState(
                controlPorts,
                policy,
                tracker,
                "worker-1",
                "1.0.0",
                "GENERAL,FILE",
                maxConcurrency,
                PREFETCH);
    }
}
