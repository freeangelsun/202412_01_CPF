package com.cpf.batch.control.compat;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatCenterCutQueryCatalogTest {
    private static final List<String> REQUIRED_KEYS = List.of(
            "centercut-operations-find-jobs",
            "centercut-operations-find-job-detail",
            "centercut-operations-find-parameters",
            "centercut-operations-summarize-items",
            "centercut-operations-summarize-results",
            "centercut-operations-find-targets",
            "centercut-operations-find-results",
            "centercut-operations-find-result-detail");

    @Test
    void allCanonicalStatementsAreResolvedFromTheBatVendorCatalogAtStartup() {
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(anyString()))
                .thenAnswer(invocation -> "SQL:" + invocation.getArgument(0, String.class));

        BatCenterCutOperationsService service =
                new BatCenterCutOperationsService(mock(JdbcTemplate.class), provider);

        assertThat(service).isNotNull();
        verify(provider).forModule("bat");
        REQUIRED_KEYS.forEach(key -> verify(catalog).required(key));
    }
}
