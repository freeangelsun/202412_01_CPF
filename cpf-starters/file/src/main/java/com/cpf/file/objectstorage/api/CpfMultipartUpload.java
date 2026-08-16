package com.cpf.file.objectstorage.api;

/** CpfMultipartUpload 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfMultipartUpload(String tenantId, String bucket, String objectKey, String uploadId) {
    public CpfMultipartUpload {
        if (tenantId == null || tenantId.isBlank()) throw new IllegalArgumentException("tenantId");
        if (objectKey == null || objectKey.isBlank()) throw new IllegalArgumentException("objectKey");
        if (uploadId == null || uploadId.isBlank()) throw new IllegalArgumentException("uploadId");
    }
}
