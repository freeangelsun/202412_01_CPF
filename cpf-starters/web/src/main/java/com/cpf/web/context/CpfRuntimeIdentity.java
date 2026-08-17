package com.cpf.web.context;

import org.springframework.core.env.Environment;
import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;

import java.util.Locale;

/** Runtime-known HTTP identity. Developers do not populate these values per request. */
public record CpfRuntimeIdentity(String systemCode, String application, String instance) {
    public CpfRuntimeIdentity {
        systemCode = requiredSystemCode(systemCode);
        application = normalize(application, 128);
        instance = normalize(instance, 160);
    }

    public static CpfRuntimeIdentity from(Environment environment) {
        String system = first(environment,
                "cpf.system-code",
                "cpf.generated-domain.system-code",
                "cpf.framework.module-id");
        if (system == null) system = System.getenv("CPF_SYSTEM_CODE");
        String application = first(environment, "spring.application.name", "cpf.framework.application-id");
        String instance = first(environment, "cpf.runtime.instance-id");
        if (instance == null) instance = System.getenv("CPF_RUNTIME_INSTANCE_ID");
        if (instance == null) instance = CpfInstanceIdentity.current().instanceId();
        if (application == null) application = system;
        return new CpfRuntimeIdentity(system, application, instance);
    }

    private static String first(Environment environment, String... keys) {
        if (environment == null) return null;
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private static String requiredSystemCode(String value) {
        String normalized = normalize(value, 32);
        if (normalized == null) throw new IllegalStateException(
                "CPF runtime systemCode is required (cpf.system-code, cpf.generated-domain.system-code, or cpf.framework.module-id)");
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{1,31}")) {
            throw new IllegalStateException("Invalid CPF runtime systemCode: " + normalized);
        }
        return normalized;
    }

    private static String normalize(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > max || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalStateException("Invalid CPF runtime identity value");
        }
        return normalized;
    }
}
