package cpf.xyz.filetransfer;

import cpf.pfw.common.filetransfer.CpfFileTransferEngine;
import cpf.pfw.common.filetransfer.CpfFileTransferRequest;
import cpf.pfw.common.filetransfer.CpfFileTransferResult;
import cpf.pfw.common.filetransfer.CpfDuplicatePreventionPort;
import cpf.pfw.common.filetransfer.CpfFileTransferHistoryPort;
import cpf.pfw.common.filetransfer.DeterministicCpfRemoteFileTransferAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XyzSftpTransferPlanEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void sftpSampleExecutesActualPfwEngineAndReferenceAdapter() throws Exception {
        Path localRoot = Files.createDirectories(tempDir.resolve("local"));
        Path remoteRoot = Files.createDirectories(tempDir.resolve("remote"));
        Path source = localRoot.resolve("result.dat");
        Files.writeString(source, "XYZ SFTP 교육 샘플");
        CpfFileTransferHistoryPort history = mock(CpfFileTransferHistoryPort.class);
        CpfDuplicatePreventionPort duplicate = mock(CpfDuplicatePreventionPort.class);
        when(duplicate.alreadyProcessed(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.nullable(String.class)))
                .thenReturn(false);
        CpfFileTransferEngine engine = new CpfFileTransferEngine(
                new DeterministicCpfRemoteFileTransferAdapter(remoteRoot, localRoot),
                history,
                duplicate,
                null);
        XyzSftpTransferPlanEducationSample sample = new XyzSftpTransferPlanEducationSample(engine);

        CpfFileTransferResult result = sample.upload("T-1", source.toString());

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(remoteRoot.resolve("XYZ_BANK_A/SFTP/recv/result.dat")).exists();
        verify(history).record(org.mockito.ArgumentMatchers.any(CpfFileTransferRequest.class),
                org.mockito.ArgumentMatchers.any(CpfFileTransferResult.class));
    }
}
