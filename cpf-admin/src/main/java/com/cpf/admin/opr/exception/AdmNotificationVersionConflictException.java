package com.cpf.admin.opr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ADM 알림 발송 상태 또는 Version이 운영자 화면의 Snapshot과 달라진 경우 발생합니다.
 *
 * <p>위험한 Retry/Cancel 조작은 최신 상태를 다시 조회한 뒤 명시적으로 재시도해야 하므로
 * HTTP 409로 응답합니다.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AdmNotificationVersionConflictException extends RuntimeException {
    public AdmNotificationVersionConflictException(String message) {
        super(message);
    }
}
