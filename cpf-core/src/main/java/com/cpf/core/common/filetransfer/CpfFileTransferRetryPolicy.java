package com.cpf.core.common.filetransfer;

import java.time.Duration;

/**
 * 파일 전송 재시도 정책 후보입니다.
 */
public record CpfFileTransferRetryPolicy(
        int maxAttempts,
        Duration backoff,
        boolean retryOnChecksumMismatch) {

    public CpfFileTransferRetryPolicy {
        maxAttempts = Math.max(1, maxAttempts);
        backoff = backoff == null ? Duration.ofSeconds(1) : backoff;
    }
}
