package com.cpf.batch.worker;

import com.cpf.batch.worker.internal.JdbcWorkerLeaseRepository;
import com.cpf.batch.api.ActualState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkerRuntimeLeaseLossTest {
    private WorkerRuntime runtime;

    @AfterEach
    void closeRuntime() {
        if (runtime != null) {
            runtime.close();
        }
    }

    @Test
    void lostLeaseDoesNotFreeConcurrencyUntilTheOldBusinessThreadEnds() throws Exception {
        JdbcWorkerLeaseRepository repository = mock(JdbcWorkerLeaseRepository.class);
        JobPackDispatcher dispatcher = mock(JobPackDispatcher.class);
        var lease = new JdbcWorkerLeaseRepository.Lease(
                41L, "worker-a", "lease-a", 7L, Instant.now().plusSeconds(30));
        when(repository.claim(eq("worker-a"), eq("1.0.0"), anyList(), any()))
                .thenReturn(Optional.of(lease), Optional.empty());
        when(repository.renew(eq(lease), any())).thenReturn(false);

        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            assertThat(release.await(3, TimeUnit.SECONDS)).isTrue();
            return null;
        }).when(dispatcher).execute(lease);

        runtime = new WorkerRuntime(repository, dispatcher, "worker-a", "1.0.0",
                "GENERAL", 1, 30);
        runtime.poll();
        assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();

        runtime.renew();

        assertThat(runtime.currentExecutionId()).isEqualTo(41L);
        assertThat(runtime.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(runtime.ready()).isFalse();
        assertThat(runtime.lastErrorCode()).isEqualTo("BAT_WORKER_LEASE_LOST");
        assertThat(runtime.availableCapacity()).isZero();
        runtime.poll();
        verify(repository, times(1)).claim(eq("worker-a"), eq("1.0.0"), anyList(), any());

        release.countDown();
        awaitExecutionCount(0);
        assertThat(runtime.ready()).isTrue();
        assertThat(runtime.availableCapacity()).isEqualTo(1);
    }

    private void awaitExecutionCount(int expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while ((runtime.currentExecutions().size() != expected
                || (expected == 0 && !runtime.ready()))
                && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertThat(runtime.currentExecutions()).hasSize(expected);
    }
}
