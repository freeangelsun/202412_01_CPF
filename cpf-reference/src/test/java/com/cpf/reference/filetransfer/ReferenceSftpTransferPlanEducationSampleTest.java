package com.cpf.reference.filetransfer;

import com.cpf.core.common.filetransfer.CpfFileTransferEngine;
import com.cpf.core.common.filetransfer.CpfFileTransferRequest;
import com.cpf.core.common.filetransfer.CpfFileTransferResult;
import com.cpf.core.common.filetransfer.CpfDuplicatePreventionPort;
import com.cpf.core.common.filetransfer.CpfFileTransferHistoryPort;
import com.cpf.core.common.filetransfer.DeterministicCpfRemoteFileTransferAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReferenceSftpTransferPlanEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void sftpSampleExecutesActualCpfEngineAndReferenceAdapter() throws Exception {
        Path localRoot = Files.createDirectories(tempDir.resolve("local"));
        Path remoteRoot = Files.createDirectories(tempDir.resolve("remote"));
        Path source = localRoot.resolve("result.dat");
        Files.writeString(source, "REF SFTP 교육 샘플");
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
        ReferenceSftpTransferPlanEducationSample sample = new ReferenceSftpTransferPlanEducationSample(engine);

        CpfFileTransferResult result = sample.upload("T-1", source.toString());

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(remoteRoot.resolve("REF_BANK_A/SFTP/recv/result.dat")).exists();
        verify(history).record(org.mockito.ArgumentMatchers.any(CpfFileTransferRequest.class),
                org.mockito.ArgumentMatchers.any(CpfFileTransferResult.class));
    }
}
