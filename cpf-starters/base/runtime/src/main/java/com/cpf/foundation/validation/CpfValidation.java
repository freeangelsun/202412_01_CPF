package com.cpf.foundation.validation;

import com.cpf.core.api.error.CpfValidationException;
import java.util.Collection;

/** CPF 공통 기술 Validation Runtime입니다. 업무 규칙 Validator를 소유하지 않습니다. */
public final class CpfValidation {
    private CpfValidation() { }

    public static String requireText(String value, String name) {
        String field = normalizeName(name);
        if (value == null || value.isBlank()) throw new CpfValidationException(field + " is required");
        return value.trim();
    }

    public static <T> T requireValue(T value, String name) {
        if (value == null) throw new CpfValidationException(normalizeName(name) + " is required");
        return value;
    }

    public static <T extends Collection<?>> T requireNotEmpty(T value, String name) {
        if (value == null || value.isEmpty()) throw new CpfValidationException(normalizeName(name) + " must not be empty");
        return value;
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new CpfValidationException(requireText(message, "message"));
    }

    private static String normalizeName(String value) {
        return value == null || value.isBlank() ? "value" : value.trim();
    }
}
