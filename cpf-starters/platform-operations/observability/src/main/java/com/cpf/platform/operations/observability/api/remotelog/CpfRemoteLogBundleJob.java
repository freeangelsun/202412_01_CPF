package com.cpf.platform.operations.observability.api.remotelog;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private static final Set<String> STATUSES =
            Set.of("SUBMITTED", "RUNNING", "COMPLETED", "FAILED", "EXPIRED");

    public CpfRemoteLogBundleJob {
        jobId = required(jobId, "jobId");
        ownerId = required(ownerId, "ownerId");
        status = required(status, "status").toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(status)) throw new IllegalArgumentException("unsupported bundle job status");
        if (requestedArtifactCount < 1 || includedArtifactCount < 0
                || includedArtifactCount > requestedArtifactCount) {
            throw new IllegalArgumentException("invalid bundle job artifact counts");
        }
        failedArtifactIds = failedArtifactIds == null ? List.of() : failedArtifactIds.stream()
                .map(id -> required(id, "failedArtifactId"))
                .distinct()
                .toList();
        if (failedArtifactIds.size() > requestedArtifactCount) {
            throw new IllegalArgumentException("too many failed artifact ids");
        }
        if (errorMessage != null) {
            errorMessage = errorMessage.trim();
            if (errorMessage.length() > 300 || errorMessage.indexOf('\r') >= 0 || errorMessage.indexOf('\n') >= 0) {
                throw new IllegalArgumentException("invalid bundle job error message");
            }
        }
        if (submittedAt == null || expiresAt == null || expiresAt.isBefore(submittedAt)) {
            throw new IllegalArgumentException("valid bundle job timestamps are required");
        }
        if (completedAt != null && completedAt.isBefore(submittedAt)) {
            throw new IllegalArgumentException("completion cannot predate submission");
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String normalized = value.trim();
        if (normalized.length() > 200 || normalized.indexOf('\r') >= 0 || normalized.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(name + " is invalid");
        }
        return normalized;
    }
}
