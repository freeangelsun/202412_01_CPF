package com.cpf.batch.control.centercut;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.runtime.CenterCutParameterProtector;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CenterCutExecutionServiceNotFoundTest {
    @Test
    void missingJobAndExecutionAreExplicitNotFoundFailures() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider catalogs = mock(CpfVendorSqlCatalogProvider.class);
        when(catalogs.forModule("bat")).thenReturn(catalog);
        when(catalog.required("centercut-execution-find-by-idempotency")).thenReturn("find-idem");
        when(catalog.required("centercut-job-find-active")).thenReturn("find-job");
        when(catalog.required("centercut-execution-detail")).thenReturn("find-execution");
        when(jdbc.queryForList("find-idem", "idem")).thenReturn(List.of());
        when(jdbc.queryForMap("find-job", "job"))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbc.queryForMap("find-execution", "missing"))
                .thenThrow(new EmptyResultDataAccessException(1));
        CenterCutExecutionService service = new CenterCutExecutionService(
                jdbc, new ObjectMapper(), mock(CenterCutParameterProtector.class), catalogs,
                mock(CpfTransactionIdGenerator.class), mock(CpfExecutionIdGenerator.class));
        CenterCutExecutionRequest request = new CenterCutExecutionRequest(
                "job", "idem", Map.of(), "1", 1, 1,
                "operator", "reason", null, null);

        assertThatThrownBy(() -> service.launch(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> org.assertj.core.api.Assertions.assertThat(
                        ((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> service.status("missing"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> org.assertj.core.api.Assertions.assertThat(
                        ((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
