package com.cpf.core.common.filetransfer;

/**
 * 실제 파일 전송 adapter가 구현해야 하는 port입니다.
 */
public interface CpfFileTransferPort {

    CpfFileTransferResult execute(CpfFileTransferEndpoint endpoint, CpfFileTransferRequest request);
}
