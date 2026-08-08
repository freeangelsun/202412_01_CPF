package com.cpf.core.api.attachment;
public record CpfMultipartUpload(String tenantId, String bucket, String objectKey, String uploadId) {}
