package com.cpf.batch.scheduler;

import com.cpf.batch.scheduler.internal.JdbcSchedulerLeaderRepository;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchProjectionScheduleSynchronizerSqlCatalogTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final SchedulerCoordinator coordinator = mock(SchedulerCoordinator.class);
    private final CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
    private BatchProjectionScheduleSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        when(provider.forModule("bat")).thenReturn(catalog);
        synchronizer = new BatchProjectionScheduleSynchronizer(
                jdbc, new ObjectMapper(), coordinator, provider);
        when(coordinator.fencingToken()).thenReturn(7L);
        when(coordinator.assertLeader(7L)).thenReturn(
                new JdbcSchedulerLeaderRepository.Lease(
                        "scheduler-1", 7L, Instant.parse("2026-08-02T03:00:00Z")));
    }

    @Test
    void resolvesProjectionQueryFromBatCatalog() {
        when(catalog.required("projection-sync-outbox-find")).thenReturn("OUTBOX_FIND");
        doReturn(List.of()).when(jdbc).query(
                eq("OUTBOX_FIND"),
                org.mockito.ArgumentMatchers.<RowMapper<Object>>any());

        synchronizer.synchronize();

        verify(provider).forModule("bat");
        verify(catalog).required("projection-sync-outbox-find");
    }

    @Test
    void failsClosedWhenProjectionQueryIsMissing() {
        when(catalog.required("projection-sync-outbox-find"))
                .thenThrow(new IllegalStateException("missing BAT query"));

        assertThrows(IllegalStateException.class, synchronizer::synchronize);
        verify(catalog).required("projection-sync-outbox-find");
    }
}
