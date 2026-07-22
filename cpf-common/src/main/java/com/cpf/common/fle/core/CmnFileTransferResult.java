package com.cpf.common.fle.core;

import java.util.List;

/** 파일 전송 명령, 실행 여부, 원격·로컬 경로를 포함한 표준 결과입니다. */
public record CmnFileTransferResult(
        boolean success,
        boolean executed,
        CmnFileProtocol protocol,
        List<String> command,
        String message,
        String localPath,
        String remotePath) {
}

