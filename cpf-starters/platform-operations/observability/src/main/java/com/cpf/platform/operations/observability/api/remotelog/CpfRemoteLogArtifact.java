package com.cpf.platform.operations.observability.api.remotelog;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

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
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    public CpfRemoteLogArtifact {
        artifactId = required(artifactId, "artifactId", 200);
        environment = required(environment, "environment", 100);
        module = required(module, "module", 100);
        service = required(service, "service", 100);
        instance = required(instance, "instance", 200);
        logType = required(logType, "logType", 100);
        fileName = safeFileName(fileName);
        relativePath = safeRelativePath(relativePath);
        Path artifactPath = Path.of(relativePath);
        if (artifactPath.getFileName() == null || !fileName.equals(artifactPath.getFileName().toString())) {
            throw new IllegalArgumentException("fileName must match the final relativePath segment");
        }
        if (size < 0L) throw new IllegalArgumentException("size must be non-negative");
        if (modifiedAt == null) throw new IllegalArgumentException("modifiedAt is required");
        if (checksumSha256 != null && !checksumSha256.isBlank()) {
            checksumSha256 = checksumSha256.trim().toLowerCase(Locale.ROOT);
            if (!SHA256.matcher(checksumSha256).matches()) {
                throw new IllegalArgumentException("checksumSha256 must be a 64-character lowercase hex digest");
            }
        } else {
            checksumSha256 = null;
        }
        maskingPolicy = required(maskingPolicy, "maskingPolicy", 100);
        onlineStatus = required(onlineStatus, "onlineStatus", 50).toUpperCase(Locale.ROOT);
        if (downloadable && active) {
            throw new IllegalArgumentException("active log files cannot be downloaded directly");
        }
        if (downloadable && checksumSha256 == null) {
            throw new IllegalArgumentException("downloadable artifacts require a checksum");
        }
        if (retentionExpiresAt != null && retentionExpiresAt.isBefore(modifiedAt)) {
            throw new IllegalArgumentException("retention expiry cannot predate modification time");
        }
    }

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

    private static String required(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String normalized = value.trim();
        if (normalized.length() > maxLength || normalized.indexOf('\r') >= 0 || normalized.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(name + " is invalid");
        }
        return normalized;
    }

    private static String safeFileName(String value) {
        String normalized = required(value, "fileName", 255);
        Path path = Path.of(normalized).normalize();
        if (path.isAbsolute() || path.getNameCount() != 1 || normalized.contains("/") || normalized.contains("\\")
                || ".".equals(normalized) || "..".equals(normalized)) {
            throw new IllegalArgumentException("fileName must be a single safe name");
        }
        return normalized;
    }

    private static String safeRelativePath(String value) {
        String normalized = required(value, "relativePath", 1000).replace('\\', '/');
        Path path = Path.of(normalized).normalize();
        if (path.isAbsolute() || path.startsWith("..") || path.getNameCount() == 0
                || ".".equals(path.toString())) {
            throw new IllegalArgumentException("relativePath must remain under the configured log root");
        }
        return path.toString().replace('\\', '/');
    }
}
