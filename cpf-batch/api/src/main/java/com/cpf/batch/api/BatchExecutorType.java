package com.cpf.batch.api;

/** BAT가 제공하는 승인 실행 유형. 임의 Shell Command는 존재하지 않습니다. */
public enum BatchExecutorType {
    SPRING_BATCH,
    APPROVED_SHELL,
    FILE_WATCH,
    FILE_PROCESS,
    FILE_TRANSFER
}
