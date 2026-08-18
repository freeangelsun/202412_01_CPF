package com.cpf.web.context;

import org.springframework.core.env.Environment;
import com.cpf.foundation.runtime.CpfRuntimeMetadata;

import java.util.Locale;

/**
 * Runtime-known HTTP identity. Business code never populates these values per request.
 *
 * <p>The runtime {@code systemCode} remains trusted runtime/registry metadata. For generated business
 * domains the exact same canonical value is also the CPF transaction {@code currentChannel}; no
 * System-to-Channel mapper or duplicate channel configuration exists.</p>
 */
public record CpfRuntimeIdentity(String systemCode, String currentChannel, String application, String instance) {
    public CpfRuntimeIdentity {
        systemCode = requiredSystemCode(systemCode, "systemCode");
        currentChannel = requiredChannel(currentChannel, "currentChannel");
        if (!systemCode.equals(currentChannel)) {
            throw new IllegalStateException("CPF runtime currentChannel must equal canonical systemCode");
        }
        application = normalize(application, 128);
        instance = normalize(instance, 160);
    }

    /** Generated-domain canonical relation: systemCode value itself is the runtime Channel identity. */
    public CpfRuntimeIdentity(String systemCode, String application, String instance) {
        this(systemCode, systemCode, application, instance);
    }

    public static CpfRuntimeIdentity from(Environment environment) {
        return from(CpfRuntimeMetadata.from(environment));
    }

    public static CpfRuntimeIdentity from(CpfRuntimeMetadata runtime) {
        if (runtime == null) throw new IllegalArgumentException("runtime");
        return new CpfRuntimeIdentity(runtime.systemCode(), runtime.systemCode(), runtime.application(), runtime.instanceId());
    }

    private static String requiredSystemCode(String value, String name) {
        String normalized = normalize(value, 32);
        if (normalized == null) throw new IllegalStateException("CPF runtime " + name + " is required");
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,31}")) {
            throw new IllegalStateException("Invalid CPF runtime " + name + ": " + normalized);
        }
        return normalized;
    }


    private static String requiredChannel(String value, String name) {
        String normalized = normalize(value, 16);
        if (normalized == null) throw new IllegalStateException("CPF runtime " + name + " is required");
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,15}")) {
            throw new IllegalStateException("Invalid CPF runtime " + name + ": " + normalized);
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
