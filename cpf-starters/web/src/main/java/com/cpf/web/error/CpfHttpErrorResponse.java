package com.cpf.web.error;

import java.util.Map;

/** 외부 HTTP 오류 응답입니다. 내부 메시지/Stack/원인 문자열을 노출하지 않습니다. */
public record CpfHttpErrorResponse(
        String code,
        String message,
        String transactionId,
        String executionId,
        Map<String, String> fieldErrors) {

    public CpfHttpErrorResponse(String code, String message, String transactionId, String executionId) {
        this(code, message, transactionId, executionId, Map.of());
    }

    public CpfHttpErrorResponse {
        fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
    }
}
