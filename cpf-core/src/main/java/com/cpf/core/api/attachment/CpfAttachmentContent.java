package com.cpf.core.api.attachment;

/**
 * 저장소에서 읽은 첨부파일 본문과 SHA-256 무결성 정보입니다.
 *
 * <p>바이트 배열은 생성 시와 조회 시 모두 복사하여 호출자가 공유 상태를 변경할 수 없게 합니다.</p>
 */
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
