package com.cpf.platform.operations.observability.api.remotelog;

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
        if (bundleId == null || bundleId.isBlank() || bundleId.length() > 200) {
            throw new IllegalArgumentException("bundleId is required");
        }
        if (fileName == null || fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")
                || fileName.contains("\r") || fileName.contains("\n")) {
            throw new IllegalArgumentException("safe bundle fileName is required");
        }
        if (path == null || path.isAbsolute() || path.normalize().startsWith("..")
                || path.normalize().getNameCount() == 0 || ".".equals(path.normalize().toString())) {
            throw new IllegalArgumentException("bundle path must be relative to the managed log root");
        }
        if (path.normalize().getFileName() == null
                || !fileName.trim().equals(path.normalize().getFileName().toString())) {
            throw new IllegalArgumentException("bundle fileName must match the final path segment");
        }
        if (includedCount < 0) throw new IllegalArgumentException("includedCount must be non-negative");
        if (expiresAt == null) throw new IllegalArgumentException("expiresAt is required");
        bundleId = bundleId.trim();
        fileName = fileName.trim();
        path = path.normalize();
        failedArtifactIds = failedArtifactIds == null ? List.of() : failedArtifactIds.stream()
                .map(id -> {
                    if (id == null || id.isBlank() || id.length() > 200) {
                        throw new IllegalArgumentException("failed artifact ids must be non-blank");
                    }
                    return id.trim();
                }).distinct().toList();
    }
}
