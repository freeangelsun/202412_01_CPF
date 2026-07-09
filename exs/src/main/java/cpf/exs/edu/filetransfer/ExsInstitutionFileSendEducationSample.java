package cpf.exs.edu.filetransfer;

import cpf.pfw.common.filetransfer.CpfFileTransferRequest;
import cpf.pfw.common.filetransfer.PfwFileTransferEducationSample;

/**
 * 대외기관 파일 송신 adapter가 PFW file transfer port를 사용하는 샘플입니다.
 */
public class ExsInstitutionFileSendEducationSample {

    public CpfFileTransferRequest sendPlan(String transactionGlobalId, String institutionCode) {
        return new PfwFileTransferEducationSample().planSftpUpload(transactionGlobalId, "EXS_" + institutionCode);
    }
}
