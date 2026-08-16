package com.cpf.batch.execution;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcBatchExecutionControlPlaneAdapterSqlCatalogTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
    private JdbcBatchExecutionControlPlaneAdapter adapter;

    @BeforeEach
    void setUp() {
        when(provider.forModule("bat")).thenReturn(catalog);
        adapter = new JdbcBatchExecutionControlPlaneAdapter(jdbc, provider);
    }

    @Test
    void resolvesExecutionControlQueryFromBatCatalog() {
        when(catalog.required("execution-control-find")).thenReturn("CONTROL_FIND");

        assertFalse(adapter.findReservation("BAT-EXEC-1").isPresent());

        verify(provider).forModule("bat");
        verify(catalog).required("execution-control-find");
    }

    @Test
    void failsClosedBeforeJdbcWhenExecutionControlQueryIsMissing() {
        when(catalog.required("execution-control-find"))
                .thenThrow(new IllegalStateException("missing BAT query"));

        assertThrows(IllegalStateException.class, () -> adapter.findReservation("BAT-EXEC-1"));

        verify(catalog).required("execution-control-find");
        verifyNoInteractions(jdbc);
    }
}
