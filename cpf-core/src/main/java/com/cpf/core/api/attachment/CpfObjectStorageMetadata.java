package com.cpf.core.api.attachment;
import java.time.Instant;
import java.util.Map;
public record CpfObjectStorageMetadata(String tenantId, String bucket, String objectKey, long size, String contentType,
        String eTag, String checksumSha256, String versionId, Instant lastModified, Map<String,String> metadata) {
    public CpfObjectStorageMetadata { metadata = metadata == null ? Map.of() : Map.copyOf(metadata); }
}
