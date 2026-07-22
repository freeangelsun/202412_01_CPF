package cpf.pfw.common.filetransfer;

/**
 * 파일 전송 endpoint 상태 확인 port입니다.
 */
public interface CpfFileTransferHealthPort {

    CpfFileTransferStatus check(CpfFileTransferEndpoint endpoint);
}
