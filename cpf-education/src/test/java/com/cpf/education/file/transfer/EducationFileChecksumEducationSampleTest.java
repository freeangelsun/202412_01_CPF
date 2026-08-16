package com.cpf.education.file.transfer;

import com.cpf.file.api.filetransfer.CpfFileEndpoint;
import com.cpf.file.api.filetransfer.CpfFileRequest;
import com.cpf.file.api.filetransfer.CpfFileResult;
import com.cpf.file.api.filetransfer.CpfFileTransferClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationFileChecksumEducationSampleTest {
    @Test
    void checksumIsSha256Hex() {
        EducationFileChecksumEducationSample sample = new EducationFileChecksumEducationSample(mock(CpfFileTransferClient.class));
        assertThat(sample.sha256("file-body")).hasSize(64);
    }

    @Test
    void 공개FileTransferClient경계를사용한다() {
        CpfFileTransferClient client = mock(CpfFileTransferClient.class);
        CpfFileEndpoint endpoint = new CpfFileEndpoint("EDU_LOCAL", "LOCAL", "localhost", 0,
                "/tmp", null, Duration.ofSeconds(5), Map.of());
        CpfFileRequest request = new CpfFileRequest("TX-EDU-1", "SEG-EDU-1", "EDU_LOCAL",
                "UPLOAD", "source.dat", "target.dat", "abc", 3L, Map.of("businessKey", "EDU-FILE-1"));
        CpfFileResult expected = new CpfFileResult("SUCCESS", "EDU_LOCAL", "source.dat",
                "target.dat", "abc", 3L, Instant.parse("2026-08-12T00:00:00Z"), "ok");
        when(client.execute(endpoint, request)).thenReturn(expected);

        CpfFileResult result = new EducationFileChecksumEducationSample(client).transfer(endpoint, request);

        verify(client).execute(endpoint, request);
        assertThat(result).isEqualTo(expected);
    }
}
