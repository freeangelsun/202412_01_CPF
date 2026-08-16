package com.cpf.education.file.transfer;
import com.cpf.file.api.filetransfer.CpfCredentialReference;
import com.cpf.file.api.filetransfer.CpfFileEndpoint;
import com.cpf.file.api.filetransfer.CpfFileRequest;
import com.cpf.file.api.filetransfer.CpfFileResult;
import com.cpf.file.api.filetransfer.CpfFileTransferClient;

import java.time.Duration;
import java.util.Map;

/**
 * EDU 업무 파일을 CPF 파일전송 엔진으로 송신하는 교육 샘플입니다.
 */
public class EducationSftpTransferPlanEducationSample {
    private final CpfFileTransferClient transferClient;

    public EducationSftpTransferPlanEducationSample(CpfFileTransferClient transferClient) {
        this.transferClient = transferClient;
    }

    /** upload 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfFileResult upload(String transactionId, String localPath) {
        CpfFileEndpoint endpoint = new CpfFileEndpoint(
                "EDU_BANK_A",
                "SFTP",
                "sftp.example.internal",
                22,
                "/recv",
                new CpfCredentialReference("file-transfer", "ref-bank-a", "latest", "EDU BANK A"),
                Duration.ofSeconds(30),
                Map.of("environment", "education"));
        CpfFileRequest request = new CpfFileRequest(
                transactionId,
                null,
                endpoint.endpointCode(),
                "UPLOAD",
                localPath,
                "/recv/result.dat",
                "sha256:pending",
                0L,
                Map.of("businessKey", transactionId + "|result.dat"));
        return transferClient.execute(endpoint, request);
    }
}
