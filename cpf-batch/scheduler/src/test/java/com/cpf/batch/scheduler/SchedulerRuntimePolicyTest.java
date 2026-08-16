package com.cpf.batch.scheduler;

import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver;
import com.cpf.common.calendar.CmnBusinessCalendar;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SchedulerRuntimePolicyTest {
    @Test
    void sharedRuntimePolicyChangesDispatchAndCalendarGates() {
        SchedulerCoordinator coordinator = mock(SchedulerCoordinator.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(mock(CpfVendorSqlCatalog.class));
        BatchExecutionControlPort executionControl = mock(BatchExecutionControlPort.class);
        BatchApprovedLaunchRequestResolver launchRequestResolver = mock(BatchApprovedLaunchRequestResolver.class);
        SchedulerDispatchService service = new SchedulerDispatchService(
                coordinator,
                jdbc,
                mock(CmnBusinessCalendar.class),
                mock(PlatformTransactionManager.class),
                provider,
                executionControl,
                launchRequestResolver);
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        service.setRuntimePolicy(policy);
        assertTrue(service.runtimeEnabled());
        assertTrue(service.calendarRuntimeEnabled());
        policy.replaceSchedule(1L, false);
        policy.replaceCalendar(2L, false);
        assertFalse(service.runtimeEnabled());
        assertFalse(service.calendarRuntimeEnabled());

        service.dispatchDue();

        verifyNoInteractions(coordinator, jdbc, executionControl, launchRequestResolver);
    }
}
