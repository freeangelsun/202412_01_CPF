package com.cpf.core.api.error;

/** 응답 코드와 메시지 카탈로그를 완전히 해석한 공개 응답 메타데이터입니다. */
public record CpfResolvedResponse(
        int httpStatus,
        String responseCode,
        String messageCode,
        String externalMessage,
        String internalMessage,
        String errorCode,
        String errorMessage) {
}
