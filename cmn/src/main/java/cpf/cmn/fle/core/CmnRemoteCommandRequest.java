package cpf.cmn.fle.core;

/**
 * 원격 명령 실행 요청 값입니다.
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
