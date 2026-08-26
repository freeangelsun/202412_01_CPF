package com.cpf.batch.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class WorkerExecutionTrackerTest {
    @Test
    void dynamicCapacityLimitsBusinessHandlerInvocationsWithoutOwningDatabaseLeases()
            throws Exception {
        WorkerExecutionTracker tracker = new WorkerExecutionTracker();
        tracker.updateCapacity(1);
        CountDownLatch secondEntered = new CountDownLatch(1);
        CountDownLatch secondFinished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        try (WorkerExecutionTracker.Scope _ = tracker.begin("cpf-1", 101L)) {
            Thread second = Thread.ofVirtual().start(() -> {
                try (WorkerExecutionTracker.Scope ignored = tracker.begin("cpf-2", 102L)) {
                    secondEntered.countDown();
                } catch (Throwable thrown) {
                    failure.set(thrown);
                } finally {
                    secondFinished.countDown();
                }
            });

            assertThat(secondEntered.await(100, TimeUnit.MILLISECONDS)).isFalse();
            assertThat(tracker.snapshot().activeInvocations()).isEqualTo(1);
            assertThat(tracker.snapshot().pendingInvocations()).isEqualTo(1);
            assertThat(tracker.snapshot().executionIds()).containsExactly("cpf-1", "cpf-2");

            tracker.updateCapacity(2);
            assertThat(secondEntered.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(secondFinished.await(2, TimeUnit.SECONDS)).isTrue();
            second.join();
        }

        assertThat(failure.get()).isNull();
        assertThat(tracker.snapshot().inFlightInvocations()).isZero();
    }

    @Test
    void closingScopeTwiceDoesNotCorruptCapacityAccounting() throws Exception {
        WorkerExecutionTracker tracker = new WorkerExecutionTracker();
        WorkerExecutionTracker.Scope scope = tracker.begin("cpf-1", 101L);
        scope.close();
        scope.close();

        assertThat(tracker.snapshot().activeInvocations()).isZero();
        assertThat(tracker.snapshot().pendingInvocations()).isZero();
    }
}
