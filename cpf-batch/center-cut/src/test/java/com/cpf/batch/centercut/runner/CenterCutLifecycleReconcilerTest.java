package com.cpf.batch.centercut.runner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.centercut.runner.internal.JdbcCenterCutClaimRepository;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;

class CenterCutLifecycleReconcilerTest {
    @Test
    void recoversExpiredClaimsWithoutWaitingForAnotherStepLaunch() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutClaimRepository claims = mock(JdbcCenterCutClaimRepository.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);

        CenterCutLifecycleReconciler reconciler =
                new CenterCutLifecycleReconciler(jdbc, provider, claims);

        reconciler.recoverExpiredClaims();

        verify(claims).recoverExpiredToUnknown();
    }

    @Test
    void lifecycleReconcileUsesTheVendorCatalogStatement() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutClaimRepository claims = mock(JdbcCenterCutClaimRepository.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required("centercut-lifecycle-mark-drained-paused"))
                .thenReturn("mark-drained-paused");

        CenterCutLifecycleReconciler reconciler =
                new CenterCutLifecycleReconciler(jdbc, provider, claims);

        reconciler.reconcile();

        InOrder order = inOrder(catalog, jdbc);
        order.verify(catalog).required("centercut-lifecycle-mark-drained-paused");
        order.verify(jdbc).update("mark-drained-paused");
    }
}
