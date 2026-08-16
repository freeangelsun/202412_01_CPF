package com.cpf.web.error;

import com.cpf.core.api.error.CpfErrorDefinition;
import org.springframework.http.HttpStatus;

/** Core/Common 오류 의미를 HTTP 상태로 변환하는 Web Capability Owner adapter입니다. */
public final class CpfHttpErrorMapper {
    private CpfHttpErrorMapper() { }

    public static HttpStatus status(CpfErrorDefinition error) {
        return status(error == null ? CpfErrorDefinition.Category.INTERNAL : error.category());
    }

    public static HttpStatus status(CpfErrorDefinition.Category category) {
        CpfErrorDefinition.Category safe = category == null ? CpfErrorDefinition.Category.INTERNAL : category;
        return switch (safe) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case AUTHENTICATION -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS;
            case BUSINESS -> HttpStatus.UNPROCESSABLE_CONTENT;
            case EXTERNAL -> HttpStatus.BAD_GATEWAY;
            case INFRASTRUCTURE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
