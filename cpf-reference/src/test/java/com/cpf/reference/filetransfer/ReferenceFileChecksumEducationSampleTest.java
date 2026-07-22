package com.cpf.reference.filetransfer;

import com.cpf.core.common.filetransfer.CpfDuplicatePreventionPort;
import com.cpf.core.common.filetransfer.CpfFileTransferEndpoint;
import com.cpf.core.common.filetransfer.CpfFileTransferEngine;
import com.cpf.core.common.filetransfer.CpfFileTransferHistoryPort;
import com.cpf.core.common.filetransfer.CpfFileTransferRequest;
import com.cpf.core.common.filetransfer.CpfFileTransferResult;
import com.cpf.core.common.filetransfer.LocalCpfFileTransferAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceFileChecksumEducationSampleTest {

    @TempDir
    Path tempDir;

    @Test
    void checksumIsSha256Hex() {
        assertThat(sample(new MemoryHistory()).sha256("file-body")).hasSize(64);
    }

    @Test
    void localTransferUsesCpfEngineAndRecordsHistory() throws Exception {
        Path source = tempDir.resolve("source.dat");
        Files.writeString(source, "REF EDU FILE");
        MemoryHistory history = new MemoryHistory();
        ReferenceFileChecksumEducationSample sample = sample(history);

        CpfFileTransferResult result = sample.transfer(
                new CpfFileTransferEndpoint(
                        "REF_LOCAL",
                        "LOCAL",
                        "localhost",
                        0,
                        tempDir.resolve("remote").toString(),
                        null,
                        Duration.ofSeconds(5),
                        Map.of("overwriteYn", "N")),
                new CpfFileTransferRequest(
                        "TX-REF-1",
                        "SEG-REF-1",
                        "REF_LOCAL",
                        "UPLOAD",
                        source.toString(),
                        "target.dat",
                        null,
                        Files.size(source),
                        Map.of("businessKey", "REF-FILE-1")));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(history.results).hasSize(1);
        assertThat(Files.readString(tempDir.resolve("remote/target.dat"))).isEqualTo("REF EDU FILE");
    }

    private ReferenceFileChecksumEducationSample sample(MemoryHistory history) {
        return new ReferenceFileChecksumEducationSample(new CpfFileTransferEngine(
                new LocalCpfFileTransferAdapter(),
                history,
                history,
                null));
    }

    private static final class MemoryHistory implements CpfFileTransferHistoryPort, CpfDuplicatePreventionPort {
        private final List<CpfFileTransferResult> results = new ArrayList<>();

        @Override
        public boolean alreadyProcessed(String endpointCode, String fileKey, String checksum) {
            return false;
        }

        @Override
        public void remember(CpfFileTransferRequest request, CpfFileTransferResult result) {
            record(request, result);
        }

        @Override
        public void record(CpfFileTransferRequest request, CpfFileTransferResult result) {
            results.add(result);
        }

        @Override
        public List<CpfFileTransferResult> findHistory(String endpointCode, Instant from, Instant to, int limit) {
            return List.copyOf(results);
        }
    }
}
