package cpf.pfw.common.filetransfer;

/**
 * PFW file transfer capability가 인식하는 전송 프로토콜입니다.
 */
public enum CpfFileTransferProtocol {
    LOCAL,
    SFTP,
    FTP,
    FTPS,
    SCP,
    SSH
}
