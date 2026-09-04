package com.cpf.web.error;

import com.cpf.core.api.error.CpfErrorCatalogResolver;
import com.cpf.core.api.error.CpfErrorDefinition;
import com.cpf.core.api.error.CpfResolvedErrorView;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
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

    public CpfGlobalExceptionHandler(ObjectProvider<CpfErrorCatalogResolver> catalogResolver) {
        this.catalogResolver = catalogResolver.getIfAvailable(CpfGlobalExceptionHandler::fallbackResolver);
    }

    @ExceptionHandler(CpfException.class)
    ResponseEntity<CpfHttpErrorResponse> handleCpf(CpfException error) {
        var fallback = error.getErrorCode() == null ? CpfErrorCode.INTERNAL_SERVER_ERROR : error.getErrorCode();
        CpfResolvedErrorView resolved = catalogResolver.resolve(
                error.getErrorReference(),
                fallback,
                LocaleContextHolder.getLocale(),
                error.getMessageArguments());
        boundarySignal(error.getClass().getSimpleName(), resolved.responseCode(), error);
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
        CpfResolvedErrorView resolved = catalogResolver.resolve(
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

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<CpfHttpErrorResponse> handleResponseStatus(ResponseStatusException error) {
        CpfErrorCode fallback = statusFallback(error.getStatusCode().value());
        CpfResolvedErrorView resolved = catalogResolver.resolve(
                error.getReason(), fallback, LocaleContextHolder.getLocale(), Map.of());
        boundarySignal(error.getClass().getSimpleName(), resolved.responseCode());
        return ResponseEntity.status(error.getStatusCode())
                .body(new CpfHttpErrorResponse(
                        resolved.responseCode(),
                        resolved.externalMessage(),
                        CpfContexts.currentTransactionId(),
                        CpfContexts.currentExecutionId()));
    }

    @ExceptionHandler(Throwable.class)
    ResponseEntity<CpfHttpErrorResponse> handleUnknown(Throwable error) {
        var fallback = CpfErrorCode.INTERNAL_SERVER_ERROR;
        CpfResolvedErrorView resolved = catalogResolver.resolve(
                fallback.statusCode(), fallback, LocaleContextHolder.getLocale(), Map.of());
        // Raw exception message/SQL/secret는 boundary log에 기록하지 않습니다.
        boundarySignal(error.getClass().getSimpleName(), resolved.responseCode());
        // 예상하지 못한 오류는 **원인 지점을 남겨야 고칠 수 있다.** 외부 응답에는 어떤 내부 정보도
        // 넣지 않지만, 서버 로그에는 stack 을 남긴다. 이것이 없으면 500 이 나도 어느 코드에서
        // 났는지 알 수 없어 Runtime 주기를 반복하며 추측해야 한다.
        // message 는 이미 boundarySignal 이 sanitize 한 type/code 만 쓰고, 여기서는 stack 만 넘긴다.
        log.error("CPF_WEB_UNEXPECTED_ERROR type={} transactionId={} executionId={}",
                safe(error.getClass().getName()),
                safe(CpfContexts.currentTransactionId()),
                safe(CpfContexts.currentExecutionId()),
                error);
        return ResponseEntity.status(CpfHttpErrorMapper.status(resolved.category()))
                .body(new CpfHttpErrorResponse(
                        resolved.responseCode(),
                        resolved.externalMessage(),
                        CpfContexts.currentTransactionId(),
                        CpfContexts.currentExecutionId()));
    }

    private static CpfErrorCatalogResolver fallbackResolver() {
        return (errorReference, fallback, locale, arguments) -> new CpfResolvedErrorView() {
            private final CpfErrorDefinition safe = fallback == null ? CpfErrorCode.INTERNAL_SERVER_ERROR : fallback;
            @Override public String responseCode() { return safe.statusCode(); }
            @Override public String messageCode() { return safe.messageCode(); }
            @Override public CpfErrorDefinition definition() { return safe; }
            @Override public String externalMessage() { return safe.defaultExternalMessage(); }
            @Override public String internalMessage() { return safe.defaultInternalMessage(); }
            @Override public java.util.Locale locale() { return locale == null ? java.util.Locale.KOREAN : locale; }
            @Override public boolean catalogHit() { return false; }
        };
    }

    private static CpfErrorCode statusFallback(int status) {
        return switch (status) {
            case 400 -> CpfErrorCode.INVALID_PARAMETER;
            case 401 -> CpfErrorCode.UNAUTHORIZED;
            case 403 -> CpfErrorCode.FORBIDDEN;
            case 404 -> CpfErrorCode.NOT_FOUND;
            case 409 -> CpfErrorCode.CONFLICT;
            case 429 -> CpfErrorCode.RATE_LIMITED;
            case 503 -> CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE;
            default -> status >= 400 && status < 500
                    ? CpfErrorCode.INVALID_PARAMETER
                    : CpfErrorCode.INTERNAL_SERVER_ERROR;
        };
    }

    private String safeFieldMessage(FieldError error) {
        String message = error.getDefaultMessage();
        if (message == null || message.isBlank()) return "invalid";
        if (message.length() > 160) message = message.substring(0, 160);
        return message.replace('\r', ' ').replace('\n', ' ').replace("<", "&lt;").replace(">", "&gt;");
    }

    private void boundarySignal(String type, String code) {
        boundarySignal(type, code, null);
    }

    // 4xx 는 외부 응답 message 가 Catalog 문구로 치환되므로, origin 이 없으면 어떤 코드가 왜
    // 거절했는지 서버 로그로도 알 수 없다. 실제로 ADM 400(ECPF010004)의 원인을 찾기 위해
    // Runtime 주기를 반복해야 했다. 값(=사용자 입력)이 아니라 **던진 위치**만 남긴다.
    private void boundarySignal(String type, String code, Throwable error) {
        log.warn("CPF_WEB_ERROR type={} code={} origin={} transactionId={} executionId={}",
                safe(type), safe(code), safe(originOf(error)),
                safe(CpfContexts.currentTransactionId()), safe(CpfContexts.currentExecutionId()));
    }

    private static String originOf(Throwable error) {
        if (error == null) return null;
        for (StackTraceElement frame : error.getStackTrace()) {
            if (frame.getClassName().startsWith("com.cpf.")) {
                return frame.getClassName() + "." + frame.getMethodName() + ":" + frame.getLineNumber();
            }
        }
        return null;
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) return "-";
        return value.replaceAll("[^A-Za-z0-9_.:-]", "_");
    }
}
