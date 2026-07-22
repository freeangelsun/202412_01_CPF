package com.cpf.common.fle.core;

/** 파일 교환 또는 원격 명령의 실행 여부와 결과를 운영 조회용으로 표현합니다. */
public record CmnFileExchangeHistoryRecord(
        String exchangeId,
        String actionType,
        String protocol,
        String direction,
        boolean executed,
        boolean success,
        String host,
        String sourcePath,
        String targetPath,
        String requestUser,
        String message,
        String createdAt) {
}

