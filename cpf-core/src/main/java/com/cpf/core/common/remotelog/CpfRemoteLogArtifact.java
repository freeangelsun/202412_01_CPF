package com.cpf.core.common.remotelog;

import java.time.Instant;

/** ADM에 노출할 수 있는 안전한 상대경로 기반 로그 파일 메타데이터입니다. */
public record CpfRemoteLogArtifact(
        String artifactId,
        String environment,
        String module,
        String service,
        String instance,
        String logType,
        String fileName,
        String relativePath,
        long size,
        Instant modifiedAt,
        boolean compressed,
        String checksumSha256,
        boolean active,
        String maskingPolicy,
        boolean downloadable,
        Instant retentionExpiresAt,
        String onlineStatus) {

    /** 초기 adapter 구현과 source 호환을 유지하는 축약 생성자입니다. */
    public CpfRemoteLogArtifact(
            String artifactId,
            String environment,
            String module,
            String service,
            String instance,
            String logType,
            String fileName,
            String relativePath,
            long size,
            Instant modifiedAt,
            boolean compressed,
            String checksumSha256,
            boolean active,
            String maskingPolicy,
            boolean downloadable) {
        this(artifactId, environment, module, service, instance, logType, fileName, relativePath,
                size, modifiedAt, compressed, checksumSha256, active, maskingPolicy, downloadable,
                null, "ONLINE");
    }
}
