package com.cpf.file.api.filetransfer;

import java.util.Map;

/**
 * 업무 개발자가 파일 송수신에 전달하는 Public 요청입니다.
 *
 * <p>transactionId/segmentId/operationId 같은 CPF 실행 Context 값은 업무 코드가 입력하지 않습니다.
 * Public Client Adapter가 {@code CpfContexts}의 현재 실행 Context를 capture하여 내부 Runtime envelope에 연결합니다.</p>
 */
public record CpfFileRequest(
        String endpointCode,
        String operation,
        String localPath,
        String remotePath,
        String checksum,
        long fileSize,
        Map<String,String> attributes) {
    public CpfFileRequest {
        if (endpointCode == null || endpointCode.isBlank()) throw new IllegalArgumentException("endpointCode is required");
        operation = operation == null || operation.isBlank() ? "UPLOAD" : operation.trim().toUpperCase(java.util.Locale.ROOT);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
