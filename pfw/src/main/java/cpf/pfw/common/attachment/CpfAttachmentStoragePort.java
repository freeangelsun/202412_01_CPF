package cpf.pfw.common.attachment;

/** 업무 모듈이 저장소 구현과 분리된 상태로 첨부파일을 저장하고 읽기 위한 PFW port입니다. */
public interface CpfAttachmentStoragePort {

    CpfStoredAttachment store(String groupId, String originalFileName, String contentType, byte[] content);

    CpfAttachmentContent read(String storageKey);

    void delete(String storageKey);
}
