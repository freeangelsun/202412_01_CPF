package com.cpf.admin.opr.dto;

import java.time.Instant;

/** ADM API의 안전한 표준 오류 응답입니다. 예외 원문과 민감정보를 노출하지 않습니다. */
public record AdmApiErrorResponse(String code, String message, Instant timestamp) {
    public AdmApiErrorResponse {
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }
}
