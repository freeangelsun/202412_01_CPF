package com.cpf.reference.filetransfer;

import com.cpf.core.api.archive.CpfArchiveChecksum;
import com.cpf.core.api.filetransfer.CpfFileEndpoint;
import com.cpf.core.api.filetransfer.CpfFileRequest;
import com.cpf.core.api.filetransfer.CpfFileResult;
import com.cpf.core.api.filetransfer.CpfFileTransferClient;

import java.nio.charset.StandardCharsets;

/**
 * 업무 파일 전송 전 checksum을 계산하는 샘플입니다.
 */
public class ReferenceFileChecksumEducationSample {
    private final CpfFileTransferClient transferClient;

    public ReferenceFileChecksumEducationSample(CpfFileTransferClient transferClient) {
        this.transferClient = transferClient;
    }

    public String sha256(String content) {
        return CpfArchiveChecksum.sha256(content.getBytes(StandardCharsets.UTF_8));
    }

    public CpfFileResult transfer(
            CpfFileEndpoint endpoint,
            CpfFileRequest request) {
        return transferClient.execute(endpoint, request);
    }
}
