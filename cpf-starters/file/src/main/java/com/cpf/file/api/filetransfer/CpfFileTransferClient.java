package com.cpf.file.api.filetransfer;

/** 파일 송수신 Runtime을 Generated Domain에서 사용하는 공개 facade 계약입니다. */
public interface CpfFileTransferClient { CpfFileResult execute(CpfFileEndpoint endpoint, CpfFileRequest request); }
