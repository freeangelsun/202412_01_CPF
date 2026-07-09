package cpf.pfw.common.filetransfer;

import java.util.Map;

/**
 * SFTP/FTP/SCP/SSH 실접속 전 파일 송수신 계획을 검증하는 교육 샘플입니다.
 */
public class PfwFileTransferEducationSample {

    /**
     * 외부 runtime 없이 전송 요청/결과 DTO와 checksum 정책을 먼저 고정합니다.
     */
    public CpfFileTransferRequest planSftpUpload(String transactionGlobalId, String endpointCode) {
        return new CpfFileTransferRequest(
                transactionGlobalId,
                "SEG-FILE-001",
                endpointCode,
                "UPLOAD",
                "work/out/result.dat",
                "/recv/result.dat",
                "sha256:pending",
                128L,
                Map.of("protocol", CpfFileTransferProtocol.SFTP.name(), "runtime", "external-runtime-required"));
    }
}
