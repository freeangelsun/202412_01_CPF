package com.cpf.file.api.filetransfer;

import java.nio.file.Path;
import java.util.List;

/** 로컬 파일 helper, 원격 전송 계획과 감사 이력을 제공하는 공개 Operations 계약입니다. */
public interface CpfFileExchangeOperations {
    Path writeText(String relativePath, String contents, String requestUser);
    String readText(String relativePath, String requestUser);
    List<String> list(String relativeDirectory, String requestUser);
    CpfFileResult transfer(
            String protocol,
            String direction,
            String host,
            int port,
            String credentialId,
            String localPath,
            String remotePath,
            String requestUser);
    CpfRemoteCommandPlan planRemoteCommand(
            String host,
            int port,
            String username,
            String command,
            String requestUser);
    List<CpfFileExchangeHistoryRecord> findRecentHistory();
}
