package cpf.xyz.edu.filetransfer;

import cpf.pfw.common.archive.CpfArchiveChecksum;
import cpf.pfw.common.filetransfer.CpfFileTransferEndpoint;
import cpf.pfw.common.filetransfer.CpfFileTransferEngine;
import cpf.pfw.common.filetransfer.CpfFileTransferRequest;
import cpf.pfw.common.filetransfer.CpfFileTransferResult;

import java.nio.charset.StandardCharsets;

/**
 * 업무 파일 전송 전 checksum을 계산하는 샘플입니다.
 */
public class XyzFileChecksumEducationSample {
    private final CpfFileTransferEngine transferEngine;

    public XyzFileChecksumEducationSample(CpfFileTransferEngine transferEngine) {
        this.transferEngine = transferEngine;
    }

    public String sha256(String content) {
        return CpfArchiveChecksum.sha256(content.getBytes(StandardCharsets.UTF_8));
    }

    public CpfFileTransferResult transfer(
            CpfFileTransferEndpoint endpoint,
            CpfFileTransferRequest request) {
        return transferEngine.execute(endpoint, request);
    }
}
