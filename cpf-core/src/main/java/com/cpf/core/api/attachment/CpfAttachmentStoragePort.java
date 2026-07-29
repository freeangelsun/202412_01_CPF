package com.cpf.core.api.attachment;

/**
 * 업무 모듈과 물리 첨부파일 저장소를 분리하는 공개 Port입니다.
 *
 * <p>업무 모듈은 이 계약만 의존하고 로컬 파일, 오브젝트 스토리지 등의 Adapter 선택은
 * cpf-core 자동 설정에 맡깁니다.</p>
 */
public interface CpfAttachmentStoragePort {

    CpfStoredAttachment store(String groupId, String originalFileName, String contentType, byte[] content);

    CpfAttachmentContent read(String storageKey);

    void delete(String storageKey);
}
