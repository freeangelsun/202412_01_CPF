package cpf.cmn.fle.core;

/**
 * CMN 프로젝트 공통 파일 규칙에서 사용하는 프로토콜 코드입니다.
 *
 * <p>CPF-OWNERSHIP:CMN_PROJECT_HELPER</p>
 * <p>실제 SFTP/FTP/SSH 전송 engine은 PFW filetransfer port가 소유하고,
 * CMN은 프로젝트별 파일 규칙과 교육용 값 객체를 제공합니다.</p>
 */
public enum CmnFileProtocol {
    LOCAL,
    FTP,
    SFTP,
    SCP
}
