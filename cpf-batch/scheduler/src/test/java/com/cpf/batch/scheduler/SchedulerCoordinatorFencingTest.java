package com.cpf.batch.scheduler;

import com.cpf.batch.scheduler.internal.JdbcSchedulerLeaderRepository;
import com.cpf.batch.api.ActualState;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class SchedulerCoordinatorFencingTest {
    @Test
    void localLeaseIsClearedWhenDatabaseNoLongerOwnsIt() {
        JdbcSchedulerLeaderRepository repository = mock(JdbcSchedulerLeaderRepository.class);
        var lease = new JdbcSchedulerLeaderRepository.Lease(
                "scheduler-a", 11L, Instant.now().plusSeconds(15));
        when(repository.acquire(eq(SchedulerCoordinator.LEASE_KEY), eq("scheduler-a"), any()))
                .thenReturn(Optional.of(lease));
        when(repository.isCurrent(SchedulerCoordinator.LEASE_KEY, lease)).thenReturn(false);
        SchedulerCoordinator coordinator = new SchedulerCoordinator(repository, "scheduler-a", 15);

        assertThat(coordinator.ready()).isFalse();
        assertThat(coordinator.dependencyHealth())
                .containsEntry("schedulerLeaseStore", "UNKNOWN");
        coordinator.elect();
        assertThat(coordinator.fencingToken()).isEqualTo(11L);
        assertThat(coordinator.ready()).isTrue();

        assertThatThrownBy(() -> coordinator.assertLeader(11L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fenced");
        assertThat(coordinator.fencingToken()).isZero();
        assertThat(coordinator.ready()).isFalse();
    }

    @Test
    void overlappingElectionDoesNotClearWinningLease() throws Exception {
        JdbcSchedulerLeaderRepository repository = mock(JdbcSchedulerLeaderRepository.class);
        var won = new JdbcSchedulerLeaderRepository.Lease(
                "scheduler-a", 21L, Instant.now().plusSeconds(15));
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(repository.acquire(eq(SchedulerCoordinator.LEASE_KEY), eq("scheduler-a"), any()))
                .thenAnswer(invocation -> {
                    entered.countDown();
                    release.await();
                    return Optional.of(won);
                });
        SchedulerCoordinator coordinator = new SchedulerCoordinator(repository, "scheduler-a", 15);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread first = new Thread(() -> {
            try { coordinator.elect(); } catch (Throwable thrown) { failure.set(thrown); }
        });
        first.start();
        entered.await();

        coordinator.elect();
        release.countDown();
        first.join();

        assertThat(failure.get()).isNull();
        assertThat(coordinator.fencingToken()).isEqualTo(21L);
        verify(repository, times(1)).acquire(eq(SchedulerCoordinator.LEASE_KEY), eq("scheduler-a"), any());
    }

    @Test
    void electionStoreFailureIsReportedAsDegradedAndRethrown() {
        JdbcSchedulerLeaderRepository repository = mock(JdbcSchedulerLeaderRepository.class);
        when(repository.acquire(
                eq(SchedulerCoordinator.LEASE_KEY),
                eq("scheduler-a"),
                any()))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        SchedulerCoordinator coordinator = new SchedulerCoordinator(repository, "scheduler-a", 15);

        assertThatThrownBy(coordinator::elect)
                .isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(coordinator.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(coordinator.ready()).isFalse();
        assertThat(coordinator.dependencyHealth())
                .containsEntry("schedulerLeaseStore", "DOWN");
        assertThat(coordinator.lastErrorCode())
                .isEqualTo("BAT_SCHEDULER_ELECTION_FAILED");
    }
}
