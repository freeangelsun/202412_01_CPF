package com.cpf.core.common.remotelog;

import java.time.Instant;
import java.util.List;

/** 비동기 원격 로그 묶음 작업 상태입니다. */
public record CpfRemoteLogBundleJob(
        String jobId,
        String ownerId,
        String status,
        int requestedArtifactCount,
        int includedArtifactCount,
        List<String> failedArtifactIds,
        String errorMessage,
        Instant submittedAt,
        Instant completedAt,
        Instant expiresAt) {

    public CpfRemoteLogBundleJob {
        failedArtifactIds = failedArtifactIds == null ? List.of() : List.copyOf(failedArtifactIds);
    }
}
