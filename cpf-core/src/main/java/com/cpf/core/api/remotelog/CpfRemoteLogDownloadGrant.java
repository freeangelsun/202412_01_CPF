package com.cpf.core.api.remotelog;

import java.time.Instant;

/** 비동기 로그 묶음을 한 번 다운로드할 수 있는 단기 token 발급 결과입니다. */
public record CpfRemoteLogDownloadGrant(
        String jobId,
        String token,
        Instant expiresAt) {
    public CpfRemoteLogDownloadGrant {
        if (jobId == null || jobId.isBlank() || jobId.length() > 200) {
            throw new IllegalArgumentException("jobId is required");
        }
        if (token == null || token.isBlank() || token.length() < 32 || token.length() > 2048
                || token.contains("\r") || token.contains("\n")) {
            throw new IllegalArgumentException("bounded opaque download token is required");
        }
        if (expiresAt == null) throw new IllegalArgumentException("expiresAt is required");
        jobId = jobId.trim();
        token = token.trim();
    }

    @Override
    public String toString() {
        return "CpfRemoteLogDownloadGrant[jobId=" + jobId
                + ", token=[REDACTED], expiresAt=" + expiresAt + "]";
    }
}
