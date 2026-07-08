package cpf.pfw.common.filetransfer;

/**
 * 파일 중복 송수신 방지 port입니다.
 */
public interface CpfDuplicatePreventionPort {

    boolean alreadyProcessed(String endpointCode, String fileKey, String checksum);

    void remember(CpfFileTransferRequest request, CpfFileTransferResult result);
}
