package com.cpf.core.common.filetransfer;

import java.time.Instant;

/**
 * 파일 송수신 처리 결과입니다.
 */
public record CpfFileTransferResult(
        String status,
        String endpointCode,
        String localPath,
        String remotePath,
        String checksum,
        long fileSize,
        Instant completedAt,
        String detail) {

    public CpfFileTransferResult {
        status = status == null || status.isBlank() ? "UNKNOWN" : status;
        completedAt = completedAt == null ? Instant.now() : completedAt;
    }

    public static CpfFileTransferResult success(CpfFileTransferRequest request, String checksum, long fileSize) {
        return new CpfFileTransferResult(
                "SUCCESS",
                request.endpointCode(),
                request.localPath(),
                request.remotePath(),
                checksum,
                fileSize,
                Instant.now(),
                null);
    }

    public static CpfFileTransferResult failed(CpfFileTransferRequest request, String detail) {
        return new CpfFileTransferResult(
                "FAILED",
                request.endpointCode(),
                request.localPath(),
                request.remotePath(),
                request.checksum(),
                request.fileSize(),
                Instant.now(),
                detail);
    }
}
