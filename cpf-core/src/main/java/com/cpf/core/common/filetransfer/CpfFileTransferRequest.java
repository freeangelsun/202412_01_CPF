package com.cpf.core.common.filetransfer;

import java.util.Map;

/**
 * 파일 송수신 요청 표준 DTO입니다.
 */
public record CpfFileTransferRequest(
        String transactionGlobalId,
        String segmentId,
        String endpointCode,
        String operation,
        String localPath,
        String remotePath,
        String checksum,
        long fileSize,
        Map<String, String> attributes) {

    public CpfFileTransferRequest {
        if (endpointCode == null || endpointCode.isBlank()) {
            throw new IllegalArgumentException("endpointCode는 필수입니다.");
        }
        operation = operation == null || operation.isBlank() ? "UPLOAD" : operation;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
