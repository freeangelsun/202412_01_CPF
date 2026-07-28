package com.cpf.gateway.transport;

import java.nio.file.Path;

/** Gateway 대용량 요청의 메모리·임시파일·timeout 경계를 정의합니다. */
public record CpfGatewayTransferPolicy(
        long maxRequestBytes,
        int memoryThresholdBytes,
        int ioBufferBytes,
        long connectTimeoutMillis,
        long requestTimeoutMillis,
        Path tempDirectory) {

    public CpfGatewayTransferPolicy {
        if (maxRequestBytes < 1L) throw new IllegalArgumentException("maxRequestBytes는 1 이상이어야 합니다.");
        if (memoryThresholdBytes < 0 || memoryThresholdBytes > maxRequestBytes)
            throw new IllegalArgumentException("memoryThresholdBytes 범위가 올바르지 않습니다.");
        if (ioBufferBytes < 4_096 || ioBufferBytes > 1_048_576)
            throw new IllegalArgumentException("ioBufferBytes는 4KB~1MB 범위여야 합니다.");
        if (connectTimeoutMillis < 100L || requestTimeoutMillis < connectTimeoutMillis)
            throw new IllegalArgumentException("Gateway timeout 범위가 올바르지 않습니다.");
        if (tempDirectory == null) throw new IllegalArgumentException("tempDirectory가 필요합니다.");
        tempDirectory = tempDirectory.toAbsolutePath().normalize();
    }
}
