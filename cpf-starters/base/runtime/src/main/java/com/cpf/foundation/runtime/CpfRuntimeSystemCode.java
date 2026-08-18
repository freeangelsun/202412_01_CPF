package com.cpf.foundation.runtime;

import java.util.Locale;
import org.springframework.core.env.Environment;

/**
 * CPF Runtime이 사용하는 Canonical systemCode를 한 곳에서 결정합니다.
 * Profile/application 이름은 systemCode 대체값이 아니며 업무 Domain/System 정본과 혼합하지 않습니다.
 */
public final class CpfRuntimeSystemCode {
    private CpfRuntimeSystemCode() {}

    public static String resolve(Environment environment) {
        String value = first(environment,
                "cpf.system-code",
                "cpf.generated-domain.system-code",
                "cpf.framework.module-id");
        if (!hasText(value)) value = System.getenv("CPF_SYSTEM_CODE");
        if (!hasText(value)) {
            throw new IllegalStateException(
                    "CPF runtime systemCode is required (cpf.system-code, cpf.generated-domain.system-code, cpf.framework.module-id, or CPF_SYSTEM_CODE)");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{1,31}")) {
            throw new IllegalStateException("Invalid CPF runtime systemCode: " + normalized);
        }
        return normalized;
    }

    private static String first(Environment environment, String... keys) {
        if (environment == null) return null;
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (hasText(value)) return value;
        }
        return null;
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
