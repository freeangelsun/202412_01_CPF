package com.cpf.batch.control.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.RuntimeCommand;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcRuntimeCommandRepositoryIdempotencyTest {
    @Test
    void duplicateInsertReadsTheExistingIdempotencyRowOutsideAnAbortedTransaction() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(sql);
        when(sql.required("runtime-command-insert")).thenReturn("INSERT_COMMAND");
        when(sql.required("runtime-command-find")).thenReturn("FIND_COMMAND");
        when(jdbc.update(eq("INSERT_COMMAND"), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("duplicate"));
        when(jdbc.queryForList("FIND_COMMAND", "IDEM-1"))
                .thenReturn(List.of(Map.of("COMMAND_ID", "CMD-1")));

        JdbcRuntimeCommandRepository repository = new JdbcRuntimeCommandRepository(jdbc, provider);
        Map<String, Object> row = repository.create(command());

        assertEquals("CMD-1", row.get("COMMAND_ID"));
        verify(jdbc).queryForList("FIND_COMMAND", "IDEM-1");
    }

    @Test
    void primaryKeyCollisionWithAnotherIdempotencyKeyIsStableConflict() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(sql);
        when(sql.required(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbc.update(eq("runtime-command-insert"), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("duplicate command_id"));
        when(jdbc.queryForList("runtime-command-find", "IDEM-1")).thenReturn(List.of());

        JdbcRuntimeCommandRepository repository = new JdbcRuntimeCommandRepository(jdbc, provider);
        assertThrows(RuntimeCommandIdempotencyConflictException.class,
                () -> repository.create(command()));
    }

    private static RuntimeCommand command() {
        Instant now = Instant.now();
        return new RuntimeCommand(
                "CMD-1", "IDEM-1", "RESTART", "INSTANCE", List.of("runtime-1"),
                "[\"runtime-1\"]", "hash", 7L, "requester", "reason", now,
                "POLICY-1", "APR-1", "approver", now.plusSeconds(300),
                CommandState.APPROVED, 0, Map.of(), null, null, null, null,
                "OBAT-AA-00000000000000000000000000", null);
    }
}
