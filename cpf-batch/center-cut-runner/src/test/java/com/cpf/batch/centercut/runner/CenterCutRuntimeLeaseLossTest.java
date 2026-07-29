package com.cpf.batch.centercut.runner;

import com.cpf.batch.centercut.runner.internal.JdbcCenterCutClaimRepository;
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

class CenterCutRuntimeLeaseLossTest {
    private CenterCutRuntime runtime;

    @AfterEach
    void closeRuntime() {
        if (runtime != null) {
            runtime.close();
        }
    }

    @Test
    void lostLeaseDoesNotPermitAnotherItemWhileOldHandlerStillRuns() throws Exception {
        JdbcCenterCutClaimRepository repository = mock(JdbcCenterCutClaimRepository.class);
        CenterCutDispatcher dispatcher = mock(CenterCutDispatcher.class);
        var claim = new JdbcCenterCutClaimRepository.Claim(
                51L, "center-a", "claim-a", 9L, Instant.now().plusSeconds(30), "execution-a");
        when(repository.claim(eq("center-a"), eq("center"), any())).thenReturn(Optional.of(claim));
        when(repository.renew(eq(claim), any())).thenReturn(false);

        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            assertThat(release.await(3, TimeUnit.SECONDS)).isTrue();
            return null;
        }).when(dispatcher).execute(claim);

        runtime = new CenterCutRuntime(repository, dispatcher, "center-a", "center", 30);
        runtime.poll();
        assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();

        runtime.renew();
        runtime.poll();

        assertThat(runtime.currentExecutions()).containsExactly("51");
        assertThat(runtime.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(runtime.ready()).isFalse();
        assertThat(runtime.lastErrorCode()).isEqualTo("BAT_CENTER_CUT_LEASE_LOST");
        verify(repository, times(1)).claim(eq("center-a"), eq("center"), any());

        release.countDown();
        awaitExecutionCount(0);
        assertThat(runtime.ready()).isTrue();
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
