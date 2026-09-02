package com.cpf.batch.scheduler.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcSchedulerLeaderRepositoryTest {
    @Test
    void heartbeatPassesOnlyLeaseDurationToTheVendorPack() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required("scheduler-leader-heartbeat")).thenReturn("scheduler-leader-heartbeat");
        when(jdbc.update("scheduler-leader-heartbeat", 7_000_000L, "cpf-scheduler", "scheduler-a", 3L))
                .thenReturn(1);
        JdbcSchedulerLeaderRepository repository = new JdbcSchedulerLeaderRepository(jdbc, provider);

        assertThat(repository.heartbeat("cpf-scheduler",
                new JdbcSchedulerLeaderRepository.Lease("scheduler-a", 3L, Instant.now().plusSeconds(7)),
                Duration.ofSeconds(7))).isTrue();
        verify(jdbc).update("scheduler-leader-heartbeat", 7_000_000L, "cpf-scheduler", "scheduler-a", 3L);
    }

    @Test
    void rejectsLeaseDurationBelowDatabaseMicrosecondPrecision() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        JdbcSchedulerLeaderRepository repository = new JdbcSchedulerLeaderRepository(jdbc, provider);

        assertThatThrownBy(() -> repository.heartbeat("cpf-scheduler",
                new JdbcSchedulerLeaderRepository.Lease("scheduler-a", 3L, Instant.now()), Duration.ofNanos(999)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one microsecond");
    }
}
