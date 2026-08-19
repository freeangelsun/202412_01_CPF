package com.cpf.foundation.runtime;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Set;

/**
 * CPF 프로세스/WAS의 단일 Runtime instance identity 정본입니다.
 *
 * <p>{@code cpf.runtime.instance-id} 시스템 속성, {@code CPF_RUNTIME_INSTANCE_ID} 환경변수,
 * 실제 Runtime hostname 순으로 instanceId를 기동 시 한 번 확정합니다. 명시 instanceId가 있으면
 * hostname 조회 실패가 instanceId 결정을 막지 않습니다.</p>
 */
public final class CpfInstanceIdentity {
    private static final Set<String> FORBIDDEN_INSTANCE_IDS = Set.of(
            "localhost", "127.0.0.1", "::1", "unknown", "local");
    private static final String EXPLICIT_INSTANCE_ID = explicitInstanceId();
    private static final String HOST_NAME = resolveHostName();
    private static final String INSTANCE_ID = resolveInstanceId(EXPLICIT_INSTANCE_ID, HOST_NAME);
    private static final String HOST_IP = resolveHostIp();
    private static final String PROCESS_ID = resolveProcessId();

    private CpfInstanceIdentity() {}

    public static Identity current() {
        return new Identity(INSTANCE_ID, HOST_NAME == null ? "" : HOST_NAME, HOST_IP, PROCESS_ID, Thread.currentThread().getName());
    }

    public static String instanceId() { return INSTANCE_ID; }
    public static String hostName() { return HOST_NAME == null ? "" : HOST_NAME; }

    private static String explicitInstanceId() {
        String configured = System.getProperty("cpf.runtime.instance-id");
        if (!hasText(configured)) configured = System.getenv("CPF_RUNTIME_INSTANCE_ID");
        return hasText(configured) ? configured.trim() : null;
    }

    static String resolveInstanceId(String explicit, String hostName) {
        if (hasText(explicit)) return validatedInstanceId(explicit, "explicit instanceId");
        if (hasText(hostName)) return validatedInstanceId(hostName, "Runtime hostname");
        throw new IllegalStateException(
                "CPF Runtime hostname을 확인할 수 없습니다. cpf.runtime.instance-id 또는 CPF_RUNTIME_INSTANCE_ID를 명시해야 합니다.");
    }

    private static String validatedInstanceId(String value, String source) {
        String normalized = value.trim();
        if (FORBIDDEN_INSTANCE_IDS.contains(normalized.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException(source + " cannot be used as CPF Runtime instanceId: " + normalized);
        }
        return normalized;
    }

    private static String resolveHostName() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            if (hasText(host)) return host.trim();
        } catch (Exception ignored) {
            // Explicit instanceId가 있으면 hostName 수집 실패는 허용합니다.
        }
        String environmentHost = System.getenv("HOSTNAME");
        if (!hasText(environmentHost)) environmentHost = System.getenv("COMPUTERNAME");
        return hasText(environmentHost) ? environmentHost.trim() : null;
    }


    private static String resolveHostIp() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            return hasText(ip) ? ip.trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String resolveProcessId() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        int separator = runtimeName.indexOf('@');
        return separator > 0 ? runtimeName.substring(0, separator) : runtimeName;
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }

    public record Identity(String instanceId, String hostName, String hostIp, String processId, String threadName) {}
}
