package cpf.pfw.common.filetransfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PfwFileTransferAdvancedEducationSampleTest {
    @TempDir
    Path tempDir;

    private final PfwFileTransferAdvancedEducationSample sample = new PfwFileTransferAdvancedEducationSample();

    @Test
    void localAdapterCopiesFileWithChecksumHistoryAndDuplicateKey() throws Exception {
        Path source = tempDir.resolve("source.dat");
        Path targetDir = tempDir.resolve("target");
        Files.writeString(source, "file-transfer-body", StandardCharsets.UTF_8);

        PfwFileTransferAdvancedEducationSample.TransferScenario scenario =
                sample.localCopy(source, targetDir, "20260709030000000BATlocal010000001");

        assertThat(Files.readString(targetDir.resolve("source.dat"))).isEqualTo("file-transfer-body");
        assertThat(scenario.result().status()).isEqualTo("SUCCESS");
        assertThat(scenario.result().checksum()).hasSize(64);
        assertThat(scenario.history().duplicateKey()).isEqualTo(scenario.duplicateKey());
        assertThat(scenario.duplicateKey()).contains("LOCAL-ARCHIVE", "COPY");
    }

    @Test
    void externalProtocolsRemainExplicitRuntimePlans() {
        assertThat(sample.protocolPlans())
                .extracting(PfwFileTransferAdvancedEducationSample.ProtocolPlan::protocol)
                .contains(CpfFileTransferProtocol.SFTP, CpfFileTransferProtocol.FTP, CpfFileTransferProtocol.SCP, CpfFileTransferProtocol.SSH);
        assertThat(sample.protocolPlans()).allSatisfy(plan -> assertThat(plan.externalRuntimeRequired()).isTrue());
    }

    @Test
    void unknownResultRequiresReconciliation() {
        CpfFileTransferRequest request = new CpfFileTransferRequest(
                "20260709030000000EXSlocal010000001",
                "SEG-FILE-001",
                "SFTP-BANK",
                "UPLOAD",
                "out.dat",
                "/recv/out.dat",
                "sha256:pending",
                10L,
                java.util.Map.of());

        CpfFileTransferResult result = sample.unknownResult(request);

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(result.detail()).contains("reconciliation");
    }
}
