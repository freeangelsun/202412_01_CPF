package com.cpf.foundation.runtime;

import java.net.InetAddress;
import java.util.Objects;
import org.springframework.core.env.Environment;

/**
 * CPF 프로세스가 기동 시 확정하는 topology-independent Runtime Metadata 정본입니다.
 * system/application/instance/host 값을 각 Capability가 다시 계산하지 않고 이 값을 공유합니다.
 */
public record CpfRuntimeMetadata(
        String systemCode, String application, String instanceId, String hostName, String hostIp) {

    public CpfRuntimeMetadata {
        systemCode = required("systemCode", systemCode, 32).toUpperCase(java.util.Locale.ROOT);
        application = required("application", application, 128);
        instanceId = required("instanceId", instanceId, 160);
        hostName = optional(hostName, 255);
        hostIp = optional(hostIp, 128);
    }

    public static CpfRuntimeMetadata from(Environment environment) {
        Objects.requireNonNull(environment, "environment");
        String systemCode = CpfRuntimeSystemCode.resolve(environment);

        String application = first(environment, "spring.application.name", "cpf.framework.application-id");
        if (!hasText(application)) application = systemCode;

        CpfInstanceIdentity.Identity instance = CpfInstanceIdentity.current();
        return new CpfRuntimeMetadata(systemCode, application, instance.instanceId(),
                instance.hostName(), resolveHostIp());
    }

    private static String resolveHostIp() {
        try {
            String value = InetAddress.getLocalHost().getHostAddress();
            return hasText(value) ? value.trim() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String first(Environment environment, String... keys) {
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (hasText(value)) return value.trim();
        }
        return null;
    }

    private static String required(String name, String value, int max) {
        String normalized = optional(value, max);
        if (normalized == null) throw new IllegalStateException("CPF runtime " + name + " is required");
        return normalized;
    }

    private static String optional(String value, int max) {
        if (!hasText(value)) return null;
        String normalized = value.trim();
        if (normalized.length() > max || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalStateException("Invalid CPF runtime metadata value");
        }
        return normalized;
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
