package cpf.xyz.edu.filetransfer;

import cpf.pfw.common.filetransfer.CpfFileTransferEndpoint;
import cpf.pfw.common.filetransfer.CpfFileTransferEngine;
import cpf.pfw.common.filetransfer.CpfFileTransferProtocol;
import cpf.pfw.common.filetransfer.CpfFileTransferRequest;
import cpf.pfw.common.filetransfer.CpfFileTransferResult;
import cpf.pfw.common.security.CpfCredentialRef;

import java.time.Duration;
import java.util.Map;

/**
 * XYZ 업무 파일을 PFW 파일전송 엔진으로 송신하는 교육 샘플입니다.
 */
public class XyzSftpTransferPlanEducationSample {
    private final CpfFileTransferEngine transferEngine;

    public XyzSftpTransferPlanEducationSample(CpfFileTransferEngine transferEngine) {
        this.transferEngine = transferEngine;
    }

    public CpfFileTransferResult upload(String transactionGlobalId, String localPath) {
        CpfFileTransferEndpoint endpoint = new CpfFileTransferEndpoint(
                "XYZ_BANK_A",
                CpfFileTransferProtocol.SFTP.name(),
                "sftp.example.internal",
                22,
                "/recv",
                new CpfCredentialRef("file-transfer", "xyz-bank-a", "latest", "XYZ BANK A"),
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
