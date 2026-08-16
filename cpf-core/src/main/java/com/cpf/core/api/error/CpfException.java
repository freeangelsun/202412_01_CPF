package com.cpf.core.api.error;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * CPF 표준 RuntimeException입니다.
 *
 * <p>Core는 기술별 transport status나 DB message row를 소유하지 않습니다. 예외는 호출자가 지정한
 * {@code errorReference}와 안전한 Framework fallback 의미만 운반하며, 실제 업무/기관 코드와 locale
 * message 해석은 Common Error Catalog가 담당합니다.</p>
 */
public class CpfException extends RuntimeException {
    private final String errorReference;
    private final CpfErrorDefinition fallbackError;
    private final String detail;
    private final Map<String, Object> arguments;

    /** CpfException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfException(CpfErrorDefinition fallbackError, String detail) {
        this(fallbackError == null ? null : fallbackError.statusCode(), fallbackError, detail, null, null);
    }

    public CpfException(CpfErrorDefinition fallbackError, String detail, Throwable cause) {
        this(fallbackError == null ? null : fallbackError.statusCode(), fallbackError, detail, cause, null);
    }

    /** CpfException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfException(CpfErrorDefinition fallbackError, String detail, Map<String, Object> arguments) {
        this(fallbackError == null ? null : fallbackError.statusCode(), fallbackError, detail, null, arguments);
    }

    public CpfException(CpfErrorDefinition fallbackError, String detail, Throwable cause,
                        Map<String, Object> arguments) {
        this(fallbackError == null ? null : fallbackError.statusCode(), fallbackError, detail, cause, arguments);
    }

    /** CpfException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfException(String errorReference, CpfErrorDefinition fallbackError, String detail,
                        Map<String, Object> arguments) {
        this(errorReference, fallbackError, detail, null, arguments);
    }

    public CpfException(String errorReference, CpfErrorDefinition fallbackError, String detail,
                        Throwable cause, Map<String, Object> arguments) {
        super(message(fallbackError, detail), cause);
        this.fallbackError = Objects.requireNonNull(fallbackError, "fallbackError");
        this.errorReference = normalizeReference(errorReference, fallbackError.statusCode());
        this.detail = detail;
        this.arguments = arguments == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(arguments));
    }

    /** DB Error Catalog lookup에 사용하는 업무/기관 오류 참조입니다. */
    public String errorReference() { return errorReference; }

    /** DB 조회 실패/누락 시에도 절대 약화되지 않는 Framework fallback 의미입니다. */
    public CpfErrorDefinition fallbackError() { return fallbackError; }
    /** 기존 Consumer의 기술중립 error accessor를 유지합니다. 의미는 안전한 Framework fallback입니다. */
    public CpfErrorDefinition error() { return fallbackError; }

    public String detail() { return detail; }
    public Map<String, Object> arguments() { return arguments; }

    // Existing framework consumers use bean-style accessors. Keep one canonical meaning only.
    public String getErrorReference() { return errorReference; }
    public CpfErrorDefinition getErrorCode() { return fallbackError; }
    public String getResponseCode() { return errorReference; }
    public String getDetail() { return detail; }
    public Map<String, Object> getMessageArguments() { return arguments; }

    private static String normalizeReference(String reference, String fallback) {
        if (reference == null || reference.isBlank()) return fallback;
        return reference.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static String message(CpfErrorDefinition fallbackError, String detail) {
        Objects.requireNonNull(fallbackError, "fallbackError");
        return detail == null || detail.isBlank() ? fallbackError.defaultInternalMessage() : detail;
    }
}
