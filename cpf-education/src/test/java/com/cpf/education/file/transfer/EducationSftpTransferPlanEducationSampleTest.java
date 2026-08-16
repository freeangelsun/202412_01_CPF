package com.cpf.education.file.transfer;

import com.cpf.file.api.filetransfer.CpfFileEndpoint;
import com.cpf.file.api.filetransfer.CpfFileRequest;
import com.cpf.file.api.filetransfer.CpfFileResult;
import com.cpf.file.api.filetransfer.CpfFileTransferClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationSftpTransferPlanEducationSampleTest {
    @Test
    void publicFileTransferFacade로Sftp요청을전달한다() {
        CpfFileTransferClient client = mock(CpfFileTransferClient.class);
        ArgumentCaptor<CpfFileEndpoint> endpoint = ArgumentCaptor.forClass(CpfFileEndpoint.class);
        ArgumentCaptor<CpfFileRequest> request = ArgumentCaptor.forClass(CpfFileRequest.class);
        CpfFileResult completed = new CpfFileResult("SUCCESS", "EDU_BANK_A", "result.dat",
                "/recv/result.dat", "sha256:pending", 0L, Instant.parse("2026-08-12T00:00:00Z"), "ok");
        when(client.execute(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(completed);

        CpfFileResult result = new EducationSftpTransferPlanEducationSample(client).upload("T-1", "result.dat");

        verify(client).execute(endpoint.capture(), request.capture());
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(endpoint.getValue().endpointCode()).isEqualTo("EDU_BANK_A");
        assertThat(endpoint.getValue().protocol()).isEqualTo("SFTP");
        assertThat(request.getValue().transactionId()).isEqualTo("T-1");
        assertThat(request.getValue().remotePath()).isEqualTo("/recv/result.dat");
    }
}
