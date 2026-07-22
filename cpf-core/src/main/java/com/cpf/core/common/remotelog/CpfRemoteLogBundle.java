package com.cpf.core.common.remotelog;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/** 선택 로그 ZIP과 checksum manifest, 부분 실패 정보를 함께 반환합니다. */
public record CpfRemoteLogBundle(
        String bundleId,
        String fileName,
        Path path,
        int includedCount,
        List<String> failedArtifactIds,
        Instant expiresAt) {
    public CpfRemoteLogBundle {
        failedArtifactIds = failedArtifactIds == null ? List.of() : List.copyOf(failedArtifactIds);
    }
}
