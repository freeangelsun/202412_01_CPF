package com.cpf.core.common.attachment;
/** 첨부 payload의 Virus/DLP scan과 quarantine을 수행하는 확장 Port입니다. */
public interface CpfAttachmentInspectionPort { Result inspect(String fileName,String contentType,byte[] content,boolean quarantineOnFailure); record Result(boolean accepted,boolean quarantined,String reason){} }
