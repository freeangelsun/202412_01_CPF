package com.cpf.core.common.filetransfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalCpfFileTransferAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadStreamsToTempAndRenamesWithChecksum() throws Exception {
        Path source = tempDir.resolve("source.dat");
        Files.writeString(source, "CPF LOCAL FILE TRANSFER");
        Path remoteBase = tempDir.resolve("remote");
        LocalCpfFileTransferAdapter adapter = new LocalCpfFileTransferAdapter();

        CpfFileTransferResult result = adapter.execute(
                endpoint(remoteBase, "N"),
                request(source, "inbound/target.dat"));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.checksum()).hasSize(64);
        assertThat(Files.readString(remoteBase.resolve("inbound/target.dat")))
                .isEqualTo("CPF LOCAL FILE TRANSFER");
        assertThat(Files.exists(remoteBase.resolve("inbound/target.dat.cpf.tmp"))).isFalse();
    }

    @Test
    void targetPathTraversalIsRejected() throws Exception {
        Path source = tempDir.resolve("source.dat");
        Files.writeString(source, "CPF");

        assertThatThrownBy(() -> new LocalCpfFileTransferAdapter().execute(
                endpoint(tempDir.resolve("remote"), "N"),
                request(source, "../outside.dat")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기준 경로");
    }

    @Test
    void downloadRejectsLocalPathOutsideConfiguredBase() throws Exception {
        Path remoteBase = tempDir.resolve("remote");
        Files.createDirectories(remoteBase);
        Files.writeString(remoteBase.resolve("source.dat"), "CPF");
        CpfFileTransferRequest download = new CpfFileTransferRequest(
                "TX-1",
                "SEG-1",
                "LOCAL_EDU",
                "DOWNLOAD",
                tempDir.resolve("../outside.dat").normalize().toString(),
                "source.dat",
                null,
                0,
                Map.of());

        assertThatThrownBy(() -> new LocalCpfFileTransferAdapter().execute(
                endpoint(remoteBase, "N"),
                download))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("localPath");
    }

    @Test
    void checksumFailureRemovesTemporaryFile() throws Exception {
        Path source = tempDir.resolve("source.dat");
        Files.writeString(source, "CPF");
        Path remoteBase = tempDir.resolve("remote");
        CpfFileTransferRequest request = new CpfFileTransferRequest(
                "TX-1",
                "SEG-1",
                "LOCAL_EDU",
                "UPLOAD",
                source.toString(),
                "target.dat",
                "invalid-checksum",
                Files.size(source),
                Map.of());

        CpfFileTransferResult result = new LocalCpfFileTransferAdapter().execute(
                endpoint(remoteBase, "N"),
                request);

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(remoteBase.resolve("target.dat.cpf.tmp")).doesNotExist();
        assertThat(remoteBase.resolve("target.dat")).doesNotExist();
    }

    @Test
    void engineRecordsSuccessAndPreventsDuplicate() throws Exception {
        Path source = tempDir.resolve("source.dat");
        Files.writeString(source, "CPF");
        MemoryHistory history = new MemoryHistory();
        CpfFileTransferEngine engine = new CpfFileTransferEngine(
                new LocalCpfFileTransferAdapter(),
                history,
                history,
                null);
        CpfFileTransferRequest request = request(source, "target.dat");

        CpfFileTransferResult first = engine.execute(endpoint(tempDir.resolve("remote"), "N"), request);
        history.duplicate = true;
        CpfFileTransferResult second = engine.execute(endpoint(tempDir.resolve("remote"), "N"), request);

        assertThat(first.status()).isEqualTo("SUCCESS");
        assertThat(second.status()).isEqualTo("DUPLICATE");
        assertThat(history.results).hasSize(1);
    }

    private CpfFileTransferEndpoint endpoint(Path remoteBase, String overwriteYn) {
        return new CpfFileTransferEndpoint(
                "LOCAL_EDU",
                "LOCAL",
                "localhost",
                0,
                remoteBase.toString(),
                null,
                Duration.ofSeconds(5),
                Map.of(
                        "overwriteYn", overwriteYn,
                        "localBasePath", tempDir.toString()));
    }

    private CpfFileTransferRequest request(Path source, String remotePath) {
        return new CpfFileTransferRequest(
                "TX-1",
                "SEG-1",
                "LOCAL_EDU",
                "UPLOAD",
                source.toString(),
                remotePath,
                null,
                0,
                Map.of("businessKey", "FILE-1"));
    }

    private static final class MemoryHistory implements CpfFileTransferHistoryPort, CpfDuplicatePreventionPort {
        private final List<CpfFileTransferResult> results = new ArrayList<>();
        private boolean duplicate;

        @Override
        public boolean alreadyProcessed(String endpointCode, String fileKey, String checksum) {
            return duplicate;
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
        public List<CpfFileTransferResult> findHistory(
                String endpointCode,
                java.time.Instant from,
                java.time.Instant to,
                int limit) {
            return List.copyOf(results);
        }
    }
}
