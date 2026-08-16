package com.cpf.foundation.id;

import com.cpf.core.api.error.CpfValidationException;
import java.util.Locale;
import java.util.UUID;

/** 기술중립 식별자 생성/정규화 Utility입니다. */
public final class CpfIds {
    private CpfIds() {
    }

    public static String uuid32() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String temporaryId(String prefix) {
        return normalizeSystemCode(prefix) + "-" + uuid32();
    }

    public static String normalizeSystemCode(String value) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException("systemCode 값은 필수입니다.");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 32) {
            throw new CpfValidationException("systemCode 길이는 32 이하여야 합니다.");
        }
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]*")) {
            throw new CpfValidationException("systemCode 형식이 올바르지 않습니다.");
        }
        return normalized;
    }
}
