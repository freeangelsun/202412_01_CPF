package com.cpf.education.file.transfer;
import com.cpf.file.archive.api.CpfArchiveChecksum;
import com.cpf.file.api.filetransfer.CpfFileEndpoint;
import com.cpf.file.api.filetransfer.CpfFileRequest;
import com.cpf.file.api.filetransfer.CpfFileResult;
import com.cpf.file.api.filetransfer.CpfFileTransferClient;

import java.nio.charset.StandardCharsets;

/**
 * 업무 파일 전송 전 checksum을 계산하는 샘플입니다.
 */
public class EducationFileChecksumEducationSample {
    private final CpfFileTransferClient transferClient;

    public EducationFileChecksumEducationSample(CpfFileTransferClient transferClient) {
        this.transferClient = transferClient;
    }

    /** sha256 작업을 CPF 표준 계약에 따라 수행한다. */
    public String sha256(String content) {
        return CpfArchiveChecksum.sha256(content.getBytes(StandardCharsets.UTF_8));
    }

    public CpfFileResult transfer(
            CpfFileEndpoint endpoint,
            CpfFileRequest request) {
        return transferClient.execute(endpoint, request);
    }
}
