package com.cpf.batch.scheduler;

import com.cpf.batch.scheduler.internal.JdbcSchedulerLeaderRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        coordinator.elect();
        assertThat(coordinator.fencingToken()).isEqualTo(11L);

        assertThatThrownBy(() -> coordinator.assertLeader(11L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fenced");
        assertThat(coordinator.fencingToken()).isZero();
    }
}
