package com.cpf.reference.filetransfer;

import com.cpf.core.api.filetransfer.CpfCredentialReference;
import com.cpf.core.api.filetransfer.CpfFileEndpoint;
import com.cpf.core.api.filetransfer.CpfFileRequest;
import com.cpf.core.api.filetransfer.CpfFileResult;
import com.cpf.core.api.filetransfer.CpfFileTransferClient;

import java.time.Duration;
import java.util.Map;

/**
 * REF 업무 파일을 CPF 파일전송 엔진으로 송신하는 교육 샘플입니다.
 */
public class ReferenceSftpTransferPlanEducationSample {
    private final CpfFileTransferClient transferClient;

    public ReferenceSftpTransferPlanEducationSample(CpfFileTransferClient transferClient) {
        this.transferClient = transferClient;
    }

    public CpfFileResult upload(String transactionId, String localPath) {
        CpfFileEndpoint endpoint = new CpfFileEndpoint(
                "REF_BANK_A",
                "SFTP",
                "sftp.example.internal",
                22,
                "/recv",
                new CpfCredentialReference("file-transfer", "ref-bank-a", "latest", "REF BANK A"),
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
