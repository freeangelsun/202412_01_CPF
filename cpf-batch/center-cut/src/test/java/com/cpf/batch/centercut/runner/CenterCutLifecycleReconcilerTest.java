package com.cpf.batch.centercut.runner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;

class CenterCutLifecycleReconcilerTest {
    @Test
    void lifecycleReconcileUsesTheVendorCatalogStatement() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required("centercut-lifecycle-mark-drained-paused"))
                .thenReturn("mark-drained-paused");

        CenterCutLifecycleReconciler reconciler =
                new CenterCutLifecycleReconciler(jdbc, provider);

        reconciler.reconcile();

        InOrder order = inOrder(catalog, jdbc);
        order.verify(catalog).required("centercut-lifecycle-mark-drained-paused");
        order.verify(jdbc).update("mark-drained-paused");
    }
}
