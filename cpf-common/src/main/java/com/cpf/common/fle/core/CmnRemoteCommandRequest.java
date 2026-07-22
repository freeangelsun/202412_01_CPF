package cpf.cmn.fle.core;

/**
 * 프로젝트 공통 원격 명령 요청 값입니다.
 *
 * <p>CPF-OWNERSHIP:CMN_PROJECT_HELPER</p>
 * <p>CMN은 요청 형태와 교육 샘플을 제공하고, 실제 SSH 실행 engine은 PFW runtime/filetransfer
 * capability로 옮길 후보입니다.</p>
 *
 * @param host 원격 서버 호스트
 * @param port 원격 서버 포트
 * @param username 원격 접속 사용자
 * @param identityFile SSH 개인키 파일 경로
 * @param command 실행할 명령
 * @param requestUser 요청 사용자
 */
public record CmnRemoteCommandRequest(
        String host,
        Integer port,
        String username,
        String identityFile,
        String command,
        String requestUser) {
}
