package com.cpf.backoffice.web.shared.error;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class ChannelExceptionHandler {
    @ExceptionHandler(UnsupportedBackofficeOperationException.class)
    ResponseEntity<Map<String, Object>> unsupported(UnsupportedBackofficeOperationException exception) {
        return problem(HttpStatus.NOT_FOUND, "MBW_OPERATION_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(HttpTimeoutException.class)
    ResponseEntity<Map<String, Object>> timeout(HttpTimeoutException exception) {
        return problem(HttpStatus.GATEWAY_TIMEOUT, "MBW_UPSTREAM_TIMEOUT", "Backoffice upstream request timed out");
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<Map<String, Object>> unavailable(IOException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "MBW_UPSTREAM_UNAVAILABLE", "Backoffice upstream is unavailable");
    }

    @ExceptionHandler(InterruptedException.class)
    ResponseEntity<Map<String, Object>> interrupted(InterruptedException exception) {
        Thread.currentThread().interrupt();
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "MBW_UPSTREAM_INTERRUPTED", "Backoffice upstream request was interrupted");
    }

    private static ResponseEntity<Map<String, Object>> problem(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "code", code,
                "message", message));
    }
}
