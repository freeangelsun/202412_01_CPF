package cpf.pfw.common.filetransfer;

import cpf.pfw.common.security.CpfCredentialRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicCpfRemoteFileTransferAdapterTest {
    @TempDir
    Path tempDir;

    @Test
    void executesEveryDeclaredRemoteProtocolThroughSafeLocalHarness() throws Exception {
        Path localRoot = Files.createDirectories(tempDir.resolve("local"));
        Path remoteRoot = Files.createDirectories(tempDir.resolve("remote"));
        Path source = localRoot.resolve("result.dat");
        Files.writeString(source, "CPF remote protocol contract");
        DeterministicCpfRemoteFileTransferAdapter adapter =
                new DeterministicCpfRemoteFileTransferAdapter(remoteRoot, localRoot);

        for (CpfFileTransferProtocol protocol : new CpfFileTransferProtocol[]{
                CpfFileTransferProtocol.SFTP,
                CpfFileTransferProtocol.FTP,
                CpfFileTransferProtocol.FTPS,
                CpfFileTransferProtocol.SCP,
                CpfFileTransferProtocol.SSH}) {
            CpfFileTransferEndpoint endpoint = endpoint(protocol);
            CpfFileTransferRequest request = new CpfFileTransferRequest(
                    "20260714090000000XYZlocal010000001",
                    "SEG-" + protocol,
                    endpoint.endpointCode(),
                    "UPLOAD",
                    source.toString(),
                    "/recv/" + protocol.name().toLowerCase() + ".dat",
                    null,
                    Files.size(source),
                    Map.of("businessKey", protocol.name()));

            CpfFileTransferResult result = adapter.execute(endpoint, request);

            assertThat(result.status()).isEqualTo("SUCCESS");
            assertThat(remoteRoot.resolve(endpoint.endpointCode()).resolve(protocol.name())
                    .resolve("recv").resolve(protocol.name().toLowerCase() + ".dat")).exists();
        }
    }

    private CpfFileTransferEndpoint endpoint(CpfFileTransferProtocol protocol) {
        return new CpfFileTransferEndpoint(
                "TEST_" + protocol.name(),
                protocol.name(),
                "reference.invalid",
                22,
                "/remote",
                new CpfCredentialRef("test", protocol.name(), "latest", "테스트 credential"),
                Duration.ofSeconds(5),
                Map.of());
    }
}
