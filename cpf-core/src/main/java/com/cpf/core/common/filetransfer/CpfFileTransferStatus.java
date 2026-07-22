package com.cpf.core.common.filetransfer;

import java.time.Instant;
import java.util.Map;

/**
 * 파일 전송 endpoint 관제 DTO입니다.
 */
public record CpfFileTransferStatus(
        String endpointCode,
        String protocol,
        String status,
        Instant checkedAt,
        Map<String, String> metrics) {

    public CpfFileTransferStatus {
        checkedAt = checkedAt == null ? Instant.now() : checkedAt;
        metrics = metrics == null ? Map.of() : Map.copyOf(metrics);
    }
}
