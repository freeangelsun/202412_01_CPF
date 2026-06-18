package cpf.cmn.fle.core;

/**
 * 파일 송수신 요청 값입니다.
 *
 * @param protocol LOCAL, FTP, SFTP, SCP 같은 전송 프로토콜
 * @param direction 업로드 또는 다운로드 방향
 * @param host 원격 서버 호스트
 * @param port 원격 서버 포트
 * @param username 원격 접속 사용자
 * @param identityFile SSH 개인키 파일 경로
 * @param localPath 로컬 파일 경로
 * @param remotePath 원격 파일 경로
 * @param requestUser 요청 사용자
 */
public record CmnFileTransferRequest(
        CmnFileProtocol protocol,
        CmnFileTransferDirection direction,
        String host,
        Integer port,
        String username,
        String identityFile,
        String localPath,
        String remotePath,
        String requestUser) {
}
