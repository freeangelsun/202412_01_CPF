package com.cpf.core.common.attachment;

/** 저장소에서 읽은 첨부파일 본문과 무결성 정보입니다. */
public record CpfAttachmentContent(
        byte[] bytes,
        String checksumSha256) {

    public CpfAttachmentContent {
        bytes = bytes == null ? new byte[0] : bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }
}
