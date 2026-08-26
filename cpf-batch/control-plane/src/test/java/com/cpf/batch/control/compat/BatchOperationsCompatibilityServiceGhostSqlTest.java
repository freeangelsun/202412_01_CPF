package com.cpf.batch.control.compat;

import com.cpf.batch.api.CpfBatchRiskCommand;
import com.cpf.common.calendar.api.CpfCalendarService;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchOperationsCompatibilityServiceGhostSqlTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CpfCalendarService calendar = mock(CpfCalendarService.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private final CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
    private final CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
    private BatchOperationsCompatibilityService service;

    @BeforeEach
    void setUp() {
        when(provider.forModule("bat")).thenReturn(catalog);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(mock(TransactionStatus.class));
        JdbcBatchRiskCommandLedger ledger = mock(JdbcBatchRiskCommandLedger.class);
        when(ledger.reserve(any(CpfBatchRiskCommand.class)))
                .thenReturn(JdbcBatchRiskCommandLedger.Decision.created());
        service = new BatchOperationsCompatibilityService(
                jdbc,
                calendar,
                transactionManager,
                provider,
                new CpfBatchRiskCommandCoordinator(ledger, new ObjectMapper()));
    }

    @Test
    void ghostFinishBindsStatusOperatorAndExecutionIdInCanonicalOrder() {
        when(catalog.required("compat-execution-lock")).thenReturn("EXECUTION_LOCK");
        when(catalog.required("compat-execution-finish-ghost")).thenReturn("EXECUTION_FINISH_GHOST");
        when(catalog.required("compat-operation-audit")).thenReturn("OPERATION_AUDIT");
        when(jdbc.queryForList("EXECUTION_LOCK", 41L)).thenReturn(List.of(Map.of(
                "execution_id", 41L,
                "job_id", "BAT.QA37",
                "execution_status", "RUNNING",
                "last_heartbeat_at", LocalDateTime.now().minusMinutes(10),
                "row_version", 3L)));
        when(jdbc.update("EXECUTION_FINISH_GHOST", "FAILED", "qa-user", 41L, 3L)).thenReturn(1);

        service.actGhostExecution(41L, "FAIL", new CpfBatchRiskCommand(
                "actGhostExecution",
                "bat_execution",
                "41",
                "FAIL",
                "qa-user",
                "stale heartbeat",
                "APR-QA37",
                "IDEM-QA37-GHOST-41",
                3L,
                ""));

        verify(jdbc).update("EXECUTION_FINISH_GHOST", "FAILED", "qa-user", 41L, 3L);
    }
}
