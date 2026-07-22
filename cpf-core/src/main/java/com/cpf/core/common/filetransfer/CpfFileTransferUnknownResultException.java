package com.cpf.core.common.filetransfer;

/**
 * 파일 전송 결과를 성공 또는 실패로 확정할 수 없는 경우 사용합니다.
 */
public class CpfFileTransferUnknownResultException extends RuntimeException {
    public CpfFileTransferUnknownResultException(String message) {
        super(message);
    }

    public CpfFileTransferUnknownResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
