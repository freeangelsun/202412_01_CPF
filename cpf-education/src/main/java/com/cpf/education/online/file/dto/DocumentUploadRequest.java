package com.cpf.education.online.file.dto;
/** DocumentUploadRequest는 CPF File/Object Storage Public API로 업로드·다운로드 책임을 분리하는 File Golden Path입니다. */
public record DocumentUploadRequest(String tenantId,String bucket,String objectKey,String content) { }
