package com.cpf.common.fle.core;

import java.util.List;

/** 원격 명령의 실행 여부, 종료 코드, 출력과 진단 메시지를 포함한 결과입니다. */
public record CmnRemoteCommandResult(
        boolean success,
        boolean executed,
        int exitCode,
        List<String> command,
        String output,
        String message) {
}

