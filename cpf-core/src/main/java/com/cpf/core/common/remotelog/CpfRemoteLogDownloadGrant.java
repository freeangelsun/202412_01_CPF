package com.cpf.core.common.remotelog;

import java.time.Instant;

/** 비동기 로그 묶음을 한 번 다운로드할 수 있는 단기 token 발급 결과입니다. */
public record CpfRemoteLogDownloadGrant(
        String jobId,
        String token,
        Instant expiresAt) {
}
