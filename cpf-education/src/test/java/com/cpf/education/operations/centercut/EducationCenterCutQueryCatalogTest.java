package com.cpf.education.operations.centercut;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationCenterCutQueryCatalogTest {
    private static final List<String> REQUIRED_KEYS = List.of(
            "centercut-count-results-by-status",
            "centercut-delete-smoke-results",
            "centercut-find-ready-targets",
            "centercut-find-result-snapshots",
            "centercut-mark-result-target",
            "centercut-mark-running",
            "centercut-operations-find-result-detail",
            "centercut-operations-find-results",
            "centercut-operations-find-targets",
            "centercut-operations-summarize-results",
            "centercut-operations-summarize-targets",
            "centercut-reset-smoke-targets",
            "centercut-upsert-result");

    @Test
    void allCenterCutStatementsAreResolvedFromTheRefVendorCatalogAtStartup() {
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        when(provider.forModule("ref")).thenReturn(catalog);
        when(catalog.required(anyString()))
                .thenAnswer(invocation -> "SQL:" + invocation.getArgument(0, String.class));

        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        EducationCenterCutTargetRepository targetRepository =
                new EducationCenterCutTargetRepository(jdbc, provider);
        EduCenterCutOperationsExtension operations =
                new EduCenterCutOperationsExtension(jdbc, provider);

        assertThat(targetRepository).isNotNull();
        assertThat(operations).isNotNull();
        verify(provider, org.mockito.Mockito.times(2)).forModule("ref");
        REQUIRED_KEYS.forEach(key -> verify(catalog).required(key));
    }
}
