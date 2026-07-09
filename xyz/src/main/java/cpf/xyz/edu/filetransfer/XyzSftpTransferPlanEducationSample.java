package cpf.xyz.edu.filetransfer;

import cpf.pfw.common.filetransfer.CpfFileTransferRequest;
import cpf.pfw.common.filetransfer.PfwFileTransferEducationSample;

/**
 * XYZ 업무 파일 송신을 PFW file transfer request로 계획하는 샘플입니다.
 */
public class XyzSftpTransferPlanEducationSample {

    public CpfFileTransferRequest plan(String transactionGlobalId) {
        return new PfwFileTransferEducationSample().planSftpUpload(transactionGlobalId, "XYZ_BANK_A");
    }
}
