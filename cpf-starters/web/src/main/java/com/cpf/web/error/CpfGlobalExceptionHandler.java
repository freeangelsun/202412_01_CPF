package com.cpf.web.error;

import com.cpf.common.message.api.CpfErrorCatalogResolver;
import com.cpf.common.message.api.CpfResolvedError;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** CPF Web Capability의 표준 Exception → HTTP 경계 매핑입니다. */
@RestControllerAdvice
public final class CpfGlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CpfGlobalExceptionHandler.class);
    private final CpfErrorCatalogResolver catalogResolver;

    public CpfGlobalExceptionHandler(CpfErrorCatalogResolver catalogResolver) {
        this.catalogResolver = catalogResolver;
    }

    @ExceptionHandler(CpfException.class)
    ResponseEntity<CpfHttpErrorResponse> handleCpf(CpfException error) {
        var fallback = error.getErrorCode() == null ? CpfErrorCode.INTERNAL_SERVER_ERROR : error.getErrorCode();
        CpfResolvedError resolved = catalogResolver.resolve(
                error.getErrorReference(),
                fallback,
                LocaleContextHolder.getLocale(),
                error.getMessageArguments());
        boundarySignal(error.getClass().getSimpleName(), resolved.responseCode());
        return ResponseEntity.status(CpfHttpErrorMapper.status(resolved.category()))
                .body(new CpfHttpErrorResponse(
                        resolved.responseCode(),
                        resolved.externalMessage(),
                        CpfContexts.currentTransactionId(),
                        CpfContexts.currentExecutionId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<CpfHttpErrorResponse> handleValidation(MethodArgumentNotValidException error) {
        var fallback = CpfErrorCode.VALIDATION_FAILED;
        CpfResolvedError resolved = catalogResolver.resolve(
                fallback.statusCode(), fallback, LocaleContextHolder.getLocale(), Map.of());
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fieldError : error.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fieldError.getField(), safeFieldMessage(fieldError));
        }
        return ResponseEntity.status(CpfHttpErrorMapper.status(resolved.category()))
                .body(new CpfHttpErrorResponse(
                        resolved.responseCode(),
                        resolved.externalMessage(),
                        CpfContexts.currentTransactionId(),
                        CpfContexts.currentExecutionId(),
                        fields));
    }

    @ExceptionHandler(Throwable.class)
    ResponseEntity<CpfHttpErrorResponse> handleUnknown(Throwable error) {
        var fallback = CpfErrorCode.INTERNAL_SERVER_ERROR;
        CpfResolvedError resolved = catalogResolver.resolve(
                fallback.statusCode(), fallback, LocaleContextHolder.getLocale(), Map.of());
        // Raw exception message/SQL/secret는 boundary log에 기록하지 않습니다.
        boundarySignal(error.getClass().getSimpleName(), resolved.responseCode());
        return ResponseEntity.status(CpfHttpErrorMapper.status(resolved.category()))
                .body(new CpfHttpErrorResponse(
                        resolved.responseCode(),
                        resolved.externalMessage(),
                        CpfContexts.currentTransactionId(),
                        CpfContexts.currentExecutionId()));
    }

    private String safeFieldMessage(FieldError error) {
        String message = error.getDefaultMessage();
        if (message == null || message.isBlank()) return "invalid";
        if (message.length() > 160) message = message.substring(0, 160);
        return message.replace('\r', ' ').replace('\n', ' ').replace("<", "&lt;").replace(">", "&gt;");
    }

    private void boundarySignal(String type, String code) {
        log.warn("CPF_WEB_ERROR type={} code={} transactionId={} executionId={}",
                safe(type), safe(code), safe(CpfContexts.currentTransactionId()), safe(CpfContexts.currentExecutionId()));
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) return "-";
        return value.replaceAll("[^A-Za-z0-9_.:-]", "_");
    }
}
