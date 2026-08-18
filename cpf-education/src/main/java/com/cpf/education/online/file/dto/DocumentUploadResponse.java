package com.cpf.education.online.file.dto;
import com.cpf.file.objectstorage.api.CpfObjectStorageMetadata;
/** DocumentUploadResponse는 CPF File/Object Storage Public API로 업로드·다운로드 책임을 분리하는 File Golden Path입니다. */
public record DocumentUploadResponse(CpfObjectStorageMetadata metadata,int downloadedBytes,String downloadedContent) { }
