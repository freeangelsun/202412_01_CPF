package com.cpf.platform.operations.runtimecontrol;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfJdbcOperationCatalogRegistryTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final TransactionTemplate tx = mock(TransactionTemplate.class);
    private final CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider catalogs = mock(CpfVendorSqlCatalogProvider.class);

    @BeforeEach
    void prepareTransactionAndCatalog() {
        when(catalogs.forModule("cpf")).thenReturn(sql);
        when(sql.required(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        }).when(tx).execute(any());
        when(jdbc.query(eq("operation-catalog-find-scoped-application"),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq("MBR"), eq("member-online")))
                .thenReturn(List.of());
    }

    @Test
    void freshExplicitCallerSeedsMissingSystemDomainAndOperationPoliciesTogether() {
        prepareFreshOperation(0);
        CpfJdbcOperationCatalogRegistry registry = registry(List.of("BAT"));

        CpfOperationCatalogRegistry.SyncResult result = registry.synchronize(request());

        assertThat(result.policiesSeeded()).isEqualTo(3);
        verify(jdbc).update(eq("operation-system-domain-access-insert-seed"),
                eq("BAT"), eq("MBR"),
                eq("Initial explicit operation caller System-to-Domain seed [source=YML, revision=GENERATED-1]"),
                any(), any());
        verify(jdbc).update(eq("operation-caller-policy-insert-seed"),
                eq("MBR_SAMPLE_TX_CREATE"), eq("BAT"), eq("YML"), eq("GENERATED-1"), any(),
                eq("Initial operation caller seed"), any(), any());
    }

    @Test
    void existingSystemDomainDecisionIsNeverOverwritten() {
        prepareFreshOperation(1);
        CpfOperationCatalogRegistry.SyncResult result = registry(List.of("BAT")).synchronize(request());

        assertThat(result.policiesSeeded()).isEqualTo(2);
        verify(jdbc, never()).update(eq("operation-system-domain-access-insert-seed"),
                any(), any(), any(), any(), any());
    }

    @Test
    void rediscoveryIsIdempotentAndDoesNotReseedAnyPolicy() {
        when(jdbc.queryForObject(eq("operation-system-registry-exists"), eq(Integer.class), eq("MBR")))
                .thenReturn(1);
        when(jdbc.queryForObject(eq("operation-catalog-exists"), eq(Integer.class), eq("MBR_SAMPLE_TX_CREATE")))
                .thenReturn(1);
        when(jdbc.queryForObject(eq("operation-discovery-exists"), eq(Integer.class),
                eq("MBR_SAMPLE_TX_CREATE"), eq("mbr-1"))).thenReturn(1);

        CpfOperationCatalogRegistry.SyncResult result = registry(List.of("BAT")).synchronize(request());

        assertThat(result.inserted()).isZero();
        assertThat(result.policiesSeeded()).isZero();
        verify(jdbc, never()).queryForObject(eq("operation-system-domain-access-exists"),
                eq(Integer.class), any(), any());
    }

    @Test
    void sameSystemCallerNeedsNoCrossSystemRelation() {
        prepareFreshOperation(0);
        CpfOperationCatalogRegistry.SyncResult result = registry(List.of("MBR")).synchronize(request());

        assertThat(result.policiesSeeded()).isEqualTo(2);
        verify(jdbc, never()).queryForObject(eq("operation-system-domain-access-exists"),
                eq(Integer.class), any(), any());
    }

    private void prepareFreshOperation(int domainAccessCount) {
        when(jdbc.queryForObject(eq("operation-system-registry-exists"), eq(Integer.class), eq("MBR")))
                .thenReturn(1);
        when(jdbc.queryForObject(eq("operation-catalog-exists"), eq(Integer.class), eq("MBR_SAMPLE_TX_CREATE")))
                .thenReturn(0);
        when(jdbc.queryForObject(eq("operation-discovery-exists"), eq(Integer.class),
                eq("MBR_SAMPLE_TX_CREATE"), eq("mbr-1"))).thenReturn(0);
        when(jdbc.queryForObject(eq("operation-system-domain-access-exists"), eq(Integer.class),
                eq("BAT"), eq("MBR"))).thenReturn(domainAccessCount);
    }

    private CpfJdbcOperationCatalogRegistry registry(List<String> callers) {
        return new CpfJdbcOperationCatalogRegistry(
                jdbc, tx, catalogs,
                Clock.fixed(Instant.parse("2026-08-24T09:42:00Z"), ZoneOffset.UTC),
                callers, "YML", "GENERATED-1");
    }

    private static CpfOperationCatalogRegistry.SyncRequest request() {
        return new CpfOperationCatalogRegistry.SyncRequest(
                "MBR", "MBR", "member-online", "mbr-1", "1.0.0", "abc123",
                List.of(new CpfOperationCatalogRegistry.Operation(
                        "MBR_SAMPLE_TX_CREATE", "Create sample", "Create sample", "MBR", "MBR",
                        "member-online", "POST", "/samples", "SampleTransactionController",
                        "create", "fingerprint")));
    }
}
