package com.cpf.common.fle.core;

/**
 * 프로젝트 공통 파일 송수신 요청 값입니다.
 *
 * <p>CPF-OWNERSHIP:CMN_PROJECT_HELPER</p>
 * <p>이 DTO는 업무 프로젝트의 파일명/경로/요청자 규칙을 표현하기 위한 값 객체입니다.
 * 실제 원격 전송 adapter는 CPF filetransfer port를 기준으로 분리합니다.</p>
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
