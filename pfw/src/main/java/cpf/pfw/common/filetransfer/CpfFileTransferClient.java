package cpf.pfw.common.filetransfer;

/**
 * 업무 모듈이 사용하는 파일 전송 facade 계약입니다.
 */
public interface CpfFileTransferClient {

    CpfFileTransferResult upload(CpfFileTransferRequest request);

    CpfFileTransferResult download(CpfFileTransferRequest request);
}
