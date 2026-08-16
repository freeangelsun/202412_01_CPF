package com.cpf.file.context;

import java.time.LocalDate;

/**
 * File capability가 자체 소유하는 처리 메타데이터입니다.
 * Core Context Component가 아니며 object key 원문 대신 hash/논리 이름만 담습니다.
 */
public record CpfFileProcessingContext(
        String fileJobId,
        String transferId,
        String logicalFileName,
        String protocol,
        String bucketAlias,
        String objectKeyHash,
        String provider,
        String direction,
        LocalDate businessDate,
        String checkpointId,
        String partId,
        String checksum,
        String unknownOutcomeId,
        int attempt,
        String recoveryId) {
    public CpfFileProcessingContext {
        if (attempt < 1) throw new IllegalArgumentException("attempt");
    }
}
