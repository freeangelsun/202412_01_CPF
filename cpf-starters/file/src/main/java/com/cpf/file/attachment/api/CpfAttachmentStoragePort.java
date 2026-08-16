package com.cpf.file.attachment.api;
import java.io.InputStream;
/** 업무 모듈과 물리 저장소를 분리하는 streaming-only Public Port입니다. */
public interface CpfAttachmentStoragePort{
 CpfStoredAttachment store(String groupId,String originalFileName,String contentType,InputStream content,long declaredSize);
 CpfAttachmentStream open(String storageKey);
 void delete(String storageKey);
}
