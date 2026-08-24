package com.cpf.batch.worker.centercut;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.centercut.runtime.CenterCutWorkProcessor;
import com.cpf.batch.worker.SpringBatchWorkerRuntimeState;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CenterCutDbWorkerTest {
    @Test
    void actualWorkerPollsTheDbProcessorWithoutBrokerTransport() {
        CenterCutWorkProcessor processor = mock(CenterCutWorkProcessor.class);
        SpringBatchWorkerRuntimeState runtime = mock(SpringBatchWorkerRuntimeState.class);
        when(runtime.acceptingCenterCut()).thenReturn(true, true);
        when(processor.processNext(eq("worker-1"), eq("center-cut"), any(), any()))
                .thenReturn(Optional.empty());
        CenterCutDbWorker worker = new CenterCutDbWorker(
                processor, runtime, "worker-1", "center-cut", 30, 5000, 1);

        worker.poll();

        verify(processor).processNext(eq("worker-1"), eq("center-cut"), any(), any());
    }

    @Test
    void drainStopsNewClaimsBeforeTheProcessor() {
        CenterCutWorkProcessor processor = mock(CenterCutWorkProcessor.class);
        SpringBatchWorkerRuntimeState runtime = mock(SpringBatchWorkerRuntimeState.class);
        when(runtime.acceptingCenterCut()).thenReturn(false);
        CenterCutDbWorker worker = new CenterCutDbWorker(
                processor, runtime, "worker-1", "center-cut", 30, 5000, 1);

        worker.poll();

        verify(processor, never()).processNext(any(), any(), any(), any());
    }
}
