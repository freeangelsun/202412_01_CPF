package com.cpf.reference.batch.file.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.cpf.batch.spi.FileProcessHandler;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReferenceCsvFileProcessHandlerTest {
    @TempDir java.nio.file.Path temp;

    @Test
    void processesClaimedCsvAndReturnsDeterministicResult() throws Exception {
        java.nio.file.Path file = temp.resolve("sample.csv");
        Files.writeString(file, "id,name\n1,A\n2,B\n");
        FileProcessHandler.FileProcessResult result = new ReferenceCsvFileProcessHandler().process(
                new FileProcessHandler.FileProcessCommand(
                        1L, 3L, "checksum", "tx", "seg", 7L, file,
                        Files.size(file), "input-sha", Map.of()));
        assertEquals(FileProcessHandler.Status.COMPLETED, result.status());
        assertEquals("rows=2", result.message());
        assertFalse(result.outputHash().isBlank());
    }
}
