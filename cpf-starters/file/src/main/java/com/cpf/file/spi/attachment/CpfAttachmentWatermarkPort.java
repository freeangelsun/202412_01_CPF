package com.cpf.file.spi.attachment;
/** 다운로드 시 운영자/승인/만료 정보를 반영한 watermark를 실제 content에 적용합니다. */
public interface CpfAttachmentWatermarkPort { byte[] apply(String storageKey,byte[] content,CpfAttachmentDownloadContext.Context context); }
