package com.cpf.file.common.filetransfer;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.file.spi.filetransfer.*;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcCpfFileTransferRepositoryTest {

    @Test
    void alreadyProcessedChecksEndpointDuplicateKeyAndChecksum() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any(), any()))
                .thenReturn(1);
        JdbcCpfFileTransferRepository repository = repository(jdbcTemplate);

        assertThat(repository.alreadyProcessed("MBW", "file-key", "sha256:1")).isTrue();
    }

    @Test
    void recordUpdatesExistingTransferWhenDuplicateTransferIdExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new DuplicateKeyException("duplicate"));
        JdbcCpfFileTransferRepository repository = repository(jdbcTemplate);
        CpfFileTransferRequest request = request();

        repository.record(request, CpfFileTransferResult.success(request, "sha256:1", 100));

        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any());
    }

    @Test
    void findHistoryMapsResultRows() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), eq("MBW"), eq("MBW"), isNull(), isNull(), isNull(), isNull(), eq(10)))
                .thenReturn(List.of(Map.of(
                        "transferStatus", "SUCCESS",
                        "endpointCode", "MBW",
                        "localPath", "/tmp/a.dat",
                        "remotePath", "/remote/a.dat",
                        "checksum", "sha256:1",
                        "fileSize", 100L,
                        "completedAt", Timestamp.from(Instant.parse("2026-07-10T01:00:00Z")),
                        "resultDetail", "OK")));
        JdbcCpfFileTransferRepository repository = repository(jdbcTemplate);

        List<CpfFileTransferResult> history = repository.findHistory("MBW", null, null, 10);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).status()).isEqualTo("SUCCESS");
        assertThat(history.get(0).fileSize()).isEqualTo(100L);
    }

    private CpfFileTransferRequest request() {
        return new CpfFileTransferRequest(
                "202607100001",
                "SEG-1",
                "MBW",
                "UPLOAD",
                "/tmp/a.dat",
                "/remote/a.dat",
                "sha256:1",
                100L,
                Map.of("businessKey", "FILE-1"));
    }

    private JdbcCpfFileTransferRepository repository(JdbcTemplate jdbcTemplate) {
        CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);
        when(sql.required(anyString())).thenAnswer(invocation -> "/* " + invocation.getArgument(0) + " */ SELECT 1");
        return new JdbcCpfFileTransferRepository(jdbcTemplate, sql);
    }
}
