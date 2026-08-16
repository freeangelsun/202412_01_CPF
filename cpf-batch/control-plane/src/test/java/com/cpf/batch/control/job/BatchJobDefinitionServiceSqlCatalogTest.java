package com.cpf.batch.control.job;

import com.cpf.batch.api.BatchJobDefinitionControlPort;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BatchJobDefinitionServiceSqlCatalogTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
    private BatchJobDefinitionService service;

    @BeforeEach
    void setUp() {
        when(provider.forModule("bat")).thenReturn(catalog);
        service = new BatchJobDefinitionService(jdbc, new ObjectMapper(), provider);
    }

    @Test
    void resolvesDefinitionStateQueryFromBatCatalog() {
        when(catalog.required("definition-state-find")).thenReturn("DEFINITION_STATE_FIND");
        when(jdbc.queryForList("DEFINITION_STATE_FIND", "BAT.QA37", 7L)).thenReturn(List.of(Map.of(
                "job_id", "BAT.QA37",
                "definition_version", 7L,
                "definition_state", "APPROVAL",
                "row_version", 3L,
                "checksum", "c".repeat(64),
                "created_by", "maker")));

        BatchJobDefinitionControlPort.DefinitionState state = service.state("BAT.QA37", 7L);

        assertEquals("BAT.QA37", state.jobId());
        assertEquals(7L, state.definitionVersion());
        assertEquals("APPROVAL", state.state());
        verify(provider).forModule("bat");
        verify(catalog).required("definition-state-find");
    }

    @Test
    void failsClosedWhenDefinitionQueryIsMissing() {
        when(catalog.required("definition-state-find"))
                .thenThrow(new IllegalStateException("missing BAT query"));

        assertThrows(IllegalStateException.class, () -> service.state("BAT.QA37", 7L));
        verifyNoInteractions(jdbc);
    }
}
