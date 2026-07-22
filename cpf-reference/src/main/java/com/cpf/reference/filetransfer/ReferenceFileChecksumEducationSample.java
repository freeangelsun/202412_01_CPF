package com.cpf.reference.filetransfer;

import com.cpf.core.common.archive.CpfArchiveChecksum;
import com.cpf.core.common.filetransfer.CpfFileTransferEndpoint;
import com.cpf.core.common.filetransfer.CpfFileTransferEngine;
import com.cpf.core.common.filetransfer.CpfFileTransferRequest;
import com.cpf.core.common.filetransfer.CpfFileTransferResult;

import java.nio.charset.StandardCharsets;

/**
 * 업무 파일 전송 전 checksum을 계산하는 샘플입니다.
 */
public class ReferenceFileChecksumEducationSample {
    private final CpfFileTransferEngine transferEngine;

    public ReferenceFileChecksumEducationSample(CpfFileTransferEngine transferEngine) {
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
