package com.cpf.batch.scheduler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.scheduler.internal.JdbcSchedulerLeaderRepository;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver;
import com.cpf.common.calendar.CmnBusinessCalendar;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

class SchedulerDispatchServiceUnknownPersistenceTest {
    private JdbcTemplate jdbc;
    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private SchedulerDispatchService service;
    private CpfVendorSqlCatalog catalog;
    private JdbcSchedulerLeaderRepository.Lease lease;
    private Timestamp scheduledAt;

    @BeforeEach
    void setUp() {
        SchedulerCoordinator coordinator = mock(SchedulerCoordinator.class);
        jdbc = mock(JdbcTemplate.class);
        CmnBusinessCalendar calendar = mock(CmnBusinessCalendar.class);
        transactionManager = mock(PlatformTransactionManager.class);
        transactionStatus = mock(TransactionStatus.class);
        catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        BatchExecutionControlPort executionControl = mock(BatchExecutionControlPort.class);
        BatchApprovedLaunchRequestResolver launchResolver =
                mock(BatchApprovedLaunchRequestResolver.class);

        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required("scheduler-trigger-mark-unknown")).thenReturn("mark-unknown");
        when(catalog.required("scheduler-trigger-find-dispatchable"))
                .thenReturn("select * from trigger where status in ('CREATED','FAILED')");
        when(catalog.required("scheduler-trigger-claim"))
                .thenReturn("update trigger set status='DISPATCHING' where status in ('CREATED','FAILED')");

        service = new SchedulerDispatchService(
                coordinator,
                jdbc,
                calendar,
                transactionManager,
                provider,
                executionControl,
                launchResolver);
        lease = new JdbcSchedulerLeaderRepository.Lease(
                "scheduler-1", 7L, Instant.now().plusSeconds(30));
        scheduledAt = Timestamp.from(Instant.parse("2026-08-05T01:00:00Z"));
    }

    @Test
    void persistsUnknownExactlyOnceWhenExternalResultIsUncertain() {
        RuntimeException original = new IllegalStateException("after-start failure");
        when(jdbc.update(
                "mark-unknown",
                "IllegalStateException",
                "schedule-1",
                scheduledAt,
                "scheduler-1",
                7L)).thenReturn(1);

        service.markUnknownOrFail(original, "schedule-1", scheduledAt, lease);

        verify(transactionManager).commit(transactionStatus);
        verify(jdbc).update(
                "mark-unknown",
                "IllegalStateException",
                "schedule-1",
                scheduledAt,
                "scheduler-1",
                7L);
    }

    @Test
    void failsClosedWhenUnknownEvidenceCannotBePersisted() {
        RuntimeException original = new IllegalStateException("after-start failure");
        when(jdbc.update(
                "mark-unknown",
                "IllegalStateException",
                "schedule-1",
                scheduledAt,
                "scheduler-1",
                7L)).thenReturn(0);

        assertThatThrownBy(() ->
                service.markUnknownOrFail(original, "schedule-1", scheduledAt, lease))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SCHEDULER_UNKNOWN_PERSISTENCE_REJECTED")
                .hasCause(original);

        verify(transactionManager).commit(transactionStatus);
    }
    @Test
    void acceptsAutomaticDispatchSqlOnlyWhenUnknownIsExcluded() {
        assertThatCode(service::assertAutomaticDispatchSqlSafe).doesNotThrowAnyException();
    }

    @Test
    void failsClosedWhenVendorSqlWouldAutomaticallyRedispatchUnknown() {
        when(catalog.required("scheduler-trigger-find-dispatchable"))
                .thenReturn("select * from trigger where status in ('CREATED','UNKNOWN','FAILED')");

        assertThatThrownBy(service::assertAutomaticDispatchSqlSafe)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("SCHEDULER_UNKNOWN_AUTO_DISPATCH_SQL_REJECTED:scheduler-trigger-find-dispatchable");
    }

    @Test
    void failsClosedWhenVendorClaimSqlWouldAutomaticallyRedispatchUnknown() {
        when(catalog.required("scheduler-trigger-claim"))
                .thenReturn("update trigger set status='DISPATCHING' where status in ('CREATED','UNKNOWN','FAILED')");

        assertThatThrownBy(service::assertAutomaticDispatchSqlSafe)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("SCHEDULER_UNKNOWN_AUTO_DISPATCH_SQL_REJECTED:scheduler-trigger-claim");
    }

}
