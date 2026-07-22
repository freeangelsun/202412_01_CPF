package com.cpf.reference.filetransfer;

import com.cpf.core.common.filetransfer.CpfFileTransferEndpoint;
import com.cpf.core.common.filetransfer.CpfFileTransferEngine;
import com.cpf.core.common.filetransfer.CpfFileTransferProtocol;
import com.cpf.core.common.filetransfer.CpfFileTransferRequest;
import com.cpf.core.common.filetransfer.CpfFileTransferResult;
import com.cpf.core.common.security.CpfCredentialRef;

import java.time.Duration;
import java.util.Map;

/**
 * REF 업무 파일을 CPF 파일전송 엔진으로 송신하는 교육 샘플입니다.
 */
public class ReferenceSftpTransferPlanEducationSample {
    private final CpfFileTransferEngine transferEngine;

    public ReferenceSftpTransferPlanEducationSample(CpfFileTransferEngine transferEngine) {
        this.transferEngine = transferEngine;
    }

    public CpfFileTransferResult upload(String transactionGlobalId, String localPath) {
        CpfFileTransferEndpoint endpoint = new CpfFileTransferEndpoint(
                "REF_BANK_A",
                CpfFileTransferProtocol.SFTP.name(),
                "sftp.example.internal",
                22,
                "/recv",
                new CpfCredentialRef("file-transfer", "ref-bank-a", "latest", "REF BANK A"),
                Duration.ofSeconds(30),
                Map.of("environment", "education"));
        CpfFileTransferRequest request = new CpfFileTransferRequest(
                transactionGlobalId,
                null,
                endpoint.endpointCode(),
                "UPLOAD",
                localPath,
                "/recv/result.dat",
                "sha256:pending",
                0L,
                Map.of("businessKey", transactionGlobalId + "|result.dat"));
        return transferEngine.execute(endpoint, request);
    }
}
