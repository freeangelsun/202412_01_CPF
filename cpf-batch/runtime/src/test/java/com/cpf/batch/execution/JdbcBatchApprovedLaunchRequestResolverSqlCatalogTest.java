package com.cpf.batch.execution;

import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver.ManualContext;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver.TriggerContext;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcBatchApprovedLaunchRequestResolverSqlCatalogTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
    private JdbcBatchApprovedLaunchRequestResolver resolver;

    @BeforeEach
    void setUp() {
        when(provider.forModule("bat")).thenReturn(catalog);
        resolver = new JdbcBatchApprovedLaunchRequestResolver(jdbc, new ObjectMapper(), provider);
    }

    @Test
    void resolvesTriggerAndManualQueriesFromBatCatalog() {
        when(catalog.required("execution-approved-launch-find-trigger")).thenReturn("TRIGGER_FIND");
        when(catalog.required("execution-approved-launch-find-manual")).thenReturn("MANUAL_FIND");

        TriggerContext trigger = new TriggerContext(
                "SCH-1", "BAT.QA37", 7L, "a".repeat(64), LocalDate.of(2026, 8, 2),
                OffsetDateTime.of(2026, 8, 2, 3, 0, 0, 0, ZoneOffset.UTC), 11L, "trigger-key");
        ManualContext manual = new ManualContext(
                "APR-1", "operator", "QA37", "manual-key", 12L, Map.of());

        assertThrows(IllegalStateException.class, () -> resolver.resolve(trigger));
        assertThrows(IllegalStateException.class, () -> resolver.resolve(manual));

        verify(catalog).required("execution-approved-launch-find-trigger");
        verify(catalog).required("execution-approved-launch-find-manual");
    }

    @Test
    void failsClosedBeforeJdbcWhenApprovedLaunchQueryIsMissing() {
        when(catalog.required("execution-approved-launch-find-manual"))
                .thenThrow(new IllegalStateException("missing BAT query"));
        ManualContext manual = new ManualContext(
                "APR-1", "operator", "QA37", "manual-key", 12L, Map.of());

        assertThrows(IllegalStateException.class, () -> resolver.resolve(manual));

        verify(catalog).required("execution-approved-launch-find-manual");
        verifyNoInteractions(jdbc);
    }
}
