package com.cpf.platform.operations.runtimecontrol.catalog;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.execution.api.CpfExecutionDefinition;
import com.cpf.foundation.execution.api.CpfExecutionType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfExecutionCatalogRepositoryTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final TransactionTemplate tx = mock(TransactionTemplate.class);
    private final CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider catalogs = mock(CpfVendorSqlCatalogProvider.class);
    private final CpfExecutionCatalogRepository repository;

    CpfExecutionCatalogRepositoryTest() {
        when(catalogs.forModule("cpf")).thenReturn(sql);
        repository = new CpfExecutionCatalogRepository(jdbc, tx, catalogs);
    }

    @Test
    void findByIdUsesCanonicalLiteralAndTrimmedIdentifier() {
        when(sql.required("execution-catalog-find-by-id")).thenReturn("FIND_BY_ID");
        when(jdbc.query(eq("FIND_BY_ID"),
                org.mockito.ArgumentMatchers.<RowMapper<CpfExecutionDefinition>>any(),
                eq("OMBRAC0001"))).thenReturn(List.of());

        assertThat(repository.findById("  OMBRAC0001  ")).isEmpty();
        verify(jdbc).query(eq("FIND_BY_ID"),
                org.mockito.ArgumentMatchers.<RowMapper<CpfExecutionDefinition>>any(),
                eq("OMBRAC0001"));
        assertThatThrownBy(() -> repository.findById("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("standardExecutionId is required");
    }

    @Test
    void upsertRunsOneTransactionalBatchWithExactCanonicalParameterContract() {
        when(sql.required("execution-catalog-upsert")).thenReturn("UPSERT");
        doAnswer(invocation -> {
            java.util.function.Consumer<TransactionStatus> work = invocation.getArgument(0);
            work.accept(mock(TransactionStatus.class));
            return null;
        }).when(tx).executeWithoutResult(any());
        CpfExecutionDefinition definition = new CpfExecutionDefinition(
                "OMBRAC0001", "Member read", CpfExecutionType.ONLINE, "MBR",
                "online", "MemberController", "find", "GET", "/members/{id}",
                "findMember", "Read one member", "MEMBER_READ", true, "PUBLIC",
                true, true, "1.2.3", Instant.parse("2026-08-23T08:00:00Z"));

        repository.upsertAll(List.of(definition));

        verify(jdbc).batchUpdate(eq("UPSERT"), org.mockito.ArgumentMatchers.<List<Object[]>>argThat(rows -> {
            if (rows.size() != 1 || rows.getFirst().length != 18) {
                return false;
            }
            Object[] values = rows.getFirst();
            return values[0].equals("OMBRAC0001")
                    && values[2].equals("ONLINE")
                    && values[12].equals("Y")
                    && values[14].equals("Y")
                    && values[15].equals("Y")
                    && values[17].equals(java.sql.Timestamp.from(definition.discoveredAt()));
        }));
    }
}
